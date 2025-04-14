package io.github.xiapxx.starter.eventbus.core;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 对于拒绝策略=RejectedPolicyEnum.SCHEDULE_RUNS的事件,
 * 溢出时交给该类执行
 *
 * @Author xiapeng
 * @Date 2025-04-11 16:55
 */
public class EventScheduler implements Runnable {


    private ScheduledThreadPoolExecutor scheduler;

    private LinkedBlockingQueue<EventRunnable> linkedBlockingQueue;

    public EventScheduler(Integer interval) {
        Integer actualInterval = interval == null ? 60 : interval;
        this.linkedBlockingQueue = new LinkedBlockingQueue<>();
        this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        this.scheduler.scheduleWithFixedDelay(this, actualInterval, actualInterval, TimeUnit.SECONDS);
    }

    /**
     * 添加任务
     *
     * @param eventRunnable eventRunnable
     */
    public void add(EventRunnable eventRunnable) {
        linkedBlockingQueue.add(eventRunnable);
    }

    public void close(){
        this.scheduler.shutdown();
    }

    @Override
    public void run() {
        EventRunnable eventRunnable;
        while ((eventRunnable = linkedBlockingQueue.poll()) != null) {
            eventRunnable.run();
        }
    }
}
