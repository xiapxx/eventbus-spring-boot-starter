package io.github.xiapxx.starter.eventbus.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 并行事件响应
 *
 * @Author xiapeng
 * @Date 2025-04-14 09:17
 */
public class EventParallelResponse {
    private static final Logger log = LoggerFactory.getLogger(EventParallelResponse.class);

    public static EventParallelResponse NO_PARALLEL = new EventParallelResponse(null);

    private CompletableFuture<Void> completableFuture;

    public EventParallelResponse(CompletableFuture<Void> completableFuture){
        this.completableFuture = completableFuture;
    }

    /**
     * 等待是否执行完成
     *
     * @param timeout timeout
     * @param timeUnit timeUnit
     * @return true/false
     */
    public boolean waitComplete(long timeout, TimeUnit timeUnit) {
        if(completableFuture == null){
            return true;
        }
        try {
            if(timeUnit == null || timeout <= 0){
                completableFuture.get();
                return true;
            }
            completableFuture.get(timeout, timeUnit);
            return true;
        } catch (Throwable e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * 等待完成
     *
     * @return true/false
     */
    public boolean waitComplete() {
        return waitComplete(0, null);
    }
}
