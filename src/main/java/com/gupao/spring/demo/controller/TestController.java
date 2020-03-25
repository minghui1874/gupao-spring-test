package com.gupao.spring.demo.controller;

import com.gupao.spring.demo.service.TestService;
import com.gupao.spring.framework.annotation.GPController;
import com.gupao.spring.framework.annotation.GpAutowired;
import com.gupao.spring.framework.annotation.GpRequestMapping;
import com.gupao.spring.framework.annotation.GpRequestParam;
import com.gupao.spring.framework.webmvc.servlet.GPModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@GpRequestMapping("/demo")
@GPController
public class TestController {


    @GpAutowired
    TestService testService;

    @GpRequestMapping("/query.json")
    public GPModelAndView test(HttpServletRequest request, HttpServletResponse response,
                               @GpRequestParam("name") String name) {
        String result = null;
        try {
            result = testService.query(name);
        } catch (Exception e) {
            Map<String, Object> model = new HashMap<>();
            model.put("detail", "出错了");
            model.put("stackTrace", e.getStackTrace());
            return new GPModelAndView("500", model);
        }
        return out(response, result);

    }

    private GPModelAndView out(HttpServletResponse response, String result) {
        return null;
    }

//
//    @GpRequestMapping("/add")
//    public GPModelAndView test(HttpServletRequest request, HttpServletResponse response,
//                               @GpRequestParam("a") Integer a,
//                               @GpRequestParam("b") Integer b) {
//        try {
//            response.getWriter().write("a + b =  " + (a + b));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
