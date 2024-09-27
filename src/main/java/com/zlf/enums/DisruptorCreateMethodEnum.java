package com.zlf.enums;

import cn.hutool.core.lang.Tuple;
import com.lmax.disruptor.EventFactory;
import com.zlf.builder.WaitStrategyBuilder;
import com.zlf.dto.DisruptorCreate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 属性关系说明：
 * dcbMaps:disruptor构建相关参数对应关系(可自定义或者使用默认的)
 * waitStrategyMaps:等待策略对应关系(可自定义或者使用默认的)
 * eventFactoryMaps:事件工厂对应关系(可自定义或者使用默认的)
 * threadFactoryMaps:线程池工厂对应对应关系(可自定义或者使用默认的)
 * executorMaps:线程池执行器对应关系(可自定义或者使用默认的)
 * <p>
 * 以上属性的key规则: createMethodName + bizName
 * <p>
 * 方法说明：
 * <p>
 * createDcbMaps(Tuple tuple)
 * createDcbMaps该方法最有调用,
 * 先调用下面几个方法自定义waitStrategyBuilder(等待策略)/eventFactory(事件工厂)/threadFactory(线程工厂)/executor(线程池执行器)
 * createWaitStrategyMaps(Tuple tuple)、
 * createEventFactoryMaps(Tuple tuple, EventFactory eventFactory)
 * createThreadFactoryMaps(Tuple tuple, ThreadFactory threadFactory)、
 * createExecutorMaps(Tuple tuple, Executor executor)
 *
 * <p>
 * 以上方法的tuple参数需要使用以下方法构建：
 * 使用DisruptorService.buildKey方法构建一个Tuple(bizName, key, disruptorCreateMethodEnum);
 */
@Getter
@AllArgsConstructor
public enum DisruptorCreateMethodEnum {

    CREATE0("create0", new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>()),

    CREATE1("create1", new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>()),

    CREATE2("create2", new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>()),

    CREATE3("create3", new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>()),

    CREATE4("create4", new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());

    private String name;

    private ConcurrentHashMap<String, DisruptorCreate> dcbMaps;

    private ConcurrentHashMap<String, WaitStrategyBuilder> waitStrategyMaps;

    private ConcurrentHashMap<String, EventFactory> eventFactoryMaps;

    private ConcurrentHashMap<String, ThreadFactory> threadFactoryMaps;

    private ConcurrentHashMap<String, Executor> executorMaps;

    /**
     * 根据方法名称获取enum
     *
     * @param createMethodName
     * @return
     */
    public static DisruptorCreateMethodEnum getByName(String createMethodName) {
        if (StringUtils.isBlank(createMethodName)) {
            throw new RuntimeException("DisruptorCreateMethodEnum.getByName.createMethodName不为空!");
        }
        return Stream.of(DisruptorCreateMethodEnum.values())
                .filter(each -> each.name.equals(createMethodName))
                .findFirst().orElseThrow(() -> new RuntimeException("根据createMethodName未匹配到DisruptorCreatMethodEnum,请检查createMethodName是否正确!"));
    }

