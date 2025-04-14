package io.github.xiapxx.starter.eventbus.properties;

import org.springframework.beans.factory.InitializingBean;

/**
 * @Author xiapeng
 * @Date 2025-04-09 09:35
 */
public class EventBusProperties implements InitializingBean {

    /**
     * 线程个数
     * 0或null:      线程数=cpu核心数
     * 等于-1:       线程数=(cpu核心数/2 + 1)
     * 大于0:        线程数=配置个数
     * 小于-1:       线程数=1
     */
    private Integer threads = -1;

    /**
     * 阻塞队列大小
     */
    private Integer queueSize = 5000;

    /**
     * 调度线程的调度间隔(单位: 秒)
     * 使用场景:
     *    存在IEventListener的事件监听器(rejectedPolicy=RejectedPolicyEnum.SCHEDULE_RUNS), 且达到处理上线时, 使用调度线程处理事件
     */
    private Integer interval = 60;

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        if(threads == null || threads.intValue() == 0){
            threads = cpuCores;
            return;
        }

        if(threads > 0){
            return;
        }

        if(threads.intValue() == -1){
            threads = (cpuCores / 2) + 1;
            return;
        }

        if(threads.intValue() < -1){
            threads = 1;
        }
    }
}
