package com.zlf.service;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolExecutorService {

    public static void execute(Runnable runnable, ThreadPoolExecutor executor) {
        if (runnable == null || executor == null) {
            return;
        }
        if (executor.isShutdown()) {
            return;
        }
        executor.execute(runnable);
    }

    public static Future submit1(Runnable runnable, ThreadPoolExecutor executor) {
        if (runnable == null || executor == null) {
            return null;
        }
        if (executor.isShutdown()) {
            return null;
        }
        return executor.submit(runnable);
    }

    public static <T> Future<T> submit2(Callable<T> callable, ThreadPoolExecutor executor) {
        if (callable == null || executor == null) {
            return null;
        }
        if (executor.isShutdown()) {
            return null;
        }
        return executor.submit(callable);
    }

    // 从线程队列中移除对象
    public static void cancel(Runnable runnable, ThreadPoolExecutor executor) {
        if (runnable == null || executor == null) {
            return;
        }
        if (executor.isShutdown()) {
            return;
        }
        executor.getQueue().remove(runnable);
    }

}
