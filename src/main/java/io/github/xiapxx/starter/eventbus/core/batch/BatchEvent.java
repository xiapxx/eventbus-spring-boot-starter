package io.github.xiapxx.starter.eventbus.core.batch;

import io.github.xiapxx.starter.eventbus.core.EventRunnable;
import io.github.xiapxx.starter.eventbus.interfaces.BatchEventListener;
import java.util.LinkedList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author xiapeng
 * @Date 2025-04-11 17:26
 */
public class BatchEvent<EVENT> {

    private ThreadPoolExecutor threadPoolExecutor;

    private BatchEventListener<EVENT> batchEventListener;

    private volatile LinkedList<EVENT> eventList;

    private volatile long pushTimeMills;

    BatchEvent(ThreadPoolExecutor threadPoolExecutor, BatchEventListener<EVENT> batchEventListener){
        this.threadPoolExecutor = threadPoolExecutor;
        this.batchEventListener = batchEventListener;
        this.eventList = new LinkedList<>();
        this.pushTimeMills = System.currentTimeMillis();
    }

    void add(EVENT event) {
        synchronized (this) {
            eventList.add(event);
            setPushTimeMills();
            if(eventList.size() >= batchEventListener.flushSize()){
                doFlush();
            }
        }
    }

    /**
     * 设置推送时间
     */
    void setPushTimeMills() {
        switch (batchEventListener.flushSecondsType()) {
            case IDLE:
                pushTimeMills = System.currentTimeMillis();
                break;
            case FIXED:
                if (eventList.size() == 1) {
                    pushTimeMills = System.currentTimeMillis();
                }
                break;
        }
    }

    private void doFlush() {
        LinkedList<EVENT> oldEventList = eventList;
        eventList = new LinkedList<>();
        threadPoolExecutor.execute(new EventRunnable(batchEventListener, oldEventList));
    }

    /**
     * 尝试清空缓冲区数据
     */
    void tryFlush() {
        if(!canFlush()){
            return;
        }
        synchronized (this) {
            if(!canFlush()){
                return;
            }
            doFlush();
        }
    }

    /**
     * 判断是否可以刷新数据
     *
     * @return true/false
     */
    private boolean canFlush() {
        if(eventList == null || eventList.isEmpty()){
            return false;
        }
        long idleSeconds = (System.currentTimeMillis() - pushTimeMills) / 1000;
        return idleSeconds >= batchEventListener.flushSeconds();
    }

}
