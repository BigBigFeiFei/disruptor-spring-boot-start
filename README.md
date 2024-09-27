# disruptor-spring-boot-start

disruptor-spring-boot-start启动器<br>
一. Disruptor简介<br>
1. 简介<br>
Disruptor是一种高性能的并发编程框架，它由LMAX Exchange公司开发，并于2011年开源。Disruptor旨在解决传统多线程编程中的性能瓶颈和并发问题，特别适用于需要高度并发处理的场景。
Disruptor采用了一种称为"无锁编程"的机制，通过使用环形缓冲区（Ring Buffer）和事件驱动的方式实现高效的消息传递和处理。它的核心思想是将消息（事件）在生产者和消费者之间进行无锁的、高效的交换，以减少线程间的竞争和上下文切换。
在Disruptor中，所有的事件都被放置在一个环形缓冲区中，生产者将事件写入缓冲区，而消费者则从缓冲区中读取事件进行处理。这种设计避免了线程间的锁竞争，使得多个线程可以同时进行读写操作，从而提高了整体的处理能力。<br>
2. Disruptor官方文档及项目地址<br>
```
gitHub项目地址:
https://github.com/LMAX-Exchange/disruptor
官方文档地址:
https://lmax-exchange.github.io/disruptor/#_read_this_first
user-guide:
https://lmax-exchange.github.io/disruptor/user-guide/index.html
changelog:
https://lmax-exchange.github.io/disruptor/changelog.html
```

