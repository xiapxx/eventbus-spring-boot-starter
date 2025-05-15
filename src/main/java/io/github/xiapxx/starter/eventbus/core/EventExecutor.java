package io.github.xiapxx.starter.eventbus.core;

import io.github.xiapxx.starter.eventbus.core.batch.BatchEventFactory;
import io.github.xiapxx.starter.eventbus.entity.EventParallelResult;
import io.github.xiapxx.starter.eventbus.enums.RejectedPolicyEnum;
import io.github.xiapxx.starter.eventbus.exceptions.WaitResultException;
import io.github.xiapxx.starter.eventbus.interfaces.BatchEventListener;
import io.github.xiapxx.starter.eventbus.interfaces.EventResultListener;
import io.github.xiapxx.starter.eventbus.interfaces.IEventListener;
import io.github.xiapxx.starter.eventbus.properties.EventBusProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author xiapeng
 * @Date 2025-04-11 17:06
 */
public class EventExecutor implements RejectedExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(EventExecutor.class);

    private EventBusProperties eventBusProperties;

    private ThreadPoolExecutor threadPoolExecutor;

    private EventScheduler eventScheduler;

    private BatchEventFactory batchEventFactory;

    EventExecutor(EventBusProperties eventBusProperties, boolean useEventScheduler,
                         List<BatchEventListener> batchEventListenerList) {
        this.eventBusProperties = eventBusProperties;
        loadThreadPoolExecutor();
        loadEventScheduler(useEventScheduler);
        loadBatchEventFactory(batchEventListenerList);
    }

    /**
     * 加载批次事件工厂
     *
     * @param batchEventListenerList batchEventListenerList
     */
    private void loadBatchEventFactory(List<BatchEventListener> batchEventListenerList) {
        if(batchEventListenerList == null || batchEventListenerList.isEmpty()){
            return;
        }
        this.batchEventFactory = new BatchEventFactory(threadPoolExecutor, batchEventListenerList);
    }

    /**
     * 加载线程池
     */
    private void loadThreadPoolExecutor() {
        this.threadPoolExecutor = new ThreadPoolExecutor(eventBusProperties.getThreads(), eventBusProperties.getThreads(),
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(eventBusProperties.getQueueSize()),
                new EventBusThreadFactory(),
                this
        );
    }

    /**
     * 加载事件调度器
     *
     * @param useEventScheduler useEventScheduler
     */
    private void loadEventScheduler(boolean useEventScheduler) {
        if(!useEventScheduler){
            return;
        }
        this.eventScheduler = new EventScheduler(eventBusProperties.getInterval());
    }

    <EVENT> CompletableFuture<Void> executeParallel(Collection<EVENT> eventColl, IEventListener eventListener) {
        CompletableFuture[] completableFutures = new CompletableFuture[eventColl.size()];
        int index = 0;
        for (EVENT event : eventColl) {
            completableFutures[index] = CompletableFuture.runAsync(new EventRunnable(eventListener, event), threadPoolExecutor);
            index++;
        }
        return CompletableFuture.allOf(completableFutures);
    }

    <EVENT, RESULT> Map<EVENT, EventParallelResult<RESULT>> executeParallelAndWaitResult(Collection<EVENT> eventColl,
                                                                 EventResultListener<EVENT, RESULT> eventListener,
                                                                 long timeout, TimeUnit timeUnit) {
        Map<EVENT, EventParallelResult<RESULT>> event2ParallelResultMap = new ConcurrentHashMap<>(eventColl.size());
        CompletableFuture[] completableFutures = new CompletableFuture[eventColl.size()];
        int index = 0;
        for (EVENT event : eventColl) {
            completableFutures[index] = CompletableFuture.runAsync(new EventRunnable(eventListener, event2ParallelResultMap, event), threadPoolExecutor);
            index++;
        }
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(completableFutures);
        try {
            if(timeUnit == null || timeout <= 0){
                voidCompletableFuture.get();
                return event2ParallelResultMap;
            }
            voidCompletableFuture.get(timeout, timeUnit);
            return event2ParallelResultMap;
        } catch (Throwable e) {
            throw new WaitResultException(e);
        }
    }

    /**
     * 执行事件
     *
     * @param event event
     * @param eventListener eventListener
     */
    void execute(Object event, IEventListener eventListener) {
        if(BatchEventListener.class.isAssignableFrom(eventListener.getClass()) && batchEventFactory != null){
            batchEventFactory.add(event, (BatchEventListener) eventListener);
            return;
        }
        threadPoolExecutor.execute(new EventRunnable(eventListener, event));
    }

    void close(){
        if(this.threadPoolExecutor != null){
            threadPoolExecutor.shutdown();
        }
        if(eventScheduler != null){
            eventScheduler.close();
        }
        if(batchEventFactory != null){
            batchEventFactory.close();
        }
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        EventRunnable eventRunnable = (EventRunnable) r;
        RejectedPolicyEnum rejectedPolicyEnum = eventRunnable.eventListener.rejectedPolicy();

        log.error("事件溢出 : 事件对象 = {}, 当前拒绝策略 = {}",
                eventRunnable.event.getClass().getName(), rejectedPolicyEnum.name());

        switch (rejectedPolicyEnum) {
            case DISCARD:
                return;
            case EXCEPTION:
                throw new RejectedExecutionException("事件溢出: " + eventRunnable.event.getClass().getName());
            case CALLER_RUNS:
                if(!executor.isShutdown()){
                    eventRunnable.run();
                }
                return;
            case SCHEDULE_RUNS:
                if(eventScheduler != null){
                    eventScheduler.add(eventRunnable);
                }
                return;
        }

    }
}
