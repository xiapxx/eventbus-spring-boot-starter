package io.github.xiapxx.starter.eventbus.enums;

/**
 * 拒绝策略枚举
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
