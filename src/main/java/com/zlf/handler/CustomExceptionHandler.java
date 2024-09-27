package com.zlf.handler;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.ExceptionHandler;
import com.zlf.event.CustomExceptionHandlerEvent;
import com.zlf.event.DisruptorEvent;
import com.zlf.utils.ZlfDisruptorSpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class CustomExceptionHandler implements ExceptionHandler<DisruptorEvent> {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void handleEventException(Throwable ex, long sequence, DisruptorEvent event) {
        log.info("CustomExceptionHandler.handleEventException===>sequence:{},event:{},ex:{}", sequence, JSON.toJSONString(event), ex);
        //可以实际业务中需要根据实际情况记录补偿消费有异常的数据,业务测可以监听CustomExceptionHandlerEvent的springBoot的事件处理异常数据--做一个补偿处理即可
        CustomExceptionHandlerEvent exceptionHandlerEvent = new CustomExceptionHandlerEvent(this, ex, sequence, event);
        if (Objects.isNull(applicationContext)) {
            applicationContext = ZlfDisruptorSpringUtils.getApplicationContext();
        }
        applicationContext.publishEvent(exceptionHandlerEvent);
        log.info("======CustomExceptionHandler发送exceptionHandlerEvent:{}完成======", JSON.toJSONString(exceptionHandlerEvent));
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.info("CustomExceptionHandler.handleOnStartException===>ex:{}", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.info("CustomExceptionHandler.handleOnShutdownException===>ex:{}", ex);
    }

}
