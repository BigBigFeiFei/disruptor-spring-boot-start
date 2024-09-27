package com.zlf.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.lang.copier.Copier;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.zlf.dto.DisruptorCreate;
import com.zlf.enums.DisruptorCreateMethodEnum;
import com.zlf.event.DisruptorEvent;
import com.zlf.factory.DisruptorEventFactory;
import com.zlf.handler.DisruptorHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 属性对应关系说明:
 * disruptorMaps:disruptor创建对应关系
 * keyMaps:key构建对应关系
 * <p>
 * 添加消费者处理器类(Disruptor第一次启动前加入)
 * disruptor.handleEventsWith(EventHandler... eventHandlers):实现EventHandler接口的处理类消费相同的消息(全部执行)
 * disruptor.then(EventHandler... eventHandlers):合并处理操作
 * disruptor.handleEventsWithWorkerPool(WorkHandler... workHandlers):实现WorkHandler接口的处理类消费相同的消息(只有一个会执行)
 * disruptor.thenHandleEventsWithWorkerPool(WorkHandler... workHandlers):合并处理操作(只有一个会执行)
 * <p>
 * bizName格式：
 * 业务名称(唯一即可)：任意字符串说明即可，一个业务名称对应一个Disruptor,不用过多创建对象，否则是浪费堆内存
 * <p>
 * 下面的key的格式：
 * key = DisruptorCreateMethodEnum.CREATE0.getName() + bizName
 * key = DisruptorCreateMethodEnum.CREATE1.getName() + bizName
 * key = DisruptorCreateMethodEnum.CREATE2.getName() + bizName
 * key = DisruptorCreateMethodEnum.CREATE3.getName() + bizName
 * key = DisruptorCreateMethodEnum.CREATE4.getName() + bizName
 */
@Data
@Slf4j
public class DisruptorService {

    private ConcurrentHashMap<String, Tuple> disruptorMaps = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Tuple> keyMaps = new ConcurrentHashMap<>();

    /**
     * 构建一个key
     *
     * @param disruptorCreateMethodEnum
     * @param bizName
     * @return
     */
    public Tuple buildKey(DisruptorCreateMethodEnum disruptorCreateMethodEnum, String bizName) {
        if (Objects.isNull(disruptorCreateMethodEnum)) {
            throw new RuntimeException("DisruptorService.buildKey.disruptorCreateMethodEnum不为空!");
        }
        if (StringUtils.isBlank(bizName)) {
            throw new RuntimeException("DisruptorService.buildKey.bizName不为空!");
        }
        String key = disruptorCreateMethodEnum.getName() + bizName;
        Tuple tuple = keyMaps.get(key);
        if (Objects.nonNull(tuple)) {
            return tuple;
        }
        tuple = new Tuple(bizName, key, disruptorCreateMethodEnum);
        keyMaps.put(key, tuple);
        return tuple;
    }

    /**
     * @param key
     * @return
     */
    public Tuple create0(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.create0.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        ConcurrentHashMap<String, DisruptorCreate> bizNameDcbMaps = DisruptorCreateMethodEnum.CREATE0.getDcbMaps();
        DisruptorCreate dcb = bizNameDcbMaps.get(key);
        if (Objects.isNull(dcb)) {
            throw new RuntimeException("DisruptorService.create0.请先设置DisruptorCreatMethodEnum的createDcbMaps对应关系!");
        }
        int bufferSize = dcb.getBufferSize();
        WaitStrategy waitStrategy = dcb.getWaitStrategy();
        if (Objects.isNull(bufferSize)) {
            throw new RuntimeException("DisruptorService.create0.bufferSize不为空,2的整数次幂 比如1024,2048,,,,,!");
        }
        if (Objects.isNull(waitStrategy)) {
            throw new RuntimeException("DisruptorService.create0.waitStrategy等待策略不为空-- 2的整数次幂 比如1024,2048,,,,!");
        }
        if (Objects.nonNull(disruptorMaps.get(key))) {
            return disruptorMaps.get(key);
        }
        Copier<DisruptorEventFactory> cp = DisruptorEventFactory::new;
        DisruptorEventFactory eventFactory = cp.copy();
        Disruptor<DisruptorEvent> disruptor = new Disruptor<>(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, waitStrategy);
        Tuple tuple = new Tuple(disruptor, disruptor.getRingBuffer(), new AtomicBoolean(false));
        disruptorMaps.put(key, tuple);
        return tuple;
    }

