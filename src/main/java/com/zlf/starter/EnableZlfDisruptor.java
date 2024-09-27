package com.zlf.starter;

import com.zlf.handler.ClearingEventHandler;
import com.zlf.handler.ClearingWorkHandler;
import com.zlf.handler.CustomExceptionHandler;
import com.zlf.utils.ZlfDisruptorSpringUtils;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在启动类上加上该注解
 *
 * @author zlf
 * @date 2024/3/14
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DisruptorServiceConfiguration.class, ClearingEventHandler.class, ClearingWorkHandler.class, CustomExceptionHandler.class, ZlfDisruptorSpringUtils.class})
public @interface EnableZlfDisruptor {

}
