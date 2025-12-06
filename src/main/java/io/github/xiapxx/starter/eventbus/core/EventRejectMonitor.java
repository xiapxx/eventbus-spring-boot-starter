package io.github.xiapxx.starter.eventbus.core;

/**
 * 事件溢出监视器
 */
public class EventRejectMonitor {

    private static final Integer REJECT_VALUE = 0;

    private static final ThreadLocal<Integer> REJECT_FLAG = new ThreadLocal<>();

    /**
     * 清除标记
     */
    static void remove() {
        REJECT_FLAG.remove();
    }

    static void set() {
        REJECT_FLAG.set(REJECT_VALUE);
    }

    /**
     * 是否发生了拒绝事件
     *
     * @return true/false
     */
    public static boolean isRejected() {
        return REJECT_FLAG.get() != null;
    }

}