    /**
     * @param key
     * @return
     */
    public Tuple create1(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.create1.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        ConcurrentHashMap<String, DisruptorCreate> bizNameDcbMaps = DisruptorCreateMethodEnum.CREATE1.getDcbMaps();
        DisruptorCreate dcb = bizNameDcbMaps.get(key);
        if (Objects.isNull(dcb)) {
            throw new RuntimeException("DisruptorService.create1.请先设置DisruptorCreatMethodEnum的createDcbMaps对应关系!");
        }
        int bufferSize = dcb.getBufferSize();
        ProducerType producerType = dcb.getProducerType();
        WaitStrategy waitStrategy = dcb.getWaitStrategy();
        if (Objects.isNull(bufferSize)) {
            throw new RuntimeException("DisruptorService.create1.bufferSize不为空,2的整数次幂 比如1024,2048,,,,,!");
        }
        if (Objects.isNull(producerType)) {
            throw new RuntimeException("DisruptorService.create1.producerType生产者类型不为空!");
        }
        if (Objects.isNull(waitStrategy)) {
            throw new RuntimeException("DisruptorService.create1.waitStrategy等待策略不为空-- 2的整数次幂 比如1024,2048,,,,!");
        }
        if (Objects.nonNull(disruptorMaps.get(key))) {
            return disruptorMaps.get(key);
        }
        Copier<DisruptorEventFactory> cp = DisruptorEventFactory::new;
        DisruptorEventFactory eventFactory = cp.copy();
        Disruptor<DisruptorEvent> disruptor = null;
        if (producerType.equals(ProducerType.MULTI)) {
            disruptor = new Disruptor<>(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE,
                    ProducerType.MULTI, waitStrategy);
        } else {
            disruptor = new Disruptor<>(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE,
                    ProducerType.SINGLE, waitStrategy);
        }
        Tuple tuple = new Tuple(disruptor, disruptor.getRingBuffer(), new AtomicBoolean(false));
        disruptorMaps.put(key, tuple);
        return tuple;
    }

    /**
     * @param key
     * @return
     */
    public Tuple create2(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.create2.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        ConcurrentHashMap<String, DisruptorCreate> bizNameDcbMaps = DisruptorCreateMethodEnum.CREATE2.getDcbMaps();
        DisruptorCreate dcb = bizNameDcbMaps.get(key);
        if (Objects.isNull(dcb)) {
            throw new RuntimeException("DisruptorService.create2.请先设置DisruptorCreatMethodEnum的createDcbMaps对应关系!");
        }
        int bufferSize = dcb.getBufferSize();
        EventFactory eventFactory = dcb.getEventFactory();
        ProducerType producerType = dcb.getProducerType();
        WaitStrategy waitStrategy = dcb.getWaitStrategy();
        if (Objects.isNull(eventFactory)) {
            throw new RuntimeException("DisruptorService.create2.eventFactory自定义事件工厂不为空!");
        }
        if (Objects.isNull(bufferSize)) {
            throw new RuntimeException("DisruptorService.create2.bufferSize不为空,2的整数次幂 比如1024,2048,,,,,!");
        }
        if (Objects.isNull(producerType)) {
            throw new RuntimeException("DisruptorService.create2.producerType生产者类型不为空!");
        }
        if (Objects.isNull(waitStrategy)) {
            throw new RuntimeException("DisruptorService.create2.waitStrategy等待策略不为空-- 2的整数次幂 比如1024,2048,,,,!");
        }
        if (Objects.nonNull(disruptorMaps.get(key))) {
            return disruptorMaps.get(key);
        }
        Disruptor<DisruptorEvent> disruptor = null;
        if (producerType.equals(ProducerType.MULTI)) {
            disruptor = new Disruptor<>(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE,
                    ProducerType.MULTI, waitStrategy);
        } else {
            disruptor = new Disruptor<>(eventFactory, bufferSize, DaemonThreadFactory.INSTANCE,
                    ProducerType.SINGLE, waitStrategy);
        }
        Tuple tuple = new Tuple(disruptor, disruptor.getRingBuffer(), new AtomicBoolean(false));
        disruptorMaps.put(key, tuple);
        return tuple;
    }

