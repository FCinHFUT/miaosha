package com.fc.miaosha.controller;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {

    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "hello world";
    }
    public static void main(String[] args) {
        SpringApplication.run(DemoController.class, args);
    }
}