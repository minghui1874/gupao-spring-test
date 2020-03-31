/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gupao.spring.framework.aop.support;

import com.gupao.spring.framework.aop.aspect.GPMethodBeforeAdviceInterceptor;
import com.gupao.spring.framework.aop.config.GPAopConfig;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPAdvisedSupport {

    private Class<?> targetClass;
    private Object target;
    private GPAopConfig config;
    private Pattern pointCutClassPattern;

    public GPAdvisedSupport(GPAopConfig config) {
        this.config = config;
    }


    public void setTarget(Object target) {
        this.target = target;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public Object getTarget() {
        return this.target;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception {
//		List<Object> cached = methodCache.get(method);
//		if(cached == null){
//			Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
//
//			cached = methodCache.get(m);
//
//			//底层逻辑，对代理方法进行一个兼容处理
//			this.methodCache.put(m,cached);
//		}
//
//		return cached;
        return null;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    private void parse() {
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        // pointCut=public .* com.gupao.spring.demo.service..*Service..*(.*)
        String pointCut4ClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class "
                + pointCut4ClassRegex.substring(pointCut4ClassRegex.lastIndexOf(" ") + 1));


//                aspectClass=com.gupao.spring.demo.aspect.LogAspect
//                aspectBefore=before
//                aspectAfter=after
//                aspectAfterThrow=afterThrow
//                aspectAfterThrowingName=java.lang.Exception

        Pattern pattern = Pattern.compile(pointCut);
        for (Method method : this.getTargetClass().getMethods()) {
            String methodString = method.toString();
            if (methodString.contains("throws")) {
                methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
            }

            Matcher matcher = pattern.matcher(methodString);
            if (matcher.matches()) {
                // 把每一个方法包装成 MethodInterceptor
                List<Object> advices = new LinkedList<>();

                //     before
                String aspectBefore = config.getAspectBefore();
                if (!(null == aspectBefore || "".equals(aspectBefore))) {
                    advices.add(new GPMethodBeforeAdviceInterceptor());
                }

                //     after
                String aspectAfter = config.getAspectAfter();
                if (!(null == aspectAfter || "".equals(aspectAfter))) {
                    advices.add();
                }

                //     afterThrow
                String aspectAfterThrow = config.getAspectAfterThrow();
                if (!(null == aspectAfterThrow || "".equals(aspectAfterThrow))) {
                    advices.add();
                }

            }
        }


    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}