3. 原理图<br>
![原理图](https://lmax-exchange.github.io/disruptor/resources/images/user-guide/models.png)<br>

二. disruptor-spring-boot-start启动器使用教程<br>
1. 项目中引入依赖如下：<br>
- 1.1 gitee坐标
```
<dependency>
    <groupId>io.gitee.bigbigfeifei</groupId>
    <artifactId>disruptor-spring-boot-start</artifactId>
    <version>1.0</version>
</dependency>
```
- 1.2 github坐标
```
<dependency>
    <groupId>io.github.bigbigfeifei</groupId>
    <artifactId>disruptor-spring-boot-start</artifactId>
    <version>1.0</version>
</dependency>
```
如果没有依赖到disruptor的依赖,需要在项目中引入以下依赖即可：<br>
```
<dependency>
   <groupId>com.lmax</groupId>
   <artifactId>disruptor</artifactId>
   <version>3.4.4</version>
</dependency>
```
需要注意的是3.4.4是支持JDK8的最有一个3.x的版本<br>
最新的4.0.0及以上版本需要最小JDK11及以上版本(Minimum Java version now 11)
2. 启动类上加入如下注解：<br>
   启动类上加上@EnableZlfDisruptor注解<br>
3. 使用Demo<br>
使用超级之简单,拓展性也很好,几行代码就搞定了,减少代码量<br>
- 3.1. DisruptorEventHandler类<br>
消费者处理类接口实现
```
package org.example.service.impl;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.EventHandler;
import com.zlf.event.DisruptorEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisruptorEventHandler implements EventHandler<DisruptorEvent> {

    @Override
    public void onEvent(DisruptorEvent event, long sequence, boolean endOfBatch) throws Exception {
        log.info("DisruptorEventHandler.event:{}", JSON.toJSONString(event));
        //这里测试用,抛异常会被CustomExceptionHandler处理(CustomExceptionHandler会发送springBoot的CustomExceptionHandlerEvent事件,业务监听处理该事件就可以了)
        throw new RuntimeException(JSON.toJSONString(event));
    }
}

```
建议消费者的handler处理中最好加上try/catch,
否则，若没有异常没有设置CustomExceptionHandler自定义的异常处理的话,
该消费者的handler线程就不会执行了,这里是需要注意的地方,
还有一个需要注意的地方就是，分开使用DisruptorService中的接口自己组合实现,
disruptor创建之后,先要设置好handler相关消费者处理的handlers,且disruptor的start方法只能触发一次,
否则,重复启动disruptor会报错,所以可以直接使用DisruptorService中提供的createAddHandlerStart1方法,
该方法是在设置好了disruptor相关的参数之后, 初始化disruptor之后设置disruptor相关的handler(只需要业务自己实现DisruptorHandler接口:可以自定义消费者的handlers的处理类链路)
且只触发启动一次。<br>
- 3.2. DisruptorBizListener类<br>
消费者异常handler处理有异常发送CustomExceptionHandlerEvent的springBoot事件监听,解耦处理
```
package org.example.service.impl;

import com.alibaba.fastjson.JSON;
import com.zlf.event.CustomExceptionHandlerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DisruptorBizListener {

    @EventListener
    public void disruptorEventListener(CustomExceptionHandlerEvent event) {
        log.info("DisruptorBizListener.disruptorEventListener.event:{}", JSON.toJSONString(event));

    }
}
```
- 3.3. DisruptorHandlerImpl类
```
package org.example.service.impl;

import com.lmax.disruptor.dsl.Disruptor;
import com.zlf.handler.ClearingEventHandler;
import com.zlf.handler.CustomExceptionHandler;
import com.zlf.handler.DisruptorHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisruptorHandlerImpl implements DisruptorHandler {

    @Override
    public void buildHandler(String key, Disruptor disruptor) {
        log.info("===key:{}自定义handler处理链开始=====", key);
        disruptor.handleExceptionsWith(new CustomExceptionHandler());
        disruptor.handleEventsWith(new DisruptorEventHandler())
                .then(new ClearingEventHandler());
        log.info("===key:{}自定义handler处理链结束=====", key);
    }
}
```
- 3.4. DisruptorController类
```
package org.example.controller;

import cn.hutool.core.lang.Tuple;
import com.lmax.disruptor.dsl.ProducerType;
import com.zlf.builder.CustomThreadBuilder;
import com.zlf.builder.ThreadPoolExecutorBuilder;
import com.zlf.builder.WaitStrategyBuilder;
import com.zlf.dto.DisruptorCreate;
import com.zlf.enums.BlockingQueueTypeEnum;
import com.zlf.enums.DisruptorCreateMethodEnum;
import com.zlf.enums.RejectedPolicyTypeEnum;
import com.zlf.enums.WaitStrategyEnum;
import com.zlf.factory.CustomThreadFactory;
import com.zlf.factory.ThreadPoolExecutorFactory;
import com.zlf.service.DisruptorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.service.impl.DisruptorHandlerImpl;
import org.example.service.impl.ThreadPoolService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 一：初始化disruptor姿势
 * https://blog.csdn.net/2401_84048205/article/details/137853949
 *
 * 二：初始化的一些方式：
 * 1. 静态代码块
 *    static {
 *
 *     }
 * 2. 构造方法或构造方法注入
 *     public DisruptorController(){
 *
 *     }
 * 3.@PostConstruct注解
 *     @PostConstruct
 *     public void init() {
 *          //执行构造方法前执行
 *     }*
 * 3. 实现InitializingBean 接口(bean初始化属性设置之后调用afterPropertiesSet)
 * 4. 实现CommandLineRunner或ApplicationRunner接口
 *   在run方法中初始化disruptor也是可以的
 *   https://www.freexyz.cn/dev/47789.html
 * 5. 非静态代码块中
 *     {}
 * 6. 监听springBoot容器启动完成Event
 *
 * 三：静态变量、静态代码块、非静态代码块、构造方法的执行顺序
 * 1. 执行顺序：先父类子类,
 * 2. 静态的东西属于类：类加载的时候只执行一次
 * 3. 非静态代码块、构造方法属于实例,
 * 4. 当类new的时候先执行非静态代码块(非静态代码块按从上到下顺序执行,跟书写位置没有关系,可以写类的任意地方)
 * 6. 后执行构造方法
 * 类的实例可以调用类的静态方法、不推荐这种方式
 */
@Slf4j
@RestController
@RequestMapping("disruptor")
public class DisruptorController implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private DisruptorService disruptorService;

    private DisruptorHandlerImpl disruptorHandler = new DisruptorHandlerImpl();

    private static final ThreadPoolExecutor executor1 = ThreadPoolService.getInstance();

    private static final ThreadFactory threadFactory = new CustomThreadFactory(CustomThreadBuilder.builder()
            .name("test888-thread-factory").isDaemon(Boolean.TRUE).build());

    private static final ThreadPoolExecutor executor2 = new ThreadPoolExecutorFactory(
            ThreadPoolExecutorBuilder.builder()
                    .blockingQueueTypeEnum(BlockingQueueTypeEnum.ARRAY_BLOCKING_QUEUE)
                    .defaultCoreSize(100)
                    .keepAliveTime(60)
                    .unit(TimeUnit.SECONDS)
                    .maxQueueSize(800)
                    .rejectedPolicyTypeEnum(RejectedPolicyTypeEnum.SYNC_PUT_QUEUE_POLICY)
                    .threadFactory(new CustomThreadFactory(CustomThreadBuilder.builder()
                            .name("test666-thread-factory").isDaemon(Boolean.TRUE)
                            .build()))
                    .build()).createThreadPoolExecutor();

    private static String KEY0;

    private static String KEY1;

    private static String KEY2;

    private static String KEY3;

    private static String KEY4;

    private static String KEY5;

    private static String KEY6;

    private static String KEY7;

    /**
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //方法pushEvent0的disruptor初始化启动方法
        disruptorInitStart0();

        //方法pushEvent1的disruptor初始化启动方法
        disruptorInitStart1();

        //方法pushEvent2的disruptor初始化启动方法
        disruptorInitStart2();

        //方法pushEvent3的disruptor初始化启动方法
        disruptorInitStart3();

        //方法pushEvent4的disruptor初始化启动方法
        disruptorInitStart4();

        //方法pushEvent5的disruptor初始化启动方法
        disruptorInitStart5();

        //方法pushEvent6的disruptor初始化启动方法
        disruptorInitStart6();

        //方法pushEvent7的disruptor初始化启动方法
        disruptorInitStart7();
    }

    private Tuple disruptorInitStart0() {
        Tuple tuple0 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE0, "test0");
        DisruptorCreateMethodEnum.createDcbMaps(tuple0);
        KEY0 = tuple0.get(1);
        return disruptorService.createAddHandlerStart1(KEY0, disruptorHandler);
    }

    private Tuple disruptorInitStart1() {
        Tuple tuple1 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE1, "test1");
        DisruptorCreateMethodEnum.createDcbMaps(tuple1);
        KEY1 = tuple1.get(1);
        return disruptorService.createAddHandlerStart1(KEY1, disruptorHandler);
    }

    private Tuple disruptorInitStart2() {
        Tuple tuple2 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE2, "test2");
        DisruptorCreateMethodEnum.createDcbMaps(tuple2);
        KEY2 = tuple2.get(1);
        return disruptorService.createAddHandlerStart1(KEY2, disruptorHandler);
    }

    private Tuple disruptorInitStart3() {
        Tuple tuple3 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE3, "test3");
        DisruptorCreateMethodEnum.createDcbMaps(tuple3);
        KEY3 = tuple3.get(1);
        return disruptorService.createAddHandlerStart1(KEY3, disruptorHandler);
    }

    private Tuple disruptorInitStart4() {
        Tuple tuple4 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE4, "test4");
        DisruptorCreateMethodEnum.createDcbMaps(tuple4);
        KEY4 = tuple4.get(1);
        return disruptorService.createAddHandlerStart1(KEY4, disruptorHandler);
    }

    private Tuple disruptorInitStart5() {
        Tuple tuple5 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE4, "test5");
        WaitStrategyBuilder waitStrategyBuilder5 = DisruptorCreateMethodEnum.createWaitStrategyMaps(tuple5);
        waitStrategyBuilder5.setWaitStrategyEnum(WaitStrategyEnum.YIELD);
        DisruptorCreateMethodEnum.createExecutorMaps(tuple5, executor1);
        DisruptorCreate disruptorCreate5 = DisruptorCreateMethodEnum.createDcbMaps(tuple5);
        disruptorCreate5.setProducerType(ProducerType.MULTI);
        KEY5 = tuple5.get(1);
        return disruptorService.createAddHandlerStart1(KEY5, disruptorHandler);
    }

    private Tuple disruptorInitStart6() {
        Tuple tuple6 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE4, "test6");
        WaitStrategyBuilder waitStrategyBuilder6 = DisruptorCreateMethodEnum.createWaitStrategyMaps(tuple6);
        waitStrategyBuilder6.setWaitStrategyEnum(WaitStrategyEnum.YIELD);
        DisruptorCreateMethodEnum.createExecutorMaps(tuple6, executor2);
        DisruptorCreate disruptorCreate6 = DisruptorCreateMethodEnum.createDcbMaps(tuple6);
        disruptorCreate6.setProducerType(ProducerType.MULTI);
        KEY6 = tuple6.get(1);
        return disruptorService.createAddHandlerStart1(KEY6, disruptorHandler);
    }

    private Tuple disruptorInitStart7() {
        Tuple tuple7 = disruptorService.buildKey(DisruptorCreateMethodEnum.CREATE3, "test7");
        WaitStrategyBuilder waitStrategyBuilder7 = DisruptorCreateMethodEnum.createWaitStrategyMaps(tuple7);
        waitStrategyBuilder7.setWaitStrategyEnum(WaitStrategyEnum.YIELD);
        DisruptorCreateMethodEnum.createThreadFactoryMaps(tuple7, threadFactory);
        DisruptorCreate disruptorCreate7 = DisruptorCreateMethodEnum.createDcbMaps(tuple7);
        disruptorCreate7.setProducerType(ProducerType.MULTI);
        KEY7 = tuple7.get(1);
        return disruptorService.createAddHandlerStart1(KEY7, disruptorHandler);
    }

    @GetMapping("/disruptorInitStart")
    public String disruptorInitStart(int index) {
        if (Objects.isNull(index)) {
            return "index不为空!";
        }
        if (0 == index) {
            disruptorInitStart0();
        } else if (1 == index) {
            disruptorInitStart1();
        } else if (2 == index) {
            disruptorInitStart2();
        } else if (3 == index) {
            disruptorInitStart3();
        } else if (4 == index) {
            disruptorInitStart4();
        } else if (5 == index) {
            disruptorInitStart5();
        } else if (6 == index) {
            disruptorInitStart6();
        } else if (7 == index) {
            disruptorInitStart7();
        }
        return "ok";
    }

    @GetMapping("/showdown")
    public String showdown(String key) {
        if (StringUtils.isEmpty(key)) {
            return "key不为空!";
        }
        Tuple tuple = disruptorService.getKeyMaps().get(key);
        if (Objects.nonNull(tuple) && StringUtils.isNotBlank(tuple.get(1))) {
            disruptorService.shutdown(tuple.get(1));
        }
        return "ok";
    }

    /**
     * 关闭方法1
     */
    @GetMapping("/destroy")
    public void destroy() {
        // 在需要关闭容器的时候调用
        SpringApplication.exit(applicationContext);
    }

    /**
     * 关闭方法2
     */
    @GetMapping("/destroy2")
    public void destroy2() {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
        context.close();
    }


    @GetMapping("/pushEvent0")
    public String pushEvent0() {
        disruptorService.pushEvent0(KEY0, "你好,disruptor0");
        return "ok";
    }

    @GetMapping("/pushEvent1")
    public String pushEvent1() {
        disruptorService.pushEvent0(KEY1, "你好,disruptor1");
        return "ok";
    }


    @GetMapping("/pushEvent2")
    public String pushEvent2() {
        disruptorService.pushEvent0(KEY2, "你好,disruptor2");
        return "ok";
    }


    @GetMapping("/pushEvent3")
    public String pushEvent3() {
        disruptorService.pushEvent0(KEY3, "你好,disruptor3");
        return "ok";
    }


    @GetMapping("/pushEvent4")
    public String pushEvent4() {
        disruptorService.pushEvent0(KEY4, "你好,disruptor4");
        return "ok";
    }

    @GetMapping("/pushEvent5")
    public String pushEvent5() {
        disruptorService.pushEvent0(KEY5, "你好,disruptor5");
        return "ok";
    }

    @GetMapping("/pushEvent6")
    public String pushEvent6() {
        disruptorService.pushEvent0(KEY6, "你好,disruptor6");
        return "ok";
    }

    @GetMapping("/pushEvent7")
    public String pushEvent7() {
        disruptorService.pushEvent0(KEY7, "你好,disruptor7");
        return "ok";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```
总结：使用该start启动器可以简化Disruptor的使用，拓展性好，代码量少，
上面的Disruptor初始化的代码可以和pushEvent发送消息的代码写在一个方法里面，
但是建议不写一起，分开可以做到解耦的目的，提高接口性能，
使用Disruptor原生的那些api足够了，其它Disruptor的api使用姿势都不推荐去搞，
专注业务即可，该start可以自定义Disruptor的相关参数，还可以自定义线程池的执行器，
根据自己的需求来自定义即可(建议不要自定义太多，否则造成对象创建泛滥导致内存溢出)，
所以一般自定义一个全局静态的就可以了，参数可以根据业务调整即可，
该start将java原有的线程池和线程池的相关队列等和Disruptor很好的结合到了一起,
可以发挥二者各自的优势，给业务带来很大的性能提升,网上我还发现一个Disruptor的一个start：
```
https://github.com/hiwepy/disruptor-spring-boot-starter
```
我把他的源码拉下来看了一下，发现写的花里胡哨的，太过于炫技了，华而不实，
搞了一条handler的链路，责任链模式的使用，我看他是照抄了spingMvc的那个实现的，其实完全没有必要的，
因为Disruptor的handler的链路实现相当的灵活，所以搞成自定义的就好了，他那个starter里面只可以搞
一个Disruptor，而我写的这个可以根据自己的业务来自定义Disruptor，从使用的角度上然Disruptor的
使用变得非常简单，代码量也降低了，拓展性也很好，事半功倍，所以说在制造轮子的时候，需要多方面的去考虑，
可以从简化的角度、拓展性的角度、适用场景的角度，尽量从多方面多角度等去设计考虑实现，
我写的这个disruptor-spring-boot-start启动器是原创独一无二超级好用的一个disruptor的启动器了,
SeaTunnel里面也用到了Disruptor的，之前网上说Disruptor处理600w订单数据，
Disruptor的设计思想确实是牛逼的。

4. 请关注作者的公众号和CSDN,记得一键三连么么么哒！

|公共号| CSDN                                                                                     |
|---|------------------------------------------------------------------------------------------|
| ![微信公众号](src/main/resources/微信公众号.jpg)| ![CSDN](src/main/resources/CSDN.png) |
<br>

三. 好文分享
```
https://developer.aliyun.com/article/1409939
https://www.cnblogs.com/konghuanxi/p/17324988.html
https://blog.csdn.net/weixin_43996530/article/details/132721172
https://blog.csdn.net/qq_39939541/article/details/131508396
https://www.cnblogs.com/konghuanxi/p/17303118.html
```
关于Disruptor的原理、源码等可以看上面的好文和Disruptor的官方项目地址及文档，关于它是如何解决CPU缓存伪共享问题的？在下面这篇文章中有讲的：

```
https://developer.aliyun.com/article/1409939
```
简单来说：将变量对齐cpu缓存行，在多核cpu执行的时候每一次尽可能多的将变量加载对齐好的缓存行数据，
避免多核加载数据被修改导致缓存行失效而要重新去加载一次数据，
这种对齐缓存行的方式提高了数据加载和使用的效率，
还请大家多去点个star，开源不易，该项目我写了好几天的，优化重构n多次，也测试了n多次，
请尊重一下原创， 且做了下压测，性能还是非常的好， 请一键三连，么么么哒!
