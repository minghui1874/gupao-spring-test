/*
 * Copyright 2002-2016 the original author or authors.
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
 * Annotation for mapping HTTP {@code PUT} requests onto specific handler
 * methods.
 *
 * <p>Specifically, {@code @PutMapping} is a <em>composed annotation</em> that
 * acts as a shortcut for {@code @RequestMapping(method = RequestMethod.PUT)}.
 *
 * @author Sam Brannen
 * @since 4.3
 * @see GetMapping
 * @see GpPostMapping
 * @see GpPatchMapping
 * @see GPRequestMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GPPutMapping {

	/**
	 * Alias for {@link GPRequestMapping#name}.
	 */
	String name() default "";

	/**
	 */
	String[] value() default {};

	/**
	 */
	String[] path() default {};

	/**
	 */
	String[] params() default {};

	/**
	 */
	String[] headers() default {};

	/**
	 */
	String[] consumes() default {};

	/**
	 */
	String[] produces() default {};

}
