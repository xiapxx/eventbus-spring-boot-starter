package io.github.xiapxx.starter.eventbus.interfaces;

/**
 * 允许有结果的事件监听器
 *
 * @see EventBusPublisher#submitParallel
 * @Author xiapeng
 * @Date 2025-05-15 18:08
 */
public interface EventResultListener<E, R> extends IEventListener<E> {

    default void onEvent(E event) {
        throw new UnsupportedOperationException("不支持的操作");
    }

    R onEventResult(E event);

}