    /**
     * 检查key的格式
     *
     * @param key
     */
    public static void checkKeyFormat(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("DisruptorCreateMethodEnum.checkKeyFormat.key不为空!");
        }
        boolean isOk = false;
        List<DisruptorCreateMethodEnum> disruptorCreateMethodEnumList = Stream.of(DisruptorCreateMethodEnum.values()).collect(Collectors.toList());
        for (DisruptorCreateMethodEnum dcm : disruptorCreateMethodEnumList) {
            if (key.indexOf(dcm.getName()) == 0) {
                isOk = true;
                break;
            }
        }
        if (!isOk) {
            throw new RuntimeException("DisruptorCreateMethodEnum.checkKeyFormat.key格式不正确!");
        }
    }

    /**
     * @param tuple
     * @return
     */
    public static DisruptorCreate createDcbMaps(Tuple tuple) {
        checkParams("DisruptorCreateMethodEnum.createDcbMaps", tuple);
        String key = tuple.get(1);
        DisruptorCreateMethodEnum disruptorCreateMethodEnum = tuple.get(2);
        ConcurrentHashMap<String, DisruptorCreate> bizNameDcbMaps = disruptorCreateMethodEnum.getDcbMaps();
        if (Objects.nonNull(bizNameDcbMaps.get(key))) {
            return bizNameDcbMaps.get(key);
        }
        DisruptorCreate disruptorCreate = new DisruptorCreate();
        bizNameDcbMaps.put(key, disruptorCreate);
        ConcurrentHashMap<String, WaitStrategyBuilder> waitStrategyMaps = disruptorCreateMethodEnum.getWaitStrategyMaps();
        WaitStrategyBuilder waitStrategyBuilder = waitStrategyMaps.get(key);
        if (Objects.nonNull(waitStrategyBuilder)) {
            disruptorCreate.setWaitStrategy(waitStrategyBuilder.createWaitStrategy());
        }
        ConcurrentHashMap<String, EventFactory> eventFactoryMaps = disruptorCreateMethodEnum.getEventFactoryMaps();
        EventFactory eventFactory = eventFactoryMaps.get(key);
        if (Objects.nonNull(eventFactory)) {
            disruptorCreate.setEventFactory(eventFactory);
        }
        ConcurrentHashMap<String, ThreadFactory> threadFactoryMaps = disruptorCreateMethodEnum.getThreadFactoryMaps();
        ThreadFactory threadFactory = threadFactoryMaps.get(key);
        if (Objects.nonNull(threadFactory)) {
            disruptorCreate.setThreadFactory(threadFactory);
        }
        ConcurrentHashMap<String, Executor> executorMaps = disruptorCreateMethodEnum.getExecutorMaps();
        Executor executor = executorMaps.get(key);
        if (Objects.nonNull(executor)) {
            disruptorCreate.setExecutor(executor);
        }
        return disruptorCreate;
    }

    /**
     * @param tuple
     * @return
     */
    public static WaitStrategyBuilder createWaitStrategyMaps(Tuple tuple) {
        checkParams("DisruptorCreateMethodEnum.createWaitStrategyMaps", tuple);
        String key = tuple.get(1);
        DisruptorCreateMethodEnum disruptorCreateMethodEnum = tuple.get(2);
        ConcurrentHashMap<String, WaitStrategyBuilder> waitStrategyMaps = disruptorCreateMethodEnum.getWaitStrategyMaps();
        if (Objects.nonNull(waitStrategyMaps.get(key))) {
            return waitStrategyMaps.get(key);
        }
        WaitStrategyBuilder waitStrategyBuilder = WaitStrategyBuilder.builder().build();
        waitStrategyMaps.put(key, waitStrategyBuilder);
        return waitStrategyBuilder;
    }

    /**
     * @param tuple        使用DisruptorService.buildKey方法构建一个Tuple(bizName, key, disruptorCreateMethodEnum);
     * @param eventFactory
     * @return
     */
    public static EventFactory createEventFactoryMaps(Tuple tuple, EventFactory eventFactory) {
        checkParams("DisruptorCreateMethodEnum.createEventFactoryMaps", tuple);
        if (Objects.isNull(eventFactory)) {
            throw new RuntimeException("DisruptorCreateMethodEnum.createEventFactoryMaps.eventFactory不为空!");
        }
        String key = tuple.get(1);
        DisruptorCreateMethodEnum disruptorCreateMethodEnum = tuple.get(2);
        ConcurrentHashMap<String, EventFactory> eventFactoryMaps = disruptorCreateMethodEnum.getEventFactoryMaps();
        if (Objects.nonNull(eventFactoryMaps.get(key))) {
            return eventFactoryMaps.get(key);
        }
        eventFactoryMaps.put(key, eventFactory);
        return eventFactory;
    }

    /**
     * @param tuple
     * @param threadFactory
     * @return
     */
    public static ThreadFactory createThreadFactoryMaps(Tuple tuple, ThreadFactory threadFactory) {
        checkParams("DisruptorCreateMethodEnum.createThreadFactoryMaps", tuple);
        if (Objects.isNull(threadFactory)) {
            throw new RuntimeException("DisruptorCreateMethodEnum.createThreadFactoryMaps.threadFactory不为空!");
        }
        String key = tuple.get(1);
        DisruptorCreateMethodEnum disruptorCreateMethodEnum = tuple.get(2);
        ConcurrentHashMap<String, ThreadFactory> bizNameDcbMaps = disruptorCreateMethodEnum.getThreadFactoryMaps();
        if (Objects.nonNull(bizNameDcbMaps.get(key))) {
            return bizNameDcbMaps.get(key);
        }
        bizNameDcbMaps.put(key, threadFactory);
        return threadFactory;
    }


    /**
     * @param tuple
     * @param executor
     * @return
     */
    public static Executor createExecutorMaps(Tuple tuple, Executor executor) {
        checkParams("DisruptorCreateMethodEnum.createExecutorMaps", tuple);
        if (Objects.isNull(executor)) {
            throw new RuntimeException("DisruptorCreateMethodEnum.createExecutorMaps.executor!");
        }
        String key = tuple.get(1);
        DisruptorCreateMethodEnum disruptorCreateMethodEnum = tuple.get(2);
        ConcurrentHashMap<String, Executor> bizNameDcbMaps = disruptorCreateMethodEnum.getExecutorMaps();
        if (Objects.nonNull(bizNameDcbMaps.get(key))) {
            return bizNameDcbMaps.get(key);
        }
        bizNameDcbMaps.put(key, executor);
        return executor;
    }

    /**
     * 检查参数
     *
     * @param methodName
     * @param tuple
     */
    private static void checkParams(String methodName, Tuple tuple) {
        if (Objects.isNull(tuple)) {
            throw new RuntimeException(methodName + ".bizName不为空！");
        }
        if (Objects.isNull(tuple.get(0))) {
            throw new RuntimeException(methodName + ".tuple第一个参数不为空！");
        }
        if (Objects.isNull(tuple.get(1))) {
            throw new RuntimeException(methodName + ".tuple第二个参数不为空！");
        }
        if (Objects.isNull(tuple.get(2))) {
            throw new RuntimeException(methodName + ".tuple第三个参数不为空！");
        }
    }

}
