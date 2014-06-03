package com.github.mati1979.play.hysterix;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixGlobalStatistics {

    private final String key;

    private AtomicLong rollingCountFailure = new AtomicLong();
    private AtomicLong rollingCountResponsesFromCache = new AtomicLong();
    private AtomicLong rollingCountFallbackSuccess = new AtomicLong();
    private AtomicLong rollingCountFallbackFailure = new AtomicLong();
    private AtomicLong rollingCountExceptionsThrown = new AtomicLong();
    private AtomicLong rollingCountSuccess = new AtomicLong();
    private AtomicLong rollingCountTimeout = new AtomicLong();

    private AtomicLong averageExecutionTime = new AtomicLong();
    private AtomicLong averageExecutionCount = new AtomicLong();

    public HysterixGlobalStatistics(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    void notifyHysterixCommand(final HysterixResponseMetadata metadata) {
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

        final long avg = computeAverage(averageExecutionTime.get(),
                metadata.getExecutionTime(TimeUnit.MILLISECONDS),
                averageExecutionCount.incrementAndGet());

        averageExecutionTime.set(avg);
    }

    private long computeAverage(final long oldAvg, final long newTime, final long n) {
        if (n <= 1 || oldAvg <= 0) {
            return newTime;
        }

        long avg = oldAvg - (oldAvg / n);
        avg += newTime / oldAvg;

        return avg;
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

    public long getRollingCountResponsesFromCache() {
        return rollingCountResponsesFromCache.get();
    }

    public long getRollingCountFallbackSuccess() {
        return rollingCountFallbackSuccess.get();
    }

    public long getRollingCountFallbackFailure() {
        return rollingCountFallbackFailure.get();
    }

    public long getRollingCountExceptionsThrown() {
        return rollingCountExceptionsThrown.get();
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
                "key='" + key + '\'' +
                ", rollingCountFailure=" + rollingCountFailure +
                ", rollingCountResponsesFromCache=" + rollingCountResponsesFromCache +
                ", rollingCountFallbackSuccess=" + rollingCountFallbackSuccess +
                ", rollingCountFallbackFailure=" + rollingCountFallbackFailure +
                ", rollingCountExceptionsThrown=" + rollingCountExceptionsThrown +
                ", rollingCountSuccess=" + rollingCountSuccess +
                ", rollingCountTimeout=" + rollingCountTimeout +
                ", averageExecutionTime=" + averageExecutionTime +
                ", averageExecutionCount=" + averageExecutionCount +
                '}';
    }

}