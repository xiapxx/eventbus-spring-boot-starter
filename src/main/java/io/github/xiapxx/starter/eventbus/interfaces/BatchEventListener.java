package io.github.xiapxx.starter.eventbus.interfaces;

import java.util.LinkedList;

/**
 * 批次事件处理监听器
 *
 * @Author xiapeng
 * @Date 2025-04-10 14:50
 */
public interface BatchEventListener<E> extends IEventListener<LinkedList<E>> {

    /**
     * 事件个数达到多少时, 才交给监听器处理
     *
     * @return 事件最大个数
     */
    default int flushSize() {
        return 1000;
    }

    /**
     * 刷新时间(单位: 秒)
     * 当前时间 - 上一次推送时间 >= N秒; 即使没有达到flushSize, 也会交给监听器处理
     *
     * @return 时间
     */
    default int flushSeconds() {
        return 15;
    }

}
