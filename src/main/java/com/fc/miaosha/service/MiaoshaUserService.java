package com.fc.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fc.miaosha.dao.MiaoshaUserDao;
import com.fc.miaosha.domain.MiaoshaUser;
import com.fc.miaosha.exception.GlobalException;
import com.fc.miaosha.redis.MiaoshaUserKey;
import com.fc.miaosha.redis.RedisService;
import com.fc.miaosha.result.CodeMsg;
import com.fc.miaosha.util.MD5Util;
import com.fc.miaosha.util.UUIDUtil;
import com.fc.miaosha.vo.LoginVo;

@Service
public class MiaoshaUserService {
	
	
	public static final String COOKI_NAME_TOKEN = "token";
	
	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;


	/*
	对象缓存：
    相比页面缓存是更细粒度缓存。在实际项目中， 不会大规模使用页面缓存，对象缓存就是当用到用户数据的时候，
    可以从缓存中取出。比如：更新用户密码，根据token来获取用户缓存对象。
	 */
	//根据id取得对象，先去缓存中取
	public MiaoshaUser getById(long id) {
		//1.取缓存	---先根据id来取得缓存
		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
		if(user != null) {  //能在缓存中拿到
			return user;
		}
		//2.缓存中拿不到，那么就去取数据库
		user = miaoshaUserDao.getById(id);
		//3.设置缓存
		if(user != null) {
			redisService.set(MiaoshaUserKey.getById, ""+id, user);
		}
		return user;
	}
	// http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323
    /*
    更新用户密码：更新数据库与缓存，一定要保证数据一致性，修改token关联的对象以及id关联的对象，
    先更新数据库后删除缓存，再将token以及对应的用户信息一起再写回缓存里面去。
    注意：不能直接删除token，删除之后就不能登录了，
     */
	public boolean updatePassword(String token, long id, String formPass) {
		//取user
		MiaoshaUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		MiaoshaUser toBeUpdate = new MiaoshaUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
		miaoshaUserDao.update(toBeUpdate);
		//处理缓存
		redisService.delete(MiaoshaUserKey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(MiaoshaUserKey.token, token, user);
		return true;
	}

    /**
     * 从缓存里面取得值，取得value
     */
	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        // 再次请求时，延长有效期
        // 重新设置缓存里面的值，使用之前cookie里面的token
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
	

	public String login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
        //经过了一次MD5的密码
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
        //查询不到该手机号的用户
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
        //手机号存在的情况，验证密码，获取数据库里面的密码与salt去验证
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
        //验证密码，计算二次MD5出来的pass是否与数据库一致
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成cookie
		String token = UUIDUtil.uuid();
		addCookie(response, token, user);
		return token;
	}

    /**
     * 添加或者叫做更新cookie
     */
	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        // 将token写到cookie当中，然后传递给客户端
        // 此token对应的是哪一个用户,将我们的私人信息存放到一个第三方的缓存中
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        // 设置cookie的有效期，与session有效期一致
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        // 设置网站的根目录
		cookie.setPath("/");
        // 需要写到response中
		response.addCookie(cookie);
	}

}
