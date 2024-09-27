package com.zlf.builder;

import lombok.Builder;
import lombok.Data;

import java.lang.Thread.UncaughtExceptionHandler;

@Data
@Builder
public class CustomThreadBuilder {

    /**
     * 自定义线程池名称
     */
    private String name;

    /**
     * 是否是后台线程
     */
    private Boolean isDaemon = Boolean.FALSE;

    /**
     * 拒绝策略
     */
    private UncaughtExceptionHandler uncaughtExceptionHandler;

}
