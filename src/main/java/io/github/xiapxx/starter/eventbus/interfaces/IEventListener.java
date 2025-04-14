package io.github.xiapxx.starter.eventbus.interfaces;

import io.github.xiapxx.starter.eventbus.enums.RejectedPolicyEnum;

/**
 * 事件监听器
 *
 * @Author xiapeng
 * @Date 2025-04-09 18:22
 */
public interface IEventListener<E> {

    void onEvent(E event);

    /**
     * 当事件个数处理达到上限时的拒绝策略
     *
     * @return 拒绝策略枚举
     */
    default RejectedPolicyEnum rejectedPolicy() {
        return RejectedPolicyEnum.CALLER_RUNS;
    }

}
