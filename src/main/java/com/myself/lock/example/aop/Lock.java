package com.myself.lock.example.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类名称：Lock<br>
 * 类描述：<br>
 * 创建时间：2018年12月26日<br>
 *
 * @author maopanpan
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Lock {
    String value() default "";
}
