package com.zlf.task.rejected;


import java.util.concurrent.ThreadPoolExecutor;


public interface RejectedAware {

    /**
     * 拒绝粗略之前执行逻辑(可以发邮件告警/错误次数计数等操作)
     *
     * @param executor ThreadPoolExecutor instance
     */
    default void beforeReject(ThreadPoolExecutor executor) {

    }

}
