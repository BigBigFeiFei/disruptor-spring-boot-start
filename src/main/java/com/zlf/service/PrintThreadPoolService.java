package com.zlf.service;

import com.zlf.builder.CustomThreadBuilder;
import com.zlf.factory.CustomThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PrintThreadPoolService {

    private static final CustomThreadFactory customFactory = new CustomThreadFactory(CustomThreadBuilder.builder().name("PrintThreadPoolService-print-thread-pool-status").build());

    private static final ScheduledExecutorService printScheduledExecutorService = new ScheduledThreadPoolExecutor(1, customFactory);

    /**
     * 打印线程池的状态
     *
     * @param threadPool
     */
    public static ScheduledExecutorService printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        printScheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("===========printThreadPoolStatus==============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: {}", threadPool.getActiveCount());
            log.info("Number of Tasks: {}", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("=========================");
        }, 0, 1, TimeUnit.SECONDS);
        //返回printScheduledExecutorService,可以随时关闭这个printScheduledExecutorService
        return printScheduledExecutorService;
    }

}
