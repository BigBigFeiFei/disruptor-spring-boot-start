package com.zlf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 等待策略
 * 常用一下几种策略,还有其它几种不推荐使用
 */
@Getter
@AllArgsConstructor
public enum WaitStrategyEnum {

    SLEEP("SLEEP", "com.lmax.disruptor.SleepingWaitStrategy", "三段式，一阶段自旋，二阶段执行Thread.yield，三阶段睡眠，性能表现跟BlockingWaitStrategy差不多，对CPU的消耗也类似，但其对生产者线程的影响最小，适合用于异步日志类似的场景"),

    YIELD("YIELD", "com.lmax.disruptor.YieldingWaitStrategy", "二段式，一阶段自旋100次，二阶段执行Thread.yield，需要低延迟的场景可使用此策略，是可以被用在低延迟系统中的两个策略之一，这种策略在减低系统延迟的同时也会增加CPU运算量"),

    BLOCK("BLOCK", "com.lmax.disruptor.BlockingWaitStrategy", "默认策略(是最低效的策略)。使用锁和Condition 的等待、唤醒机制。速度慢，但节省CPU资源并且在不同部署环境中能提供更加一致的性能表现"),

    BUSYSPIN("BUSYSPIN", "com.lmax.disruptor.BusySpinWaitStrategy", "性能最高的策略，与YieldingWaitStrategy一样在低延迟场景使用，但是此策略要求消费者数量低于CPU逻辑内核总数"),

    TIMEOUT("TIMEOUT", "com.lmax.disruptor.TimeoutBlockingWaitStrategy", "加锁，有超时限制CPU资源紧缺，吞吐量和延迟并不重要的场景");

    /**
     * 类型
     */
    private String type;
    /**
     * 类名称
     */
    private String className;
    /**
     * 描述
     */
    private String desc;

}
