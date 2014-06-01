package com.github.mati1979.play.hysterix;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixCacheMetrics {

    private final String cacheMetricsKey;

    private AtomicLong rollingCountFailure = new AtomicLong();
    private AtomicLong rollingCountResponsesFromCache = new AtomicLong();
    private AtomicLong rollingCountFallbackSuccess = new AtomicLong();
    private AtomicLong rollingCountFallbackFailure = new AtomicLong();
    private AtomicLong rollingCountFallbackRejection = new AtomicLong();
    private AtomicLong rollingCountExceptionsThrown = new AtomicLong();
    private AtomicLong rollingCountSuccess = new AtomicLong();
    private AtomicLong rollingCountTimeout = new AtomicLong();

    private AtomicLong averageExecutionTime = new AtomicLong();
    private AtomicLong averageExecutionCount = new AtomicLong();

    public HysterixCacheMetrics(String cacheMetricsKey) {
        this.cacheMetricsKey = cacheMetricsKey;
    }

    public String getCacheMetricsKey() {
        return cacheMetricsKey;
    }

    synchronized void notifyHysterixCommand(final HysterixResponseMetadata metadata) {
        if (metadata.isSuccessfulExecution()) {
            rollingCountSuccess.incrementAndGet();
        }
        if (metadata.isFailedExecution()) {
            rollingCountFailure.incrementAndGet();
        }
        if (metadata.isResponseTimeout()) {
            rollingCountTimeout.incrementAndGet();
        }
        if (metadata.isFallbackSuccess()) {
            rollingCountFallbackSuccess.incrementAndGet();
        }
        if (metadata.isFallbackFailed()) {
            rollingCountFallbackFailure.incrementAndGet();
        }
        if (metadata.isExceptionThrown()) {
            rollingCountExceptionsThrown.incrementAndGet();
        }
        if (metadata.isResponseFromCache()) {
            rollingCountResponsesFromCache.incrementAndGet();
        }

        this.averageExecutionCount.incrementAndGet();
        final long executionTime = metadata.getExecutionTime(TimeUnit.MILLISECONDS);
        averageExecutionTime.set(computeAverage(executionTime));

        //System.out.println("cache metrics" + this.toString());
    }

    private long computeAverage(final long newTime) {
        final long oldAvg = averageExecutionTime.get();
        final long n = averageExecutionCount.get();

        if (n == 1) {
            return newTime;
        }

        long avg = oldAvg - (oldAvg / n);
        avg += newTime / averageExecutionTime.get();

        return avg;
    }

    public long getRollingCountFallbackRejection() {
        return rollingCountFallbackRejection.get();
    }

    public long getErrorCount() {
       return getRollingCountFailure() + getRollingTimeoutCount();
    }

    public long getTotalCount() {
        return getRollingCountSuccess() + getRollingCountFailure() + getRollingTimeoutCount();
    }

    public long getRollingCountSuccess() {
        return rollingCountSuccess.get();
    }

    public long getRollingCountFailure() {
        return rollingCountFailure.get();
    }

    public long getRollingTimeoutCount() {
        return rollingCountTimeout.get();
    }

    public int getErrorPercentage() {
        int errorPercentage = 0;

        if (getTotalCount() > 0) {
            errorPercentage = (int) ((double) getErrorCount() / getTotalCount() * 100);
        }

        return errorPercentage;
    }

    public long getAverageExecutionTime() {
        return averageExecutionTime.get();
    }

    @Override
    public String toString() {
        return "HysterixCacheMetrics{" +
                "cacheMetricsKey=" + cacheMetricsKey +
                ", rollingCountFailure=" + rollingCountFailure +
                ", rollingCountResponsesFromCache=" + rollingCountResponsesFromCache +
                ", rollingCountFallbackSuccess=" + rollingCountFallbackSuccess +
                ", rollingCountFallbackFailure=" + rollingCountFallbackFailure +
                ", rollingCountFallbackRejection=" + rollingCountFallbackRejection +
                ", rollingCountExceptionsThrown=" + rollingCountExceptionsThrown +
                ", rollingCountSuccess=" + rollingCountSuccess +
                ", rollingCountTimeout=" + rollingCountTimeout +
                ", averageExecutionTime=" + averageExecutionTime +
                ", averageExecutionCount=" + averageExecutionCount +
                '}';
    }

}
