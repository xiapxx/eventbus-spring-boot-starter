package io.github.xiapxx.starter.eventbus;

import io.github.xiapxx.starter.eventbus.core.EventBusPublisherImpl;
import io.github.xiapxx.starter.eventbus.interfaces.EventBusPublisher;
import io.github.xiapxx.starter.eventbus.properties.EventBusProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 事件总线自动配置
 *
 * @Author xiapeng
 * @Date 2025-04-09 09:31
 */
public class EventBusAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "event.bus")
    public EventBusProperties eventBusProperties() {
        return new EventBusProperties();
    }

    @Bean
    public EventBusPublisher eventBusPublisher(EventBusProperties eventBusProperties) {
        return new EventBusPublisherImpl(eventBusProperties);
    }
}
