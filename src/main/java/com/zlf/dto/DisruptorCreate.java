package com.zlf.dto;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.BasicExecutor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.zlf.builder.CustomThreadBuilder;
import com.zlf.builder.WaitStrategyBuilder;
import com.zlf.factory.CustomThreadFactory;
import com.zlf.factory.DisruptorEventFactory;
import lombok.Data;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

@Data
public class DisruptorCreate {

    /**
     * 业务名称(唯一即可)：任意字符串说明即可，一个业务名称对应一个Disruptor,不用过多创建对象，否则是浪费堆内存
     */
    private String threadFactoryName = "disruptor-factory:" + UUID.randomUUID().toString().replace("-", "");

    /**
     * 生产者类型：单生产者或多生产者
     */
    private ProducerType producerType = ProducerType.SINGLE;

    /**
     * 2的整数次幂 比如1024,2048,,,,,
     */
    private int bufferSize = 1024;

    /**
     * 等待策略
     */
    private WaitStrategy waitStrategy = WaitStrategyBuilder.builder().build().createWaitStrategy();

    /**
     * 事件工厂 可自定义
     */
    private EventFactory eventFactory = new DisruptorEventFactory();

    /**
     * 线程工厂 可自定义
     */
    private ThreadFactory threadFactory = new CustomThreadFactory(CustomThreadBuilder.builder()
            .name(threadFactoryName).isDaemon(Boolean.TRUE).build());

    /**
     * 线程池执行器 可自定义
     */
    private Executor executor = new BasicExecutor(DaemonThreadFactory.INSTANCE);

}
