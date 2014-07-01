package com.github.mati1979.play.hysterix.stats;

import com.github.mati1979.play.hysterix.HysterixResponseMetadata;
import com.github.mati1979.play.hysterix.HysterixSettings;
import com.yammer.metrics.Histogram;

import java.util.concurrent.TimeUnit;

/**
 * Created by mati on 28/06/2014.
 */
public abstract class AbstractHysterixGlobalStatistics implements HysterixGlobalStatistics {

    protected final HysterixSettings hysterixSettings;
    protected final String key;

    protected Histogram countFailure;
    protected Histogram countResponsesFromCache;
    protected Histogram countFallbackSuccess;
    protected Histogram countFallbackFailure;
    protected Histogram countShortCircuited;
    protected Histogram countExceptionsThrown;
    protected Histogram countSuccess;
    protected Histogram countTimeout;

    protected Histogram averageExecutionTime;

    protected AbstractHysterixGlobalStatistics(final HysterixSettings hysterixSettings, final String key) {
        this.hysterixSettings = hysterixSettings;
        this.key = key;
        countFailure = createHistogram();
        countResponsesFromCache = createHistogram();
        countFallbackSuccess = createHistogram();
        countFallbackFailure = createHistogram();
        countShortCircuited = createHistogram();
        countExceptionsThrown = createHistogram();
        countSuccess = createHistogram();
        countTimeout = createHistogram();
        averageExecutionTime = createHistogram();
    }

    @Override
    public void clearStats() {
        countFailure = createHistogram();
        countResponsesFromCache = createHistogram();
        countFallbackSuccess = createHistogram();
        countFallbackFailure = createHistogram();
        countShortCircuited = createHistogram();
        countExceptionsThrown = createHistogram();
        countSuccess = createHistogram();
        countTimeout = createHistogram();
        averageExecutionTime = createHistogram();
    }

    @Override
    public String getKey() {
        return key;
    }

    public void notify(final HysterixResponseMetadata metadata) {
        if (metadata.isSuccessfulExecution()) {
            countSuccess.update(1);
        }
        if (metadata.isFailedExecution()) {
            countFailure.update(1);
        }
        if (metadata.isResponseTimeout()) {
            countTimeout.update(1);
        }
        if (metadata.isFallbackSuccess()) {
            countFallbackSuccess.update(1);
        }
        if (metadata.isFallbackFailed()) {
            countFallbackFailure.update(1);
        }
        if (metadata.isExceptionThrown()) {
            countExceptionsThrown.update(1);
        }
        if (metadata.isResponseFromCache()) {
            countResponsesFromCache.update(1);
        }
        if (metadata.isShortCircuited()) {
            countShortCircuited.update(1);
        }
        averageExecutionTime.update(metadata.getExecutionTime(TimeUnit.MILLISECONDS));
    }

    @Override
    public long getErrorCount() {
        return getFailureCount() + getTimeoutCount() + getExceptionsThrownCount() + getShortCircuitedCount();
    }

    @Override
    public long getTotalCount() {
        return getSuccessWithoutRequestCacheCount() + getFailureCount() + getTimeoutCount() + getExceptionsThrownCount() + getShortCircuitedCount();
    }

    @Override
    public long getSuccessWithoutRequestCacheCount() {
        return getSuccessCount() - getResponsesFromCacheCount();
    }

    @Override
    public long getShortCircuitedCount() {
        return countShortCircuited.getSnapshot().size();
    }

    @Override
    public long getSuccessCount() {
        return countSuccess.getSnapshot().size();
    }

    @Override
    public long getFailureCount() {
        return countFailure.getSnapshot().size();
    }

    @Override
    public long getResponsesFromCacheCount() {
        return countResponsesFromCache.getSnapshot().size();
    }

    @Override
    public long getFallbackSuccessCount() {
        return countFallbackSuccess.getSnapshot().size();
    }

    @Override
    public long getFallbackFailureCount() {
        return countFallbackFailure.getSnapshot().size();
    }

    @Override
    public long getExceptionsThrownCount() {
        return countExceptionsThrown.getSnapshot().size();
    }

    @Override
    public long getTimeoutCount() {
        return countTimeout.getSnapshot().size();
    }

    @Override
    public int getErrorPercentage() {
        int errorPercentage = 0;

        if (getTotalCount() > 0) {
            errorPercentage = (int) ((double) getErrorCount() / getTotalCount() * 100);
        }

        return errorPercentage;
    }

    @Override
    public long getAverageExecutionTime() {
        return Math.round(averageExecutionTime.getSnapshot().getMean());
    }

    @Override
    public long getAverageExecutionTimePercentile(final double quantile) {
        return Math.round(averageExecutionTime.getSnapshot().getValue(quantile));
    }

    protected abstract Histogram createHistogram();

    @Override
    public String toString() {
        return "HysterixGlobalStatistics{" +
                "hysterixSettings=" + hysterixSettings +
                ", key='" + key + '\'' +
                ", countFailure=" + countFailure +
                ", countResponsesFromCache=" + countResponsesFromCache +
                ", countFallbackSuccess=" + countFallbackSuccess +
                ", countFallbackFailure=" + countFallbackFailure +
                ", countExceptionsThrown=" + countExceptionsThrown +
                ", countSuccess=" + countSuccess +
                ", countTimeout=" + countTimeout +
                ", averageExecutionTime=" + averageExecutionTime +
                '}';
    }

}
