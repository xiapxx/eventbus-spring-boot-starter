# eventbus-spring-boot-starter

# 简介
基于线程池实现的异步的事件总线

# 如何使用?
## 引入依赖
~~~~xml
<dependency>
    <groupId>io.github.xiapxx</groupId>
    <artifactId>eventbus-spring-boot-starter</artifactId>
    <version>1.0.5</version>
</dependency>
~~~~
~~~~xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.26</version>
</dependency>
~~~~
~~~~
还需引入slf4j-api日志的实现依赖, 比如logback或log4j
~~~~

## 装配EventBusPublisher

    @Service
    public class XXXService {
    
        @Autowired
        private EventBusPublisher eventBusPublisher;  // 事件发布者; 所有事件都通过此类发布
        
        ...
    }

## 发布单个异步事件
    1. 发布XXXEvent事件
    XXXEvent xxxEvent = new XXXEvent();
    ...
    eventBusPublisher.publish(xxxEvent); // 发布事件
    
    2. 定义XXXEvent的事件监听器
    @Component 
    public class XXXEventListener implements IEventListener<XXXEvent> {
        
        public void onEvent(XXXEvent event) {
            // 处理事件
            ...
        }
    }

## 将单个Runnable直接丢给线程池执行
    eventBusPublisher.execute(() -> {
        // 业务逻辑
    });

## 发布多个并行执行的事件
    1. 发布XXXEvent事件
    List<XXXEvent> eventList = new ArrayList<>();
    ...
    EventParallelResponse response = eventBusPublisher.publishParallel(eventList); // 发布多个并行事件
    response.waitComplete(); // 等待并行事件执行完成(如果不想等待结束, 可不调用waitComplete)

    2. 定义XXXEvent的事件监听器
    @Component
    public class XXXEventListener implements IEventListener<XXXEvent> {
    
            public void onEvent(XXXEvent event) {
                // 处理事件
                ...
            }
    }

## 将多个Runnable直接丢给线程池并行执行
    List<Runnable> eventList = new ArrayList<>();
    ...
    EventParallelResponse response = eventBusPublisher.executeParallel(eventList);
    response.waitComplete(); // 等待并行事件执行完成(如果不想等待结束, 可不调用waitComplete)

## 将业务对象分页封装成事件对象, 然后并行执行
    1. 定义XXXEvent事件对象
    public class XXXEvent {
        private List<BusinessObject> list;
        ...
    }
    
    2. 发布XXXEvent事件
    List<BusinessObject> businessList = new ArrayList<>(); // 假设有100个业务对象
    ...
    // 100个业务对象, 每10个一组封装成XXXEvent对象, 得到10个XXXEvent对象, 然后发布这10个XXXEvent事件对象
    EventParallelResponse response = eventBusPublisher.publishParallel(businessList, 10, itemBusinessList -> {
        XXXEvent xxxEvent = new XXXEvent();
        xxxEvent.setList(itemBusinessList);
        return xxxEvent;
    });
    response.waitComplete(); // 等待并行事件执行完成(如果不想等待结束, 可不调用waitComplete)

    3. 定义XXXEvent事件监听器
    @Component
    public class XXXEventListener implements IEventListener<XXXEvent> {
    
            public void onEvent(XXXEvent event) {
                // 处理事件
                ...
            }
    }

## 发布批量异步事件
单个异步事件和批量异步事件的唯一区别是, 批量事件的监听器使用的是BatchEventListener, 而单个事件监听器使用的是IEventListener

     1. 发布XXXEvent事件
    XXXEvent xxxEvent = new XXXEvent();
    ...
    eventBusPublisher.publish(xxxEvent); // 发布事件
    
    2. 定义XXXEvent的批量事件监听器
    @Component 
    public class XXXEventListener implements BatchEventListener<XXXEvent> {

        // 定义每批次最多处理的事件个数(可选), 默认1000个    
        public int flushSize() {
            return 1000;
        }

        /**
         * 刷新时间(单位: 秒)
         *
         * @return 时间(单位: 秒)
         */
        public int flushSeconds() {
            return 15;
        }
    
        /**
         * 根据时间的刷新缓存区方式
         *
         * @return
         *     FlushSecondsTypeEnum.IDLE时,  当前时间 - 缓存区中最后一条事件的推送时间 >= {flushSeconds}秒; 即使没有达到flushSize, 也会交给监听器处理
         *     FlushSecondsTypeEnum.FIXED时, 当前时间 - 缓存区中第一条事件的推送时间 >= {flushSeconds}秒; 即使没有达到flushSize, 也会交给监听器处理
         */
        public FlushSecondsTypeEnum flushSecondsType() {
            return FlushSecondsTypeEnum.FIXED;
        }
                
        public void onEvent(LinkedList<XXXEvent> events) {
            // 批量处理事件
            ...
        }
    }

# 配置
    event.bus.threads=-1 // 核心线程数;  0或null: 线程数=cpu核心数, 等于-1:  线程数=(cpu核心数/2 + 1), 大于0: 线程数=配置个数, 小于-1: 线程数=1
    event.bus.queueSize=5000 // 阻塞队列大小
    event.bus.interval=60 // 调度线程的调度间隔(单位: 秒); 存在IEventListener的事件监听器(rejectedPolicy=RejectedPolicyEnum.SCHEDULE_RUNS), 且达到处理上限时, 使用调度线程处理事件
    