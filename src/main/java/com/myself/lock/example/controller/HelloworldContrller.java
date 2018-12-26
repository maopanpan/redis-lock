package com.myself.lock.example.controller;

import com.myself.lock.example.aop.Lock;
import com.myself.lock.example.aop.Param;
import com.myself.lock.example.service.HelloworldService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类名称：HelloworldContrller<br>
 * 类描述：<br>
 * 创建时间：2018年12月26日<br>
 *
 * @author maopanpan
 * @version 1.0.0
 */
@RestController
public class HelloworldContrller {
    private final HelloworldService helloworldService;

    public HelloworldContrller(HelloworldService helloworldService) {
        this.helloworldService = helloworldService;
    }

    @GetMapping(value = "/sayHello")
    @Lock
    public String sayHello(String message, @Param String name) {
        return helloworldService.sayHello(message);
    }
}
