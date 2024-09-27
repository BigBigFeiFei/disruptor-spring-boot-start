package com.zlf.handler;

import com.lmax.disruptor.EventHandler;
import com.zlf.event.DisruptorEvent;
import org.springframework.stereotype.Component;
/**
 * 最后一个handler加入处理
 */
@Component
public class ClearingEventHandler implements EventHandler<DisruptorEvent> {

    @Override
    public void onEvent(DisruptorEvent event, long sequence, boolean endOfBatch) throws Exception {
        event.clear();
    }

}
