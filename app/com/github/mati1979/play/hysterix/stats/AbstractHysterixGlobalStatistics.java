package com.github.mati1979.play.hysterix.stats;

import com.codahale.metrics.Reservoir;
import com.github.mati1979.play.hysterix.HysterixResponseMetadata;
import com.github.mati1979.play.hysterix.HysterixSettings;

import java.util.concurrent.TimeUnit;

public abstract class AbstractHysterixGlobalStatistics implements HysterixGlobalStatistics {

    protected final HysterixSettings hysterixSettings;
    protected final String key;

    protected Reservoir countFailure;
    protected Reservoir countResponsesFromCache;
    protected Reservoir countFallbackSuccess;
    protected Reservoir countFallbackFailure;
    protected Reservoir countShortCircuited;
    protected Reservoir countExceptionsThrown;
    protected Reservoir countSuccess;
    protected Reservoir countTimeout;

    protected Reservoir averageExecutionTime;

    protected AbstractHysterixGlobalStatistics(final HysterixSettings hysterixSettings, final String key) {
        this.hysterixSettings = hysterixSettings;
        this.key = key;
        countFailure = createReservoir();
        countResponsesFromCache = createReservoir();
        countFallbackSuccess = createReservoir();
        countFallbackFailure = createReservoir();
        countShortCircuited = createReservoir();
        countExceptionsThrown = createReservoir();
        countSuccess = createReservoir();
        countTimeout = createReservoir();
        averageExecutionTime = createReservoir();
    }

    @Override
    public synchronized void clearStats() {
        countFailure = createReservoir();
        countResponsesFromCache = createReservoir();
        countFallbackSuccess = createReservoir();
        countFallbackFailure = createReservoir();
        countShortCircuited = createReservoir();
        countExceptionsThrown = createReservoir();
        countSuccess = createReservoir();
        countTimeout = createReservoir();
        averageExecutionTime = createReservoir();
    }

    @Override
    public String getKey() {
        return key;
    }

    public synchronized void notify(final HysterixResponseMetadata metadata) {
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
    public synchronized long getErrorCount() {
        return getFailureCount() + getTimeoutCount() + getExceptionsThrownCount() + getShortCircuitedCount();
    }

    @Override
    public synchronized long getTotalCount() {
        return getSuccessWithoutRequestCacheCount() + getFailureCount() + getTimeoutCount() + getExceptionsThrownCount() + getShortCircuitedCount();
    }

    @Override
    public synchronized long getSuccessWithoutRequestCacheCount() {
        return getSuccessCount() - getResponsesFromCacheCount();
    }

    @Override
    public long getShortCircuitedCount() {
        return countShortCircuited.size();
    }

    @Override
    public long getSuccessCount() {
        return countSuccess.size();
    }

    @Override
    public long getFailureCount() {
        return countFailure.size();
    }

    @Override
    public long getResponsesFromCacheCount() {
        return countResponsesFromCache.size();
    }

    @Override
    public long getFallbackSuccessCount() {
        return countFallbackSuccess.size();
    }

    @Override
    public long getFallbackFailureCount() {
        return countFallbackFailure.size();
    }

    @Override
    public long getExceptionsThrownCount() {
        return countExceptionsThrown.size();
    }

    @Override
    public long getTimeoutCount() {
        return countTimeout.size();
    }

    @Override
    public synchronized int getErrorPercentage() {
        int errorPercentage = 0;

        if (getTotalCount() > 0) {
            errorPercentage = (int) ((double) getErrorCount() / getTotalCount() * 100);
        }

        return errorPercentage;
    }

    public Reservoir getAverageExecutionTimeReservoir() {
        return averageExecutionTime;
    }

    protected abstract Reservoir createReservoir();

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
