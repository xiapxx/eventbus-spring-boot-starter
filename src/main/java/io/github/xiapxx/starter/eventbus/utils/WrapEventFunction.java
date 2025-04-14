package io.github.xiapxx.starter.eventbus.utils;

import java.util.List;

/**
 * @Author xiapeng
 * @Date 2025-04-11 16:04
 */
@FunctionalInterface
public interface WrapEventFunction<INPUT, EVENT> {

    EVENT wrap(List<INPUT> inputs);

}
