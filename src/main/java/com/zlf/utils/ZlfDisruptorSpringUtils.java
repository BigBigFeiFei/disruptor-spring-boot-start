package com.zlf.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zlf
 * @description spring上下文工具类
 * @date 2024/09/23
 **/
@Component
public class ZlfDisruptorSpringUtils implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ZlfDisruptorSpringUtils.class);
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.info("应用程序上下文 ： [{}]", "开始初始化");
        ZlfDisruptorSpringUtils.applicationContext = applicationContext;
        logger.info("应用程序上下文 ： [{}]", "初始化完成");
    }

    /**
     * 获取applicationContext
     * 发给
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}

