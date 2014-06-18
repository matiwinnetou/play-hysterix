package com.github.mati1979.play.hysterix.stats;

import com.github.mati1979.play.hysterix.HysterixResponseMetadata;
import com.github.mati1979.play.hysterix.HysterixSettings;
import com.yammer.metrics.Histogram;
import com.yammer.metrics.SlidingTimeWindowReservoir;

import java.util.concurrent.TimeUnit;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixGlobalStatistics {

    private final HysterixSettings hysterixSettings;
    private final String key;

    private Histogram rollingCountFailure = createHistogram();
    private Histogram rollingCountResponsesFromCache = createHistogram();
    private Histogram rollingCountFallbackSuccess = createHistogram();
    private Histogram rollingCountFallbackFailure = createHistogram();
    private Histogram rollingCountShortCircuited = createHistogram();
    private Histogram rollingCountExceptionsThrown = createHistogram();
    private Histogram rollingCountSuccess = createHistogram();
    private Histogram rollingCountTimeout = createHistogram();

    private Histogram averageExecutionTime = createHistogram();

    public HysterixGlobalStatistics(final HysterixSettings hysterixSettings, final String key) {
        this.hysterixSettings = hysterixSettings;
        this.key = key;
    }

    public void clearStats() {
        rollingCountFailure = createHistogram();
        rollingCountResponsesFromCache = createHistogram();
        rollingCountFallbackSuccess = createHistogram();
        rollingCountFallbackFailure = createHistogram();
        rollingCountShortCircuited = createHistogram();
        rollingCountExceptionsThrown = createHistogram();
        rollingCountSuccess = createHistogram();
        rollingCountTimeout = createHistogram();
    }

    public String getKey() {
        return key;
    }

    void notify(final HysterixResponseMetadata metadata) {
        if (metadata.isSuccessfulExecution()) {
            rollingCountSuccess.update(1);
        }
        if (metadata.isFailedExecution()) {
            rollingCountFailure.update(1);
        }
        if (metadata.isResponseTimeout()) {
            rollingCountTimeout.update(1);
        }
        if (metadata.isFallbackSuccess()) {
            rollingCountFallbackSuccess.update(1);
        }
        if (metadata.isFallbackFailed()) {
            rollingCountFallbackFailure.update(1);
        }
        if (metadata.isExceptionThrown()) {
            rollingCountExceptionsThrown.update(1);
        }
        if (metadata.isResponseFromCache()) {
            rollingCountResponsesFromCache.update(1);
        }
        if (metadata.isShortCircuited()) {
            rollingCountShortCircuited.update(1);
        }
        averageExecutionTime.update(metadata.getExecutionTime(TimeUnit.MILLISECONDS));
    }

    public long getErrorCount() {
        return getRollingCountFailure() + getRollingTimeoutCount() + getRollingCountExceptionsThrown() + getRollingCountShortCircuited();
    }

    public long getTotalCount() {
        return getRollingSuccessWithoutRequestCache() + getRollingCountFailure() + getRollingTimeoutCount() + getRollingCountExceptionsThrown() + getRollingCountShortCircuited();
    }

    public long getRollingSuccessWithoutRequestCache() {
        return getRollingCountSuccess() - getRollingCountResponsesFromCache();
    }

    public long getRollingCountShortCircuited() {
        return rollingCountShortCircuited.getSnapshot().size();
    }

    public long getRollingCountSuccess() {
        return rollingCountSuccess.getSnapshot().size();
    }

    public long getRollingCountFailure() {
        return rollingCountFailure.getSnapshot().size();
    }

    public long getRollingCountResponsesFromCache() {
        return rollingCountResponsesFromCache.getSnapshot().size();
    }

    public long getRollingCountFallbackSuccess() {
        return rollingCountFallbackSuccess.getSnapshot().size();
    }

    public long getRollingCountFallbackFailure() {
        return rollingCountFallbackFailure.getSnapshot().size();
    }

    public long getRollingCountExceptionsThrown() {
        return rollingCountExceptionsThrown.getSnapshot().size();
    }

    public long getRollingTimeoutCount() {
        return rollingCountTimeout.getSnapshot().size();
    }

    public int getErrorPercentage() {
        int errorPercentage = 0;

        if (getTotalCount() > 0) {
            errorPercentage = (int) ((double) getErrorCount() / getTotalCount() * 100);
        }

        return errorPercentage;
    }

    public long getAverageExecutionTime() {
        return Math.round(averageExecutionTime.getSnapshot().getMean());
    }

    public long getAverageExecutionTimePercentile(final double quantile) {
        return Math.round(averageExecutionTime.getSnapshot().getValue(quantile));
    }

    private Histogram createHistogram() {
        final long rollingTimeWindowIntervalInMs = hysterixSettings.getRollingTimeWindowIntervalInMs();

        return new Histogram(new SlidingTimeWindowReservoir(rollingTimeWindowIntervalInMs, TimeUnit.MILLISECONDS));
    }

    @Override
    public String toString() {
        return "HysterixGlobalStatistics{" +
                "hysterixSettings=" + hysterixSettings +
                ", key='" + key + '\'' +
                ", rollingCountFailure=" + rollingCountFailure +
                ", rollingCountResponsesFromCache=" + rollingCountResponsesFromCache +
                ", rollingCountFallbackSuccess=" + rollingCountFallbackSuccess +
                ", rollingCountFallbackFailure=" + rollingCountFallbackFailure +
                ", rollingCountExceptionsThrown=" + rollingCountExceptionsThrown +
                ", rollingCountSuccess=" + rollingCountSuccess +
                ", rollingCountTimeout=" + rollingCountTimeout +
                ", averageExecutionTime=" + averageExecutionTime +
                '}';
    }

}
