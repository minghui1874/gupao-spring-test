package com.gupao.spring.demo.controller;

import com.gupao.spring.framework.annotation.GPController;
import com.gupao.spring.framework.annotation.GpRequestMapping;
import com.gupao.spring.framework.annotation.GpRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@GpRequestMapping(name = "/demo")
@GPController
public class TestController {

    @GpRequestMapping(name = "/print")
    public void test(HttpServletRequest request, HttpServletResponse response,
                     @GpRequestParam("name") String name) {
        try {
            response.getWriter().write("my name is " + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @GpRequestMapping(name = "/add")
    public void test(HttpServletRequest request, HttpServletResponse response,
                     @GpRequestParam("a") Integer a,
                     @GpRequestParam("b") Integer b) {
        try {
            response.getWriter().write("a + b =  " + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
