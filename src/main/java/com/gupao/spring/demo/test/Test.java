package com.gupao.spring.demo.test;

import com.gupao.spring.framework.context.GPApplicationContext;

public class Test {
    public static void main(String[] args) {
        GPApplicationContext context = new GPApplicationContext("classpath:application.properties");
        System.out.println(context);
    }
}
