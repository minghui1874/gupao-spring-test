/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gupao.spring.framework.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which indicates that a method parameter should be bound to a web request header.
 *
 * <p>Supported for annotated handler methods in Spring MVC and Spring WebFlux.
 *
 * <p>If the method parameter is {@link java.util.Map Map&lt;String, String&gt;},
 * populated with all header names and values.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPRequestHeader {

	/**
	 * Alias for {@link #name}.
	 */
	String value() default "";

	/**
	 * The name of the request header to bind to.
	 * @since 4.2
	 */
	String name() default "";

	/**
	 * Whether the header is required.
	 * <p>Defaults to {@code true}, leading to an exception being thrown
	 * if the header is missing in the request. Switch this to
	 * {@code false} if you prefer a {@code null} value if the header is
	 * not present in the request.
	 * sets this flag to {@code false}.
	 */
	boolean required() default true;


}
