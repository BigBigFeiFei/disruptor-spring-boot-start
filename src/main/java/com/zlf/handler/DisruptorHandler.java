package com.zlf.handler;

import com.lmax.disruptor.dsl.Disruptor;

/**
 * 实现该接口可以个性化构建消费者处理链路
 */
public interface DisruptorHandler {

    void buildHandler(String key, Disruptor disruptor);

}
