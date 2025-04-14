package io.github.xiapxx.starter.eventbus.interfaces;

import io.github.xiapxx.starter.eventbus.entity.EventParallelResponse;
import io.github.xiapxx.starter.eventbus.utils.WrapEventFunction;
import java.util.Collection;

/**
 * @Author xiapeng
 * @Date 2025-04-09 18:23
 */
public interface EventBusPublisher {

    /**
     * 发布事件
     *
     * @param event event
     */
    void publish(Object event);

    /**
     * 执行Runnable
     *
     * @param runnable runnable
     */
    void execute(Runnable runnable);

    /**
     * 发布并行的事件
     *
     * @param events events
     * @return 结果; 如果不想等待并行结果, 对结果不做任何操作即可; 如果系统等待并行事件完成, 调用waitComplete方法
     */
    <EVENT> EventParallelResponse publishParallel(Collection<EVENT> events);

    /**
     * 执行Runnable的并行事件
     *
     * @param events events
     * @return 结果; 如果不想等待并行结果, 对结果不做任何操作即可; 如果系统等待并行事件完成, 调用waitComplete方法
     */
    EventParallelResponse executeParallel(Collection<Runnable> events);

    /**
     * 发布并行的事件
     *
     * @param inputs 业务对象集合
     * @param pageSize 每个事件对象存储pageSize个业务对象
     * @param wrapEventFunction 将业务对象封装成事件对象的函数
     * @return 结果; 如果不想等待并行结果, 对结果不做任何操作即可; 如果系统等待并行事件完成, 调用waitComplete方法
     */
    <INPUT, EVENT> EventParallelResponse publishParallel(Collection<INPUT> inputs,
                                                         int pageSize,
                                                         WrapEventFunction<INPUT, EVENT> wrapEventFunction);

}
