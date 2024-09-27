package com.zlf.service;

import com.zlf.event.ThreadPoolExecutorErrorEvent;
import com.zlf.utils.ZlfDisruptorSpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadPoolService {

    private static final int DEFAULT_CORE_SIZE = 8;

    private static final int MAX_QUEUE_SIZE = 100;

    private static final int QUEUE_INIT_MAX_SIZE = 1000;

    private volatile static ThreadPoolExecutor executor;

    private ThreadPoolService() {

    }

    // 获取单例的线程池对象(默认实现)
    public static ThreadPoolExecutor getInstance() {
        if (executor == null) {
            synchronized (ThreadPoolService.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(DEFAULT_CORE_SIZE,// 核心线程数
                            MAX_QUEUE_SIZE, // 最大线程数
                            Integer.MAX_VALUE, // 闲置线程存活时间
                            TimeUnit.MILLISECONDS,// 时间单位
                            new LinkedBlockingDeque<>(QUEUE_INIT_MAX_SIZE),// 线程队列
                            Executors.defaultThreadFactory()// 线程工厂
                    ) {
                        //重写afterExecute方法
                        @Override
                        protected void afterExecute(Runnable r, Throwable t) {
                            if (t != null) {
                                log.error("afterExecute获取到异常信息:{}", t.getMessage());
                            }
                            if (r instanceof FutureTask) {
                                try {
                                    FutureTask<?> futureTask = (FutureTask<?>) r;
                                    futureTask.get();
                                } catch (Exception e) {
                                    log.error("afterExecute获取到submit提交的异常信息:{}", e.getMessage());
                                }
                            }
                            ThreadPoolExecutorErrorEvent threadPoolExecutorErrorEvent = new ThreadPoolExecutorErrorEvent(this, r, t);
                            ZlfDisruptorSpringUtils.getApplicationContext().publishEvent(threadPoolExecutorErrorEvent);
                            log.info("====ThreadPoolService.捕获异常发送springBoot事件完成======");
                        }
                    };
                    //自定义数据策略
                    executor.setRejectedExecutionHandler((r, executor) -> {
                        try {
                            if (executor.isShutdown()) {
                                return;
                            }
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            log.error("线程处理拒绝策略失败:{}", e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
        return executor;
    }

}