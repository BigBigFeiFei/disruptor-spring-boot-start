//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zlf.handler;

import com.lmax.disruptor.WorkHandler;
import com.zlf.event.DisruptorEvent;
import org.springframework.stereotype.Component;

@Component
public class ClearingWorkHandler implements WorkHandler<DisruptorEvent> {

    @Override
    public void onEvent(DisruptorEvent event) throws Exception {
        event.clear();
    }

}
