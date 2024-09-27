package com.zlf.factory;

import com.lmax.disruptor.EventFactory;
import com.zlf.event.DisruptorEvent;

public class DisruptorEventFactory implements EventFactory<DisruptorEvent> {

    @Override
    public DisruptorEvent newInstance() {
        return new DisruptorEvent();
    }

}
