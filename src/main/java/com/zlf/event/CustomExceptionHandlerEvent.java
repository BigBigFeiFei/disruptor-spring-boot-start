package com.zlf.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CustomExceptionHandlerEvent extends ApplicationEvent {

    private Throwable ex;

    private long sequence;

    private DisruptorEvent event;

    public CustomExceptionHandlerEvent(Object source, Throwable ex, long sequence, DisruptorEvent event) {
        super(source);
        this.ex = ex;
        this.sequence = sequence;
        this.event = event;
    }

}