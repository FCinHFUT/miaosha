package com.fc.miaosha.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fc.miaosha.access.AccessLimit;
import com.fc.miaosha.domain.MiaoshaOrder;
import com.fc.miaosha.domain.MiaoshaUser;
import com.fc.miaosha.rabbitmq.MQSender;
import com.fc.miaosha.rabbitmq.MiaoshaMessage;
import com.fc.miaosha.redis.GoodsKey;
import com.fc.miaosha.redis.MiaoshaKey;
import com.fc.miaosha.redis.OrderKey;
import com.fc.miaosha.redis.RedisService;
import com.fc.miaosha.result.CodeMsg;
import com.fc.miaosha.result.Result;
import com.fc.miaosha.service.GoodsService;
import com.fc.miaosha.service.MiaoshaService;
import com.fc.miaosha.service.MiaoshaUserService;
import com.fc.miaosha.service.OrderService;
import com.fc.miaosha.vo.GoodsVo;

/*
秒杀业务主要逻辑：

判断登录
根据商品id从数据库拿到商品
判断库存，库存足够，进行秒杀，不足则结束
判断是否重复秒杀（我们限制一个用户只能秒杀一件商品，怎么判断？即从数据库根据商品和用户id 查询秒杀订单表，如果已经存在订单，说明重复秒杀 ，给出提示，退出）
以上都通过，那么该用户可以秒杀商品
注意：
执行秒杀逻辑是一个原子操作，是一个事务：

库存减1
下订单（写入秒杀订单）
所以使用@Transactional注解标注，其中一步没有成功，则回滚
 */

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	MQSender sender;
	
	private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();
	
	/**
	 * 系统初始化： 商品库存数量预加载库存到Redis上
	 * MiaoshaController实现InitializingBean接口，重写afterPropertiesSet方法。
	 * 在容器启动的时候，检测到了实现了接口InitializingBean之后，就回去回调afterPropertiesSet方法。
	 * 将每种商品的库存数量加载到redis里面去。
	 */
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if(goodsList == null) {
			return;
		}
		//如果不是null的时候，将库存加载到redis里面去
		for(GoodsVo goods : goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}
	
	@RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for(GoodsVo goods : goodsList) {
			goods.setStockCount(10);
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
			localOverMap.put(goods.getId(), false);
		}
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
		redisService.delete(MiaoshaKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}
	
	/**
	 * QPS:1306
	 * 5000 * 10
	 * QPS: 2114
	 *
	 * 做页面静态化，直接返回订单的信息
	 *
	 * 后端接收秒杀请求的接口doMiaosha，收到请求，Redis预减库存（先减少Redis里面的库存数量，库存不足，直接返回），
	 * 如果库存已经到达临界值的时候，即=0，就不需要继续往下走，直接返回失败
	 * */
    @RequestMapping(value="/{path}/do_miaosha", method=RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId,
    		@PathVariable("path") String path) {
    	model.addAttribute("user", user);
		//如果用户为空，则返回至登录页面
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	//验证path
    	boolean check = miaoshaService.checkPath(user, goodsId, path);
    	if(!check){
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
    	//内存标记，减少redis访问
    	boolean over = localOverMap.get(goodsId);
    	if(over) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//预减库存
    	long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);//10
    	if(stock < 0) { //线程不安全---库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
    		 localOverMap.put(goodsId, true);
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) { //重复下单
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//正常请求，入队，发送一个秒杀message到队列里面去，入队之后客户端应该进行轮询。
    	MiaoshaMessage mm = new MiaoshaMessage();
    	mm.setUser(user);
    	mm.setGoodsId(goodsId);
    	sender.sendMiaoshaMessage(mm);
    	return Result.success(0);//排队中，并不知道是否可以秒杀成功
    	/*
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
    	//判断商品库存，库存大于0，才进行操作，多线程下会出错
    	int stock = goods.getStockCount();
    	if(stock <= 0) {  {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	////判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {  //重复下单
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//可以秒杀，原子操作： 1.减库存 2.下订单 3.写入秒杀订单---> 是一个事务
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
        */
    }
    
    /**
     * orderId：成功
	 *
	 * 客户端做一个轮询，查看是否成功与失败，失败了则不用继续轮询。
	 * 秒杀成功，返回订单的Id。
	 * 库存不足直接返回-1。
	 * 排队中则返回0。
	 * 查看是否生成秒杀订单。
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
    	//result返回轮询结果
    	return Result.success(result);
    }

	/**
	 * 获取秒杀的path,并且验证验证码的值是否正确
	 *
	 * 1.加上了秒杀接口地址隐藏之后可以防止恶意用户登陆之后，通过不断调用秒杀地址接口，骚扰服务器，所以使用动态获取秒杀地址，
	 * 只有真正点击秒杀按钮，才会根据用户id和商品goodsId生成对应的秒杀接口地址。
	 *
	 * 2.这种情况仍然不能解决利用机器人频繁点击按钮的操作，为了降低点击按钮的次数，以及高并发下，防止多个用户在同一时间内，
	 * 并发出大量请求，加入数学公式图形验证码以及接口防刷等优化技术。
	 */
	@AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId,
    		@RequestParam(value="verifyCode", defaultValue="0")int verifyCode
    		) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
    	if(!check) {
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
    	}
		//生成一个随机串
    	String path  =miaoshaService.createMiaoshaPath(user, goodsId);
    	return Result.success(path);
    }


	/**
	 * 生成图片验证码
	 */
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response,MiaoshaUser user,
    		@RequestParam("goodsId")long goodsId) {
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	try {
    		BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return Result.error(CodeMsg.MIAOSHA_FAIL);
    	}
    }
}
