package io.github.xiapxx.starter.eventbus.core.simple;

import io.github.xiapxx.starter.eventbus.interfaces.IEventListener;

/**
 * Runnable事件监听器
 *
 * @Author xiapeng
 * @Date 2025-04-14 14:18
 */
public class RunnableEventListener implements IEventListener<Runnable> {

    @Override
    public void onEvent(Runnable event) {
        event.run();
    }

}
