package com.github.mati1979.play.hysterix.stats;

import com.github.mati1979.play.hysterix.HysterixResponseMetadata;

/**
 * Created by mati on 28/06/2014.
 */
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

    long getAverageExecutionTime();

    long getAverageExecutionTimePercentile(double quantile);

    void notify(HysterixResponseMetadata metadata);

}
