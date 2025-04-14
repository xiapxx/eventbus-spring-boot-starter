package io.github.xiapxx.starter.eventbus.core;

import io.github.xiapxx.starter.eventbus.interfaces.IEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author xiapeng
 * @Date 2025-04-11 16:46
 */
public class EventRunnable implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(EventRunnable.class);

    IEventListener eventListener;

    Object event;

    public EventRunnable(IEventListener eventListener, Object event) {
        this.eventListener = eventListener;
        this.event = event;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            eventListener.onEvent(event);
            success = true;
        } catch (Throwable e) {
            log.error("", e);
        } finally {
            log.info("{} 执行{}, 耗时: {}ms",
                    eventListener.getClass().getName(),
                    success ? "成功" : "失败",
                    System.currentTimeMillis() - start
            );
        }
    }
}
