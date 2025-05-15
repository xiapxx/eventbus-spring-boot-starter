package io.github.xiapxx.starter.eventbus.core;

import io.github.xiapxx.starter.eventbus.entity.EventParallelResult;
import io.github.xiapxx.starter.eventbus.interfaces.EventResultListener;
import io.github.xiapxx.starter.eventbus.interfaces.IEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * @Author xiapeng
 * @Date 2025-04-11 16:46
 */
public class EventRunnable<EVENT> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(EventRunnable.class);

    IEventListener eventListener;

    EVENT event;

    Map<EVENT, EventParallelResult> event2ParallelResultMap;

    public EventRunnable(IEventListener eventListener,
                         EVENT event) {
        this.eventListener = eventListener;
        this.event = event;
    }

    public EventRunnable(EventResultListener eventListener, Map<EVENT, EventParallelResult> event2ParallelResultMap, EVENT event) {
        this.eventListener = eventListener;
        this.event2ParallelResultMap = event2ParallelResultMap;
        this.event = event;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        boolean isEventResultListener = eventListener instanceof EventResultListener;
        boolean success = false;
        try {
            if(isEventResultListener){
                EventResultListener eventResultListener = (EventResultListener) eventListener;
                Object innerResult = eventResultListener.onEventResult(event);
                event2ParallelResultMap.put(event, EventParallelResult.success(innerResult));
            } else {
                eventListener.onEvent(event);
            }
            success = true;
        } catch (Throwable e) {
            log.error("", e);
            if(isEventResultListener){
                event2ParallelResultMap.put(event, EventParallelResult.fail(e));
            }
        } finally {
            log.info("{} 执行{}, 耗时: {}ms",
                    eventListener.getClass().getName(),
                    success ? "成功" : "失败",
                    System.currentTimeMillis() - start
            );
        }
    }
}
