package io.github.xiapxx.starter.eventbus.core.batch;

import io.github.xiapxx.starter.eventbus.interfaces.BatchEventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author xiapeng
 * @Date 2025-04-11 17:36
 */
public class BatchEventFactory implements Runnable {

    private Map<BatchEventListener, BatchEvent> batchEventListener2BatchEventMap;

    private ScheduledThreadPoolExecutor scheduler;

    public BatchEventFactory(ThreadPoolExecutor threadPoolExecutor,
                             List<BatchEventListener> batchEventListenerList){
        this.batchEventListener2BatchEventMap = batchEventListenerList
                .stream().collect(Collectors.toMap(item -> item, item -> new BatchEvent(threadPoolExecutor, item)));
        this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        this.scheduler.scheduleWithFixedDelay(this, 10, 5, TimeUnit.SECONDS);
    }

    public <EVENT> void add(EVENT event, BatchEventListener<EVENT> batchEventListener){
        BatchEvent batchEvent = batchEventListener2BatchEventMap.get(batchEventListener);
        batchEvent.add(event);
    }

    @Override
    public void run() {
        for (BatchEvent batchEvent : batchEventListener2BatchEventMap.values()) {
            batchEvent.tryFlush();
        }
    }

    public void close() {
       if(scheduler != null){
           scheduler.shutdown();
       }
    }
}
