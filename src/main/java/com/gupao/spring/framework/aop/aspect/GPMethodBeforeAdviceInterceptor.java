package com.gupao.spring.framework.aop.aspect;

import com.gupao.spring.framework.aop.intercept.GPMethodInterceptor;
import com.gupao.spring.framework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPMethodBeforeAdviceInterceptor extends GPAbstractAspectAdvice implements GPMethodInterceptor {

    public GPMethodBeforeAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(GPMethodInvocation invocation) throws Throwable {
        return null;
    }
}