    /**
     * @param key
     * @return
     */
    public Tuple create3(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.create3.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        ConcurrentHashMap<String, DisruptorCreate> bizNameDcbMaps = DisruptorCreateMethodEnum.CREATE3.getDcbMaps();
        DisruptorCreate dcb = bizNameDcbMaps.get(key);
        if (Objects.isNull(dcb)) {
            throw new RuntimeException("DisruptorService.create3.请先设置DisruptorCreatMethodEnum的createDcbMaps对应关系!");
        }
        int bufferSize = dcb.getBufferSize();
        EventFactory eventFactory = dcb.getEventFactory();
        ThreadFactory threadFactory = dcb.getThreadFactory();
        ProducerType producerType = dcb.getProducerType();
        WaitStrategy waitStrategy = dcb.getWaitStrategy();
        if (Objects.isNull(eventFactory)) {
            throw new RuntimeException("DisruptorService.create3.eventFactory自定义事件工厂不为空!");
        }
        if (Objects.isNull(threadFactory)) {
            throw new RuntimeException("DisruptorService.create3.threadFactory自定义线程工厂不为空!");
        }
        if (Objects.isNull(bufferSize)) {
            throw new RuntimeException("DisruptorService.create3.bufferSize不为空,2的整数次幂 比如1024,2048,,,,,!");
        }
        if (Objects.isNull(producerType)) {
            throw new RuntimeException("DisruptorService.create3.producerType生产者类型不为空!");
        }
        if (Objects.isNull(waitStrategy)) {
            throw new RuntimeException("DisruptorService.create3.waitStrategy等待策略不为空-- 2的整数次幂 比如1024,2048,,,,!");
        }
        if (Objects.nonNull(disruptorMaps.get(key))) {
            return disruptorMaps.get(key);
        }
        Disruptor<DisruptorEvent> disruptor = null;
        if (producerType.equals(ProducerType.MULTI)) {
            disruptor = new Disruptor<>(eventFactory, bufferSize, threadFactory,
                    ProducerType.MULTI, waitStrategy);
        } else {
            disruptor = new Disruptor<>(eventFactory, bufferSize, threadFactory,
                    ProducerType.SINGLE, waitStrategy);
        }
        Tuple tuple = new Tuple(disruptor, disruptor.getRingBuffer(), new AtomicBoolean(false));
        disruptorMaps.put(key, tuple);
        return tuple;
    }

    /**
     * @param key
     * @return
     */
    public Tuple create4(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.create4.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        ConcurrentHashMap<String, DisruptorCreate> bizNameDcbMaps = DisruptorCreateMethodEnum.CREATE4.getDcbMaps();
        DisruptorCreate dcb = bizNameDcbMaps.get(key);
        if (Objects.isNull(dcb)) {
            throw new RuntimeException("DisruptorService.create4.请先设置DisruptorCreatMethodEnum的createDcbMaps对应关系!");
        }
        int bufferSize = dcb.getBufferSize();
        EventFactory eventFactory = dcb.getEventFactory();
        Executor executor = dcb.getExecutor();
        ProducerType producerType = dcb.getProducerType();
        WaitStrategy waitStrategy = dcb.getWaitStrategy();
        if (Objects.isNull(eventFactory)) {
            throw new RuntimeException("DisruptorService.create4.eventFactory自定义事件工厂不为空!");
        }
        if (Objects.isNull(bufferSize)) {
            throw new RuntimeException("DisruptorService.create4.bufferSize不为空,2的整数次幂 比如1024,2048,,,,,!");
        }
        if (Objects.isNull(executor)) {
            throw new RuntimeException("DisruptorService.create4.executor不为空!");
        }
        if (Objects.isNull(producerType)) {
            throw new RuntimeException("DisruptorService.create4.producerType生产者类型不为空!");
        }
        if (Objects.isNull(waitStrategy)) {
            throw new RuntimeException("DisruptorService.create3.waitStrategy等待策略不为空-- 2的整数次幂 比如1024,2048,,,,!");
        }
        if (Objects.nonNull(disruptorMaps.get(key))) {
            return disruptorMaps.get(key);
        }
        Disruptor<DisruptorEvent> disruptor = null;
        if (producerType.equals(ProducerType.MULTI)) {
            disruptor = new Disruptor<>(eventFactory, bufferSize, executor,
                    ProducerType.MULTI, waitStrategy);
        } else {
            disruptor = new Disruptor<>(eventFactory, bufferSize, executor,
                    ProducerType.SINGLE, waitStrategy);
        }
        Tuple tuple = new Tuple(disruptor, disruptor.getRingBuffer(), new AtomicBoolean(false));
        disruptorMaps.put(key, tuple);
        return tuple;
    }

