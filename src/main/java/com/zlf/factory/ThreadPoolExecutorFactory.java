package com.zlf.factory;

import com.zlf.builder.ThreadPoolExecutorBuilder;
import com.zlf.enums.BlockingQueueTypeEnum;
import com.zlf.enums.RejectedPolicyTypeEnum;
import com.zlf.event.ThreadPoolExecutorErrorEvent;
import com.zlf.utils.ZlfDisruptorSpringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 不建议创建多个(搞一个全局静态的足够了)
 */
@Data
@Slf4j
public class ThreadPoolExecutorFactory {

    private ThreadPoolExecutorBuilder threadPoolExecutorBuilder;

    public ThreadPoolExecutorFactory(ThreadPoolExecutorBuilder threadPoolExecutorBuilder) {
        this.threadPoolExecutorBuilder = threadPoolExecutorBuilder;
    }

    public ThreadPoolExecutor createThreadPoolExecutor() {
        BlockingQueue<Runnable> workQueue = null;
        RejectedExecutionHandler rejectedExecutionHandler = null;
        int defaultCoreSize = threadPoolExecutorBuilder.getDefaultCoreSize();
        if (Objects.isNull(defaultCoreSize) || defaultCoreSize <= 0) {
            defaultCoreSize = 8;
        }
        int maxQueueSize = threadPoolExecutorBuilder.getMaxQueueSize();
        if (Objects.isNull(maxQueueSize) || maxQueueSize <= 0) {
            maxQueueSize = 100;
        }
        long keepAliveTime = threadPoolExecutorBuilder.getKeepAliveTime();
        if (Objects.isNull(keepAliveTime) || keepAliveTime < 0) {
            keepAliveTime = Integer.MAX_VALUE;
        }
        TimeUnit unit = threadPoolExecutorBuilder.getUnit();
        if (Objects.isNull(unit)) {
            unit = TimeUnit.MILLISECONDS;
        }
        int queueInitMaxSize = threadPoolExecutorBuilder.getQueueInitMaxSize();
        if (Objects.isNull(queueInitMaxSize) || queueInitMaxSize <= 0) {
            queueInitMaxSize = 1000;
        }
        BlockingQueueTypeEnum blockingQueueTypeEnum = threadPoolExecutorBuilder.getBlockingQueueTypeEnum();
        if (Objects.isNull(blockingQueueTypeEnum)) {
            workQueue = new LinkedBlockingDeque<>(queueInitMaxSize);
        } else {
            if (Objects.isNull(threadPoolExecutorBuilder.getBlockingQueueIsFair())) {
                threadPoolExecutorBuilder.setBlockingQueueIsFair(false);
            }
            workQueue = BlockingQueueTypeEnum.buildBq(blockingQueueTypeEnum.getName(), queueInitMaxSize, threadPoolExecutorBuilder.getBlockingQueueIsFair());
        }
        ThreadFactory threadFactory = threadPoolExecutorBuilder.getThreadFactory();
        if (Objects.isNull(threadFactory)) {
            threadFactory = Executors.defaultThreadFactory();
        }
        RejectedPolicyTypeEnum rejectedPolicyTypeEnum = threadPoolExecutorBuilder.getRejectedPolicyTypeEnum();
        if (Objects.isNull(rejectedPolicyTypeEnum)) {
            rejectedExecutionHandler = (r, executor2) -> {
                try {
                    if (executor2.isShutdown()) {
                        return;
                    }
                    executor2.getQueue().put(r);
                } catch (InterruptedException e) {
                    log.error("线程处理拒绝策略失败2:{}", e.getMessage());
                    e.printStackTrace();
                }
            };
        } else {
            rejectedExecutionHandler = RejectedPolicyTypeEnum.getRejectedPolicyTypeEnumByName(rejectedPolicyTypeEnum.getName());
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                defaultCoreSize,// 核心线程数
                maxQueueSize, // 最大线程数
                keepAliveTime, // 闲置线程存活时间
                unit,// 时间单位
                workQueue,// 线程队列
                threadFactory// 线程工厂
        ) {
            //重写afterExecute方法
            protected void afterExecute(Runnable r, Throwable t) {
                if (t != null) {
                    log.error("afterExecute获取到异常信息2:{}", t.getMessage());
                }
                if (r instanceof FutureTask) {
                    try {
                        FutureTask<?> futureTask = (FutureTask<?>) r;
                        futureTask.get();
                    } catch (Exception e) {
                        log.error("afterExecute获取到submit提交的异常信息2:{}", e.getMessage());
                    }
                }
                ThreadPoolExecutorErrorEvent threadPoolExecutorErrorEvent = new ThreadPoolExecutorErrorEvent(this, r, t);
                ZlfDisruptorSpringUtils.getApplicationContext().publishEvent(threadPoolExecutorErrorEvent);
                log.info("====ThreadPoolExecutorFactory.捕获异常发送springBoot事件完成======");
            }
        };
        //自定义数据策略
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        return executor;
    }

}
