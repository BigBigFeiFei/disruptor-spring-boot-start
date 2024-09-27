package com.zlf.builder;

import com.zlf.enums.BlockingQueueTypeEnum;
import com.zlf.enums.RejectedPolicyTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Data
@Builder
public class ThreadPoolExecutorBuilder {

    private int defaultCoreSize;

    private int maxQueueSize;

    private long keepAliveTime;

    private TimeUnit unit;

    private BlockingQueueTypeEnum blockingQueueTypeEnum;

    private Boolean blockingQueueIsFair;

    private int queueInitMaxSize;

    private ThreadFactory threadFactory;

    private RejectedPolicyTypeEnum rejectedPolicyTypeEnum;

}
