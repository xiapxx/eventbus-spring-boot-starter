package io.github.xiapxx.starter.eventbus.enums;

/**
 * 拒绝策略枚举
 * 1. eventBusPublisher.*Parallel类型事件只支持CALLER_RUNS
 * 2. 可通过在onEvent方法中调用EventRejectMonitor.isRejected()方法判断是否发生了溢出
 *
 * @Author xiapeng
 * @Date 2025-04-10 14:46
 */
public enum RejectedPolicyEnum {
    DISCARD,  // 直接丢弃
    CALLER_RUNS, // 调用线程执行
    EXCEPTION, //抛出异常
    SCHEDULE_RUNS; // 调度线程执行

}
