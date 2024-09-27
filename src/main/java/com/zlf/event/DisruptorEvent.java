package com.zlf.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件(Event父类) 就是通过 Disruptor 进行交换的数据类型。
 */
@Data
public class DisruptorEvent {

    /**
     * 时间标记
     */
    private LocalDateTime ldt = LocalDateTime.now();

    /**
     * 事件交换数据o1,o2,o3,objectList,子类可以继承拓展实现
     */
    private Object o1;

    private Object o2;

    private Object o3;

    private List<Object> objectList;

    /**
     * 如果是大对象需要增加ClearingEventHandler来触发清理
     */
    public void clear() {
        o1 = null;
        o2 = null;
        o3 = null;
        objectList = null;
    }


}
