/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zlf.enums;


import com.zlf.task.rejected.RejectedInvocationHandler;
import com.zlf.task.rejected.policy.RunsOldestTaskPolicy;
import com.zlf.task.rejected.policy.SyncPutQueuePolicy;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum RejectedPolicyTypeEnum {

    /**
     * 由提交任务的线程自己来执行任务，而非放入队列。这会降低主线程处理其他请求的速度，起到一定的流量控制作用。
     */
    CALLER_RUNS_POLICY(1, "CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy()),

    /**
     * AbortPolicy（默认策略）：
     * <p>
     * 直接抛出 RejectedExecutionException，阻止任务的提交。
     */
    ABORT_POLICY(2, "AbortPolicy", new ThreadPoolExecutor.AbortPolicy()),

    /**
     * 默默地丢弃新任务，不进行任何处理。
     */
    DISCARD_POLICY(3, "DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy()),

    /**
     * 移除队列中最早进入还未开始执行的任务，尝试将新任务放入队列。
     */
    DISCARD_OLDEST_POLICY(4, "DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy()),

    /**
     * 执行最早的一个且将拒绝的任务入队
     */
    RUNS_OLDEST_TASK_POLICY(5, "RunsOldestTaskPolicy", new RunsOldestTaskPolicy()),

    /**
     * 拒绝之后将任务put到队列中
     */
    SYNC_PUT_QUEUE_POLICY(6, "SyncPutQueuePolicy", new SyncPutQueuePolicy());

    private Integer type;

    private String name;

    private RejectedExecutionHandler rejectedHandler;

    public static RejectedExecutionHandler getRejectedPolicyTypeEnumByName(String name) {
        ServiceLoader<RejectedExecutionHandler> serviceLoader = ServiceLoader.load(RejectedExecutionHandler.class);
        for (RejectedExecutionHandler handler : serviceLoader) {
            String handlerName = handler.getClass().getSimpleName();
            if (name.equalsIgnoreCase(handlerName)) {
                return handler;
            }
        }
        Optional<RejectedPolicyTypeEnum> rejectedTypeEnum = Stream.of(RejectedPolicyTypeEnum.values())
                .filter(each -> each.name.equals(name))
                .findFirst();
        if (rejectedTypeEnum.isPresent()) {
            return rejectedTypeEnum.get().rejectedHandler;
        }
        return CALLER_RUNS_POLICY.rejectedHandler;
    }

    public static RejectedExecutionHandler getProxy(String name) {
        return getProxy(getRejectedPolicyTypeEnumByName(name));
    }

    public static RejectedExecutionHandler getProxy(RejectedExecutionHandler handler) {
        return (RejectedExecutionHandler) Proxy
                .newProxyInstance(handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new RejectedInvocationHandler(handler));
    }

}
