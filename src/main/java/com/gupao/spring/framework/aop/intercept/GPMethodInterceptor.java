package com.gupao.spring.framework.aop.intercept;

/**
 * Created by Tom on 2019/4/14.
 */
public interface GPMethodInterceptor {
    Object invoke(GPMethodInvocation invocation) throws Throwable;
}
