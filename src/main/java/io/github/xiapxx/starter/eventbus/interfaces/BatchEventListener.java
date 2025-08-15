package io.github.xiapxx.starter.eventbus.interfaces;

import io.github.xiapxx.starter.eventbus.enums.FlushSecondsTypeEnum;
import java.util.LinkedList;

/**
 * 批次事件处理监听器
 *
 * @see EventBusPublisher#publish
 * @Author xiapeng
 * @Date 2025-04-10 14:50
 */
public interface BatchEventListener<E> extends IEventListener<LinkedList<E>> {

    /**
     * 缓冲区中的事件个数达到多少时, 才交给监听器处理
     *
     * @return 事件最大个数
     */
    default int flushSize() {
        return 1000;
    }

    /**
     * 刷新时间(单位: 秒)
     *
     * @return 时间(单位: 秒)
     */
    default int flushSeconds() {
        return 15;
    }

    /**
     * 根据时间的刷新缓存区方式
     *
     * @return
     *     FlushSecondsTypeEnum.IDLE时,  当前时间 - 缓存区中最后一条事件的推送时间 >= {flushSeconds}秒; 即使没有达到flushSize, 也会交给监听器处理
     *     FlushSecondsTypeEnum.FIXED时, 当前时间 - 缓存区中第一条事件的推送时间 >= {flushSeconds}秒; 即使没有达到flushSize, 也会交给监听器处理
     */
    default FlushSecondsTypeEnum flushSecondsType() {
        return FlushSecondsTypeEnum.FIXED;
    }

}
