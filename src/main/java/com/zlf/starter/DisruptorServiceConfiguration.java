package com.zlf.starter;


import com.lmax.disruptor.dsl.Disruptor;
import com.zlf.service.DisruptorService;
import com.zlf.service.PrintThreadPoolService;
import com.zlf.service.ThreadPoolExecutorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disruptor服务配置类
 *
 * @author zlf
 * @date 2024/9/20
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Disruptor.class})
public class DisruptorServiceConfiguration {

    @Bean(destroyMethod = "destroy")
    public DisruptorService disruptorService() {
        return new DisruptorService();
    }

    @Bean
    public PrintThreadPoolService printThreadPoolService() {
        return new PrintThreadPoolService();
    }

    @Bean
    public ThreadPoolExecutorService threadPoolExecutorService() {
        return new ThreadPoolExecutorService();
    }

}
