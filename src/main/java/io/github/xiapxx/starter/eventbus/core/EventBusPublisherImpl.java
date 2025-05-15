package io.github.xiapxx.starter.eventbus.core;

import io.github.xiapxx.starter.eventbus.core.simple.RunnableEventListener;
import io.github.xiapxx.starter.eventbus.entity.EventParallelResponse;
import io.github.xiapxx.starter.eventbus.entity.EventParallelResult;
import io.github.xiapxx.starter.eventbus.enums.RejectedPolicyEnum;
import io.github.xiapxx.starter.eventbus.interfaces.BatchEventListener;
import io.github.xiapxx.starter.eventbus.interfaces.EventBusPublisher;
import io.github.xiapxx.starter.eventbus.interfaces.EventResultListener;
import io.github.xiapxx.starter.eventbus.interfaces.IEventListener;
import io.github.xiapxx.starter.eventbus.properties.EventBusProperties;
import io.github.xiapxx.starter.eventbus.utils.EventObjectUtils;
import io.github.xiapxx.starter.eventbus.utils.WrapEventFunction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author xiapeng
 * @Date 2025-04-10 15:00
 */
public class EventBusPublisherImpl implements EventBusPublisher, SmartInitializingSingleton, DisposableBean, ApplicationContextAware {
    private static final RunnableEventListener RUNNABLE_EVENT_LISTENER = new RunnableEventListener();

    private ApplicationContext applicationContext;

    private EventBusProperties eventBusProperties;

    private Map<Class, IEventListener> eventClass2ListenerMap;

    private EventExecutor eventExecutor;

    public EventBusPublisherImpl(EventBusProperties eventBusProperties) {
        this.eventBusProperties = eventBusProperties;
    }

    @Override
    public void destroy() throws Exception {
        if(eventExecutor != null){
            eventExecutor.close();
        }
    }

    /**
     * 获取事件监听器
     *
     * @param eventClass eventClass
     * @return 事件监听器
     */
    private IEventListener getIEventListener(Class eventClass) {
        IEventListener eventListener = eventClass2ListenerMap.get(eventClass);
        Assert.notNull(eventListener, "未找到有效的事件监听器 : " + eventClass.getName());
        return eventListener;
    }

    @Override
    public void afterSingletonsInstantiated() {
        loadEventClass2ListenerMap();
        loadEventExecutor();
    }

