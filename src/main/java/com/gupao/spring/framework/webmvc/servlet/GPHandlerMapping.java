package com.gupao.spring.framework.webmvc.servlet;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

@Data
public class GPHandlerMapping {

    private Object controller; // 保存方法对应的实例
    private Method method; // 保存映射的方法
    private Pattern pattern; // URL的正则表达式

    public GPHandlerMapping(Pattern pattern, Object controller, Method method) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }
}
