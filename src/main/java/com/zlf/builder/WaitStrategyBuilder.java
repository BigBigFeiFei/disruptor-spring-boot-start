package com.zlf.builder;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.zlf.enums.WaitStrategyEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Data
@Builder
public class WaitStrategyBuilder {

    private Long timeoutMillis = 10L;

    private Long sleepTimeNs = 100L;

    private Integer retries = 100;

    private WaitStrategyEnum waitStrategyEnum;

    public WaitStrategy createWaitStrategy() {
        if (Objects.nonNull(waitStrategyEnum) && WaitStrategyEnum.SLEEP.getType().equals(waitStrategyEnum.getType())) {
            return new SleepingWaitStrategy(retries, sleepTimeNs);
        } else if (Objects.nonNull(waitStrategyEnum) && WaitStrategyEnum.YIELD.getType().equals(waitStrategyEnum.getType())) {
            return new YieldingWaitStrategy();
        } else if (Objects.nonNull(waitStrategyEnum) && WaitStrategyEnum.BLOCK.getType().equals(waitStrategyEnum.getType())) {
            return new BlockingWaitStrategy();
        } else if (Objects.nonNull(waitStrategyEnum) && WaitStrategyEnum.BUSYSPIN.getType().equals(waitStrategyEnum.getType())) {
            return new BusySpinWaitStrategy();
        } else if (Objects.nonNull(waitStrategyEnum) && WaitStrategyEnum.TIMEOUT.getType().equals(waitStrategyEnum.getType())) {
            return new TimeoutBlockingWaitStrategy(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            return new YieldingWaitStrategy();
        }
    }

    /**
     * 等待策略默认创建：YIELD
     *
     * @return
     */
    public WaitStrategy createWaitStrategy0() {
        return WaitStrategyBuilder.builder().build().createWaitStrategy();
    }

}
