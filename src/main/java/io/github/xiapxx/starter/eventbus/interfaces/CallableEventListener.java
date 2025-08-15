package io.github.xiapxx.starter.eventbus.interfaces;

import java.util.Collection;

/**
 * @see EventBusPublisher#callableParallel
 * @Author xiapeng
 * @Date 2025-08-15 18:07
 */
public interface CallableEventListener<E> extends IEventListener<E> {

    void callback(Collection<E> events);

}
