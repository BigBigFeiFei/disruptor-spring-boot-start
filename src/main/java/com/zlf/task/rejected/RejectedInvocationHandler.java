package com.zlf.task.rejected;

import com.zlf.task.rejected.RejectedAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝粗略执行动态代理handler
 */
public class RejectedInvocationHandler implements InvocationHandler, RejectedAware {

    private final Object target;

    public RejectedInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) args[1];
            beforeReject(executor);

            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
