package com.github.mati1979.play.hysterix.stats;

import com.codahale.metrics.Reservoir;
import com.github.mati1979.play.hysterix.HysterixResponseMetadata;

public interface HysterixGlobalStatistics {

    void clearStats();

    String getKey();

    long getErrorCount();

    long getTotalCount();

    long getSuccessWithoutRequestCacheCount();

    long getShortCircuitedCount();

    long getSuccessCount();

    long getFailureCount();

    long getResponsesFromCacheCount();

    long getFallbackSuccessCount();

    long getFallbackFailureCount();

    long getExceptionsThrownCount();

    long getTimeoutCount();

    int getErrorPercentage();

    Reservoir getAverageExecutionTimeReservoir();

    void notify(HysterixResponseMetadata metadata);

}
