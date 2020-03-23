package com.gupao.spring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GPHandlerAdapter {


    public boolean supports(Object handler){

        return handler instanceof GPHandlerMapping;
    }


    GPModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return null;
    }

}
