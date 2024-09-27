package com.zlf.factory;

import com.zlf.builder.CustomThreadBuilder;
import com.zlf.event.ThreadPoolExecutorErrorEvent;
import com.zlf.utils.ZlfDisruptorSpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

@Data
@Slf4j
public class CustomThreadFactory implements ThreadFactory {

    private CustomThreadBuilder customThreadBuilder;

    public CustomThreadFactory(CustomThreadBuilder customThreadBuilder) {
        this.customThreadBuilder = customThreadBuilder;
    }

    @Override
    public Thread newThread(Runnable r) {
        UncaughtExceptionHandler uncaughtExceptionHandler = customThreadBuilder.getUncaughtExceptionHandler();
        Thread t = new Thread(r, customThreadBuilder.getName());
        //线程执行异常捕获
        if (Objects.isNull(uncaughtExceptionHandler)) {
            t.setUncaughtExceptionHandler((Thread t1, Throwable e) -> {
                e.printStackTrace();
                log.error("线程工厂设置exceptionHandler捕获线程执行异常:{}", e.getMessage());
                ThreadPoolExecutorErrorEvent threadPoolExecutorErrorEvent = new ThreadPoolExecutorErrorEvent(this, t1, e);
                ZlfDisruptorSpringUtils.getApplicationContext().publishEvent(threadPoolExecutorErrorEvent);
                log.info("====CustomFactory.捕获异常发送springBoot事件完成======");
            });
        } else {
            t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
        if (customThreadBuilder.getIsDaemon()) {
            t.setDaemon(true);
        }
        return t;
    }

}
