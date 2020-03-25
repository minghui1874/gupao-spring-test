package com.gupao.spring.framework.webmvc.servlet;

import com.gupao.spring.framework.annotation.GpRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GPHandlerAdapter {


    public boolean supports(Object handler) {


        return (handler instanceof GPHandlerMapping);
    }


    public GPModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        GPHandlerMapping handlerMapping = (GPHandlerMapping) handler;
        // 把方法的形参列表和request的参数列表顺序进行一一对应
        Map<String, Integer> paramIndexMapping = new HashMap<>();

        // 把方法上的注解拿到，获取到一个二维数组
        // 同一个方法可能有多个参数，且同一个参数上可以添加多个注解
        // 所以是二维数组
        Annotation[][] annotations = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof GpRequestParam) {
                    String paramName = ((GpRequestParam) annotation).value();
                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }


        // 提取方法中的request和response参数类型
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramIndexMapping.put(type.getName(), i);
            }
        }


        // 获取方法的形参列表
        Map<String, String[]> params = request.getParameterMap();

        // 实参列表
        Object[] paramValues = new Object[parameterTypes.length];

        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", ",");

            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            Integer index = paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(parameterTypes[index], value);
        }

        // 处理req的参数位置
        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            Integer reqIndex = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = request;

        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            // 处理req的参数位置
            Integer respIndex = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = response;

        }


        Object returnValue = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);
        if (returnValue == null || returnValue instanceof Void) {
            return null;
        }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == GPModelAndView.class;
        if (isModelAndView) {
            return (GPModelAndView) returnValue;
        }

        return null;
    }

    // url传过来的参数都是String类型的 ，HTTP是基于字符串协议
    // 只需要把String转换为对应类型就好
    private Object convert(Class<?> type, String value) {
        // 可以使用策略模式优化
        if (Integer.class == type) {
            return Integer.valueOf(value);
        } else if (Double.class == type) {
            return Double.valueOf(value);
        }

        return value;
    }


}
