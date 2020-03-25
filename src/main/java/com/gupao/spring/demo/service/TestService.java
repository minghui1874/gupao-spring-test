package com.gupao.spring.demo.service;

import com.gupao.spring.framework.annotation.GPService;

import java.io.FileNotFoundException;

@GPService
public class TestService {

    public String query(String name) throws Exception {

        throw new Exception("处理异常啊", new FileNotFoundException());
//        return name + " test ";
    }
}
