package io.github.xiapxx.starter.eventbus.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author xiapeng
 * @Date 2025-04-11 16:24
 */
public class EventBusThreadFactory implements ThreadFactory {

    private final ThreadGroup threadGroup;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public EventBusThreadFactory(){
        SecurityManager securityManager = System.getSecurityManager();
        threadGroup = securityManager == null ? Thread.currentThread().getThreadGroup() : securityManager.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup, r,"event-bus-thread-" + threadNumber.getAndIncrement(), 0);
        thread.setDaemon(false);
        return thread;
    }
}
