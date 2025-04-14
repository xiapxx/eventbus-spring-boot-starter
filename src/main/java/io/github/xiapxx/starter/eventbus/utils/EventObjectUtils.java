package io.github.xiapxx.starter.eventbus.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author xiapeng
 * @Date 2025-04-11 16:18
 */
public class EventObjectUtils {

    /**
     * 将业务实体封装成事件实体
     *
     * @param inputs inputs
     * @param pageSize pageSize
     * @param wrapEventFunction wrapEventFunction
     * @return 封装后的事件实体集合
     */
    public static <INPUT, EVENT> List<EVENT> pageWrap(Collection<INPUT> inputs,
                                                      int pageSize,
                                                      WrapEventFunction<INPUT, EVENT> wrapEventFunction) {
        if(inputs == null || inputs.isEmpty()){
            return null;
        }
        List<EVENT> resultEventList = new LinkedList<>();
        EventPageLinkedList<INPUT> inputPageList = new EventPageLinkedList<>(inputs, pageSize);
        for (List<INPUT> inputList : inputPageList) {
            if(inputList == null || inputList.isEmpty()){
                continue;
            }
            EVENT event = wrapEventFunction.wrap(inputList);
            if(event == null){
                continue;
            }
            resultEventList.add(event);
        }
        return resultEventList;
    }
}
