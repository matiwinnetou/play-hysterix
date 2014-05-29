package com.github.mati1979.play.hysterix;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mszczap on 27.05.14.
 */
public class HysterixResponseMetadata {

    protected List<HysterixEventType> executionEvents = Lists.newArrayList();

    private Stopwatch stopwatch = new Stopwatch();

    public HysterixResponseMetadata() {
    }

    public List<HysterixEventType> getExecutionEvents() {
        return Lists.newArrayList(executionEvents);
    }

    protected Stopwatch getStopwatch() {
        return stopwatch;
    }

    protected void markSuccess() {
        executionEvents.add(HysterixEventType.SUCCESS);
    }

    protected void markTimeout() {
        executionEvents.add(HysterixEventType.TIMEOUT);
    }

    protected void markFallbackSuccess() {
        executionEvents.add(HysterixEventType.FALLBACK_SUCCESS);
    }

    protected void markFallbackFailure() {
        executionEvents.add(HysterixEventType.FALLBACK_FAILURE);
    }

    protected void markFailure() {
        executionEvents.add(HysterixEventType.FAILURE);
    }

    protected void markResponseFromCache() {
        executionEvents.add(HysterixEventType.RESPONSE_FROM_CACHE);
    }




    public boolean isExecutionComplete() {
        return executionEvents.size() > 0;
    }

    public boolean isSuccessfulExecution() {
        return executionEvents.contains(HysterixEventType.SUCCESS);
    }

    public boolean isFailedExecution() {
        return executionEvents.contains(HysterixEventType.FAILURE);
    }

    public boolean isFallbackSuccess() {
        return executionEvents.contains(HysterixEventType.FALLBACK_SUCCESS);
    }

    public boolean isFallbackFailed() {
        return executionEvents.contains(HysterixEventType.FALLBACK_SUCCESS);
    }

    public boolean isResponseTimeout() {
        return executionEvents.contains(HysterixEventType.TIMEOUT);
    }

    public boolean isResponseFromCache() {
        return executionEvents.contains(HysterixEventType.RESPONSE_FROM_CACHE);
    }

    public long getExecutionTime(final TimeUnit timeUnit) {
        return stopwatch.elapsed(timeUnit);
    }

}
