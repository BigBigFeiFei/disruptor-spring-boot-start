package com.zlf.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;


@Slf4j
@Getter
public enum BlockingQueueTypeEnum {

    /**
     * 需要指定固定容量，队列满时会阻塞提交任务的线程。
     * 适用于对队列大小有严格限制或希望控制任务积压程度的场景。
     * https://blog.csdn.net/weixin_43094085/article/details/137831671
     */
    ARRAY_BLOCKING_QUEUE(1, "ArrayBlockingQueue"),

    /**
     * 默认无界（除非在构造时指定容量），理论上可以无限增长，可能导致内存溢出风险。
     * 具有良好的吞吐量，适用于大多数场景，尤其是当任务提交速率和处理速率相对稳定时。
     */
    LINKED_BLOCKING_QUEUE(2, "LinkedBlockingQueue"),

    /**
     * 优先级队列，元素按照自然排序或自定义比较器的顺序出队。
     * 适用于需要任务按照优先级顺序执行的场景。
     */
    PRIORITY_BLOCKING_QUEUE(3, "PriorityBlockingQueue"),

    /**
     * 延迟元素处理：只有当元素的延迟时间到期时，元素才能被取出。使用 take 方法会阻塞直到有元素到期。
     * 无界队列：DelayQueue 是一个无界队列，这意味着它可以包含任意数量的元素(太多可能内存溢出)。
     * 元素排序：DelayQueue 中的元素按到期时间排序，最先到期的元素最早被取出。
     * 阻塞操作：take 方法会阻塞直到有元素到期，而 poll 方法可以在指定的时间内等待。
     * <p>
     * 原文链接：https://blog.csdn.net/qq_37883866/article/details/139739685
     */
    DELAY_QUEUE(4, "DelayQueue"),

    /**
     * 特殊的无缓冲队列，每个插入操作必须等待另一个线程的对应移除操作，反之亦然。
     * 通常会导致线程池立即创建新线程处理任务，除非已有空闲线程可用。
     * 适用于任务提交和处理速率相匹配且要求低延迟的场景。
     */
    SYNCHRONOUS_QUEUE(5, "SynchronousQueue"),

    /**
     * LinkedTransferQueue 是一个高效阻塞无界链表队列。
     * 和SynchronousQueue.TransferQueue(公平模式)相比，
     * 它是可以统计长度，可以进行查询的；和LinkedBlockingQueue相比，
     * 它拥有更高的性能（使用CAS自旋）；和ConcurrentLinkedQueue相比，它拥有阻塞功能。
     * <p>
     * LinkedTransferQueue是一个由链表结构组成的无界阻塞TransferQueue队列。相对于其他阻塞队列，
     * LinkedTransferQueue多了tryTransfer和transfer方法。可以算是 LinkedBolckingQueue 和
     * SynchronousQueue 的合体。LinkedTransferQueue是一种无界阻塞队列，底层基于单链表实现,其内部节点分
     * <p>
     * https://www.cnblogs.com/yuanjiangnan/p/12790644.html
     * https://www.cnblogs.com/tong-yuan/p/LinkedTransferQueue.html
     * https://blog.csdn.net/qq_51226710/article/details/142083573
     */
    LINKED_TRANSFER_QUEUE(6, "LinkedTransferQueue"),

    /**
     * LinkedBlockingDeque 是一个由链表结构组成的双向阻塞队列，
     * 即可以从队列的两端插入和移除元素。
     * 双向队列因为多了一个操作队列的入口，
     * 在多线程同时入队时，也就减少了一半的竞争。
     * 相比于其他阻塞队列，LinkedBlockingDeque 多了 addFirst、addLast、peekFirst、peekLast 等方法。
     * 以first结尾的方法，表示插入、获取或移除双端队列的第一个元素。
     * 以 last 结尾的方法，表示插入、获取或移除双端队列的最后一个元素。
     * LinkedBlockingDeque 是可选容量的，在初始化时可以设置容量防止其过度膨胀。
     * 如果不设置，默认容量大小为 Integer.MAX_VALUE。
     * <p>
     * LinkedBlockingDeque是双向链表实现的双向并发阻塞队列。该阻塞队列同时支持FIFO和FILO两种操作方式，即可以从队列的头和尾同时操作(插入/删除)；该阻塞队列是支持线程安全。
     * 此外，LinkedBlockingDeque还是可选容量的(防止过度膨胀)，即可以指定队列的容量。如果不指定，默认容量大小等于Integer.MAX_VALUE。
     * <p>
     * https://blog.csdn.net/ChineseSoftware/article/details/123331258
     * https://www.cnblogs.com/loveLands/articles/9777200.html
     */
    LINKED_BLOCKING_DEQUE(7, "LinkedBlockingDeque");

    private final Integer code;

    private final String name;

    BlockingQueueTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static BlockingQueue<Runnable> buildBlockingQueue(String name, int capacity) {
        return buildBq(name, capacity, false);
    }

    public static BlockingQueue<Runnable> buildBq(String name, int capacity, boolean fair) {
        BlockingQueue<Runnable> blockingQueue = null;
        if (Objects.equals(name, ARRAY_BLOCKING_QUEUE.getName())) {
            blockingQueue = new ArrayBlockingQueue<>(capacity);
        } else if (Objects.equals(name, LINKED_BLOCKING_QUEUE.getName())) {
            blockingQueue = new LinkedBlockingQueue<>(capacity);
        } else if (Objects.equals(name, PRIORITY_BLOCKING_QUEUE.getName())) {
            blockingQueue = new PriorityBlockingQueue<>(capacity);
        } else if (Objects.equals(name, DELAY_QUEUE.getName())) {
            blockingQueue = new DelayQueue();
        } else if (Objects.equals(name, SYNCHRONOUS_QUEUE.getName())) {
            blockingQueue = new SynchronousQueue<>(fair);
        } else if (Objects.equals(name, LINKED_TRANSFER_QUEUE.getName())) {
            blockingQueue = new LinkedTransferQueue<>();
        } else if (Objects.equals(name, LINKED_BLOCKING_DEQUE.getName())) {
            blockingQueue = new LinkedBlockingDeque<>(capacity);
        }
        if (blockingQueue != null) {
            return blockingQueue;
        }
        log.error("BlockingQueueTypeEnum.buildBq未匹配到队列name:{}", name);
        throw new RuntimeException("BlockingQueueTypeEnum.buildBq未匹配到队列name:" + name);
    }
}