    /**
     * @param key
     * @param disruptorHandler
     * @return
     */
    public Tuple createAddHandlerStart1(String key, DisruptorHandler disruptorHandler) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.createAddHandlerStart1.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        Tuple tuple = null;
        if (key.indexOf(DisruptorCreateMethodEnum.CREATE0.getName()) == 0) {
            tuple = create0(key);
        } else if (key.indexOf(DisruptorCreateMethodEnum.CREATE1.getName()) == 0) {
            tuple = create1(key);
        } else if (key.indexOf(DisruptorCreateMethodEnum.CREATE2.getName()) == 0) {
            tuple = create2(key);
        } else if (key.indexOf(DisruptorCreateMethodEnum.CREATE3.getName()) == 0) {
            tuple = create3(key);
        } else if (key.indexOf(DisruptorCreateMethodEnum.CREATE4.getName()) == 0) {
            tuple = create4(key);
        }
        if (Objects.nonNull(tuple)) {
            Disruptor disruptor = tuple.get(0);
            AtomicBoolean started = tuple.get(2);
            if (Objects.nonNull(disruptor) && Objects.nonNull(started) && !started.get()) {
                disruptorHandler.buildHandler(key, disruptor);
            }
            start(key);
        }
        return tuple;
    }


    /**
     * 启动disruptor
     *
     * @param key
     */
    public Boolean start(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.start.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        Tuple tuple = disruptorMaps.get(key);
        if (Objects.isNull(tuple)) {
            throw new RuntimeException("DisruptorService.start.根据key未匹配到tuple!");
        }
        AtomicBoolean started = tuple.get(2);
        if (Objects.nonNull(started) && !started.compareAndSet(false, true)) {
            return Boolean.TRUE;
        }
        Disruptor disruptor = tuple.get(0);
        if (Objects.nonNull(disruptor)) {
            disruptor.start();
            disruptorMaps.put(key, new Tuple(disruptor, disruptor.getRingBuffer(), started));
            log.info("DisruptorService.start.disruptor:{}==>成功!", disruptor);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 关闭disruptor
     *
     * @param key
     */
    public void shutdown(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.start.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        Tuple tuple = disruptorMaps.get(key);
        if (Objects.nonNull(tuple) && Objects.nonNull(tuple.get(0))) {
            Disruptor disruptor = tuple.get(0);
            disruptor.shutdown();
            log.info("DisruptorService.shutdown.disruptor:{}==>关闭成功!", disruptor);
            disruptorMaps.entrySet().removeIf(e -> key.equals(e.getKey()));
        }
    }

    /**
     * 一个参数转换发消息方法
     *
     * @param key
     * @param o1
     */
    public void pushEvent0(String key, Object o1) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.pushEvent0.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        if (Objects.isNull(o1)) {
            throw new RuntimeException("pushEvent0.o1不为空!");
        }
        Tuple tuple = disruptorMaps.get(key);
        if (Objects.isNull(tuple)) {
            throw new RuntimeException("DisruptorService.pushEvent0.根据key未匹配到tuple!");
        }
        Disruptor disruptor = tuple.get(0);
        if (Objects.isNull(disruptor)) {
            throw new RuntimeException("pushEvent0.未找到对应key的disruptor,请检查key参数是否正确!");
        }
        RingBuffer ringBuffer = disruptor.getRingBuffer();
        if (Objects.isNull(ringBuffer)) {
            throw new RuntimeException("pushEvent0.ringBuffer不为空!");
        }
        EventTranslatorOneArg<DisruptorEvent, Object> eventTranslatorOneArg = (event, sequence, arg0) -> event.setO1(o1);
        ringBuffer.publishEvent(eventTranslatorOneArg, o1);
    }

    /**
     * 两参数转换发消息方法
     *
     * @param key
     * @param o1
     * @param o2
     */
    public void pushEvent1(String key, Object o1, Object o2) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.pushEvent1.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        if (Objects.isNull(o1)) {
            throw new RuntimeException("pushEvent1.o1不为空!");
        }
        if (Objects.isNull(o2)) {
            throw new RuntimeException("pushEvent1.o2不为空!");
        }
        Tuple tuple = disruptorMaps.get(key);
        if (Objects.isNull(tuple)) {
            throw new RuntimeException("DisruptorService.pushEvent1.根据key未匹配到tuple!");
        }
        Disruptor disruptor = tuple.get(0);
        if (Objects.isNull(disruptor)) {
            throw new RuntimeException("pushEvent1.未找到对应key的disruptor,请检查key参数是否正确!");
        }
        RingBuffer ringBuffer = disruptor.getRingBuffer();
        if (Objects.isNull(ringBuffer)) {
            throw new RuntimeException("pushEvent1.ringBuffer不为空!");
        }
        EventTranslatorTwoArg<DisruptorEvent, Object, Object> eventTranslatorTwoArg = (event, sequence, arg0, arg1) -> {
            event.setO1(o1);
            event.setO2(o2);
        };
        ringBuffer.publishEvent(eventTranslatorTwoArg, o1, o2);
    }

    /**
     * 三个参数转换发消息方法
     *
     * @param key
     * @param o1
     * @param o2
     * @param o3
     */
    public void pushEvent2(String key, Object o1, Object o2, Object o3) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.pushEvent2.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        if (Objects.isNull(o1)) {
            throw new RuntimeException("pushEvent2.o1不为空!");
        }
        if (Objects.isNull(o2)) {
            throw new RuntimeException("pushEvent2.o2不为空!");
        }
        if (Objects.isNull(o3)) {
            throw new RuntimeException("pushEvent2.o3不为空!");
        }
        Tuple tuple = disruptorMaps.get(key);
        if (Objects.isNull(tuple)) {
            throw new RuntimeException("DisruptorService.pushEvent2.根据key未匹配到tuple!");
        }
        Disruptor disruptor = tuple.get(0);
        if (Objects.isNull(disruptor)) {
            throw new RuntimeException("pushEvent2.未找到对应key的disruptor,请检查key参数是否正确!");
        }
        RingBuffer ringBuffer = disruptor.getRingBuffer();
        if (Objects.isNull(ringBuffer)) {
            throw new RuntimeException("pushEvent2.ringBuffer不为空!");
        }
        EventTranslatorThreeArg<DisruptorEvent, Object, Object, Object> eventTranslatorThreeArg = (event, sequence, arg0, arg1, arg2) -> {
            event.setO1(o1);
            event.setO2(o2);
            event.setO3(o3);
        };
        ringBuffer.publishEvent(eventTranslatorThreeArg, o1, o2, o3);
    }

    /**
     * 多个个参数转换发消息方法
     *
     * @param key
     * @param objectList
     */
    public void pushEvent3(String key, List<Object> objectList) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorService.pushEvent3.key不为空!");
        }
        DisruptorCreateMethodEnum.checkKeyFormat(key);
        if (CollectionUtil.isNotEmpty(objectList)) {
            throw new RuntimeException("pushEvent2.objectList不为空!");
        }
        Tuple tuple = disruptorMaps.get(key);
        if (Objects.isNull(tuple)) {
            throw new RuntimeException("DisruptorService.pushEvent3.根据key未匹配到tuple!");
        }
        Disruptor disruptor = tuple.get(0);
        if (Objects.isNull(disruptor)) {
            throw new RuntimeException("pushEvent3.未找到对应key的disruptor,请检查key参数是否正确!");
        }
        RingBuffer ringBuffer = disruptor.getRingBuffer();
        if (Objects.isNull(ringBuffer)) {
            throw new RuntimeException("pushEvent2.ringBuffer不为空!");
        }
        EventTranslatorOneArg<DisruptorEvent, Object> eventTranslatorOneArg = (event, sequence, arg0) -> event.setObjectList(objectList);
        ringBuffer.publishEvent(eventTranslatorOneArg, objectList);
    }

    /**
     * 销毁清理工作
     */
    public void destroy() {
        log.info("DisruptorService.destroy.keyMaps.size:{}", keyMaps.size());
        if (CollectionUtil.isNotEmpty(keyMaps)) {
            for (Map.Entry<String, Tuple> entry : keyMaps.entrySet()) {
                Tuple tuple = entry.getValue();
                if (Objects.isNull(tuple)) {
                    continue;
                }
                shutdown(tuple.get(1));
                log.info("key:{}的disruptor关闭完成!", entry.getKey());
            }
        }
    }

}
