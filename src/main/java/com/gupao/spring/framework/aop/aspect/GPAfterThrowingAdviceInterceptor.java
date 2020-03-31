package com.gupao.spring.framework.aop.aspect;

import com.gupao.spring.framework.aop.intercept.GPMethodInterceptor;
import com.gupao.spring.framework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPAfterThrowingAdviceInterceptor extends GPAbstractAspectAdvice implements GPMethodInterceptor {


    private String throwName;

    public GPAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(GPMethodInvocation invocation) throws Throwable {
        return null;
    }


    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }

    public String getThrowName() {
        return throwName;
    }
}
