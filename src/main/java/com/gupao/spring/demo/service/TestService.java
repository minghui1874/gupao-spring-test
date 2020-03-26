package com.gupao.spring.demo.service;

import com.gupao.spring.framework.annotation.GPService;

@GPService
public class TestService {

    public String query(String name) throws Exception {

        throw new Exception("处理异常啊");
//        return name + " test ";
    }
}
