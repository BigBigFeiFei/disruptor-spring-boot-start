package com.zlf.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ThreadPoolExecutorErrorEvent extends ApplicationEvent {

    private Runnable r;

    private Throwable t;

    public ThreadPoolExecutorErrorEvent(Object source, Runnable r, Throwable t) {
        super(source);
        this.r = r;
        this.t = t;
    }

}
