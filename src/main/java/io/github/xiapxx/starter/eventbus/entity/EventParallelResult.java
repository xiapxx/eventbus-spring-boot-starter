package io.github.xiapxx.starter.eventbus.entity;

/**
 * 并行执行的结果
 *
 * @Author xiapeng
 * @Date 2025-05-15 17:57
 */
public class EventParallelResult<RESULT> {

    private Throwable error;

    private RESULT result;


    public static <RESULT> EventParallelResult<RESULT> success(RESULT result){
        EventParallelResult eventParallelResult = new EventParallelResult();
        eventParallelResult.result = result;
        return eventParallelResult;
    }

    public static EventParallelResult fail(Throwable error) {
        EventParallelResult eventParallelResult = new EventParallelResult();
        eventParallelResult.error = error;
        return eventParallelResult;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public RESULT getResult() {
        return result;
    }

    public void setResult(RESULT result) {
        this.result = result;
    }
}