    /**
     * 加载EventExecutor
     */
    private void loadEventExecutor() {
        boolean useEventScheduler = eventClass2ListenerMap == null ? false : eventClass2ListenerMap.values()
                .stream().anyMatch(item -> item.rejectedPolicy() == RejectedPolicyEnum.SCHEDULE_RUNS);
        List<BatchEventListener> batchEventListeners = eventClass2ListenerMap == null ? null : eventClass2ListenerMap.values().stream()
                .filter(item -> item instanceof BatchEventListener)
                .map(item -> (BatchEventListener) item)
                .collect(Collectors.toList());
        this.eventExecutor = new EventExecutor(eventBusProperties, useEventScheduler, batchEventListeners);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 加载eventClass2ListenerMap
     */
    private void loadEventClass2ListenerMap() {
        Map<String, IEventListener> eventBeanName2ListenerMap = applicationContext.getBeansOfType(IEventListener.class);
        if(eventBeanName2ListenerMap == null || eventBeanName2ListenerMap.isEmpty()){
            return;
        }

        Map<Class, List<IEventListener>> eventClass2ListenerListMap = eventBeanName2ListenerMap
                .values()
                .stream().collect(Collectors.groupingBy(eventListener -> {
                    Class supperClass = eventListener instanceof BatchEventListener ? BatchEventListener.class : IEventListener.class;
                    return ResolvableType.forClass(eventListener.getClass()).as(supperClass).getGeneric().resolve();
                }));

        checkMulti(eventClass2ListenerListMap);
        this.eventClass2ListenerMap = eventClass2ListenerListMap.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().get(0)));
    }

    /**
     * 校验, 一个事件对象不允许有多个事件监听器
     *
     * @param eventClass2ListenerListMap eventClass2ListenerListMap
     */
    private void checkMulti(Map<Class, List<IEventListener>> eventClass2ListenerListMap) {
        Class multiEventListenerEventClass = eventClass2ListenerListMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey())
                .findAny().orElse(null);

        if(multiEventListenerEventClass != null){
            throw new IllegalArgumentException("找到多个事件监听器(期望1个) : " + multiEventListenerEventClass.getName());
        }
    }

    /**
     * 发布事件
     *
     * @param event event
     */
    @Override
    public void publish(Object event) {
        if(event == null){
            return;
        }
        IEventListener eventListener = getIEventListener(event.getClass());
        eventExecutor.execute(event, eventListener);
    }

    /**
     * 执行Runnable
     *
     * @param runnable runnable
     */
    @Override
    public void execute(Runnable runnable) {
        if(runnable == null){
            return;
        }
        eventExecutor.execute(runnable, RUNNABLE_EVENT_LISTENER);
    }

    /**
     * 发布并行事件(并且汇总每个事件的结构, 成功与否)
     *
     * @param events events
     * @return 结果; key=原始事件对象 value=结果
     * @throws Throwable 可能抛出超时异常
     */
    @Override
    public <EVENT, RESULT> Map<EVENT, EventParallelResult<RESULT>> publishParallelAndWaitResult(Collection<EVENT> events) throws Throwable {
        return publishParallelAndWaitResult(events, -1, null);
    }

    /**
     * 发布并行事件(并且汇总每个事件的结构, 成功与否)
     *
     * @param events events
     * @param timeout 超时时间
     * @param timeUnit 超时时间单位
     * @return 结果; key=原始事件对象 value=结果
     * @throws Throwable 可能抛出超时异常
     */
    @Override
    public <EVENT, RESULT> Map<EVENT, EventParallelResult<RESULT>> publishParallelAndWaitResult(Collection<EVENT> events,
                                                                                   long timeout, TimeUnit timeUnit)
            throws Throwable {

        if(events == null || events.isEmpty()){
            return null;
        }
        EVENT event = events.stream().findFirst().orElse(null);
        Assert.notNull(event, "不允许有空的事件对象");

        IEventListener eventListener = getIEventListener(event.getClass());
        Assert.isTrue(eventListener instanceof EventResultListener, "事件监听器必须是EventResultListener类型");

        return eventExecutor.executeParallelAndWaitResult(events, (EventResultListener<EVENT, RESULT>) eventListener, timeout, timeUnit);
    }

    /**
     * 发布并行的事件
     *
     * @param events events
     * @return 结果; 如果不想等待并行结果, 对结果不做任何操作即可; 如果系统等待并行事件完成, 调用waitComplete方法
     */
    @Override
    public <EVENT> EventParallelResponse publishParallel(Collection<EVENT> events) {
        if(events == null || events.isEmpty()){
            return EventParallelResponse.NO_PARALLEL;
        }
        EVENT event = events.stream().findFirst().orElse(null);
        Assert.notNull(event, "不允许有空的事件对象");

        Class eventClass = event.getClass();
        return new EventParallelResponse(eventExecutor.executeParallel(events, getIEventListener(eventClass)));
    }

    /**
     * 执行Runnable的并行事件
     *
     * @param events events
     * @return 结果; 如果不想等待并行结果, 对结果不做任何操作即可; 如果系统等待并行事件完成, 调用waitComplete方法
     */
    @Override
    public EventParallelResponse executeParallel(Collection<Runnable> events) {
        if(events == null || events.isEmpty()){
            return EventParallelResponse.NO_PARALLEL;
        }
        return new EventParallelResponse(eventExecutor.executeParallel(events, RUNNABLE_EVENT_LISTENER));
    }

    /**
     * 发布并行的事件
     *
     * @param inputs 业务对象集合
     * @param pageSize 每个事件对象存储pageSize个业务对象
     * @param wrapEventFunction 将业务对象封装成事件对象的函数
     * @return 结果; 如果不想等待并行结果, 对结果不做任何操作即可; 如果系统等待并行事件完成, 调用waitComplete方法
     */
    @Override
    public <INPUT, EVENT> EventParallelResponse publishParallel(Collection<INPUT> inputs,
                                                                int pageSize,
                                                                WrapEventFunction<INPUT, EVENT> wrapEventFunction) {
        List<EVENT> events = EventObjectUtils.pageWrap(inputs, pageSize, wrapEventFunction);
        if(events == null || events.isEmpty()){
            return EventParallelResponse.NO_PARALLEL;
        }

        if(events.size() == 1){
            EVENT event = events.get(0);
            Class eventClass = event.getClass();
            IEventListener eventListener = getIEventListener(eventClass);
            eventListener.onEvent(event);
            return EventParallelResponse.NO_PARALLEL;
        }

        return publishParallel(events);
    }
}
