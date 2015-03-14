package com.github.mati1979.play.hysterix.web;

import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mati1979.play.hysterix.HysterixContext;
import com.github.mati1979.play.hysterix.HysterixSettings;
import com.github.mati1979.play.hysterix.event.HysterixStatisticsEvent;
import com.github.mati1979.play.hysterix.stats.RollingHysterixGlobalStatistics;
import com.google.common.eventbus.Subscribe;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class HysterixController extends Controller {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixController.class);

    private final HysterixContext hysterixContext;
    private List<EventSource> activeEventSources;

    public HysterixController(final HysterixContext hysterixContext) {
        this.hysterixContext = hysterixContext;
        activeEventSources = new CopyOnWriteArrayList();
        hysterixContext.getEventBus().register(new Subscriber());
    }

    public Result index() {
        final EventSource eventSource = new EventSource() {
            @Override
            public void onConnected() {
                final boolean hasNulls = activeEventSources.stream()
                        .filter(eventSource -> eventSource == null)
                        .findAny()
                        .isPresent();

                if (hasNulls) {
                    activeEventSources = new CopyOnWriteArrayList(activeEventSources.stream()
                            .filter(eventS -> eventS != null)
                            .collect(Collectors.toList()));
                }
                if (activeEventSources.size() > 1000) {
                    logger.warn("activeEventSources over 1000, possibly memory leak!");
                }
                activeEventSources.add(this);
                onDisconnected(() -> {
                    activeEventSources.remove(this);
                    logger.debug("client disconnected, activeEventSources.size:" + activeEventSources.size());
                });

                logger.debug("client connected, activeEventSources.size:" + activeEventSources.size());
            }
        };

        return ok(eventSource);
    }

    public Result clearActiveEventSources() {
        activeEventSources.clear();

        return ok(String.valueOf(activeEventSources.size() == 0));
    }

    private class Subscriber {

        @Subscribe
        public void onEvent(final HysterixStatisticsEvent event) {
            final ObjectNode data = Json.newObject();

            final HysterixSettings hysterixSettings = hysterixContext.getHysterixSettings();
            final RollingHysterixGlobalStatistics timeWindowedMetrics = event.getTimeWindowedMetrics();
            final Snapshot timeWindowedSnapshot = timeWindowedMetrics.getAverageExecutionTimeReservoir().getSnapshot();

            data.put("type", "HystrixCommand");
            data.put("name", event.getEvent().getHysterixCommand().getCommandKey());
            data.put("group", (String) event.getEvent().getHysterixCommand().getCommandGroupKey().orElse(""));
            data.put("currentTime", event.getEvent().getCurrentTime());
            data.put("errorPercentage", timeWindowedMetrics.getErrorPercentage());
            data.put("isCircuitBreakerOpen", event.getEvent().getHysterixCommand().getHysterixCircuitBreaker().isOpen());
            data.put("errorCount", timeWindowedMetrics.getErrorCount());
            data.put("requestCount", timeWindowedMetrics.getTotalCount());
            data.put("rollingCountCollapsedRequests", timeWindowedMetrics.getResponsesFromCacheCount());
            data.put("rollingCountExceptionsThrown", timeWindowedMetrics.getExceptionsThrownCount());
            data.put("rollingCountFailure", timeWindowedMetrics.getFailureCount());
            data.put("rollingCountFallbackFailure", timeWindowedMetrics.getFallbackFailureCount());
            data.put("rollingCountFallbackRejection", 0);
            data.put("rollingCountFallbackSuccess", timeWindowedMetrics.getFallbackSuccessCount());
            data.put("rollingCountResponsesFromCache", timeWindowedMetrics.getResponsesFromCacheCount());
            data.put("rollingCountSemaphoreRejected", 0);
            data.put("rollingCountShortCircuited", timeWindowedMetrics.getShortCircuitedCount());
            data.put("rollingCountSuccess", timeWindowedMetrics.getSuccessWithoutRequestCacheCount());
            data.put("rollingCountThreadPoolRejected", 0);
            data.put("rollingCountTimeout", timeWindowedMetrics.getTimeoutCount());
            data.put("currentConcurrentExecutionCount", 0);

            data.put("latencyExecute_mean", getAverageExecutionTime(timeWindowedSnapshot));

            final ObjectNode percentiles = Json.newObject();
            percentiles.put("0", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.0D));
            percentiles.put("25", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.25D));
            percentiles.put("50", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.50D));
            percentiles.put("75", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.75D));
            percentiles.put("90", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.90D));
            percentiles.put("95", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.95D));
            percentiles.put("99", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.99D));
            percentiles.put("99.5", getAverageExecutionTimePercentile(timeWindowedSnapshot, 0.995D));
            percentiles.put("100", getAverageExecutionTimePercentile(timeWindowedSnapshot, 1.0D));

            data.put("latencyExecute", percentiles);

            data.put("latencyTotal_mean", getAverageExecutionTime(timeWindowedSnapshot));
            data.put("latencyTotal", percentiles);

            data.put("propertyValue_circuitBreakerRequestVolumeThreshold", hysterixSettings.getCircuitBreakerRequestVolumeThreshold());
            data.put("propertyValue_circuitBreakerSleepWindowInMilliseconds", hysterixSettings.getCircuitBreakerSleepWindowInMilliseconds());
            data.put("propertyValue_circuitBreakerErrorThresholdPercentage", hysterixSettings.getCircuitBreakerErrorThresholdPercentage());
            data.put("propertyValue_circuitBreakerForceOpen", false);
            data.put("propertyValue_circuitBreakerForceClosed", hysterixSettings.isCircuitBreakerForceClosed());
            data.put("propertyValue_circuitBreakerEnabled", hysterixSettings.isCircuitBreakerEnabled());
            data.put("propertyValue_executionIsolationStrategy", "THREAD");
            data.put("propertyValue_executionIsolationThreadTimeoutInMilliseconds", "2000");
            data.put("propertyValue_executionIsolationThreadInterruptOnTimeout", true);
            data.putNull("propertyValue_executionIsolationThreadPoolKeyOverride");
            data.put("propertyValue_executionIsolationSemaphoreMaxConcurrentRequests", 20);
            data.put("propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests", 20);
            data.put("propertyValue_metricsRollingStatisticalWindowInMilliseconds", hysterixSettings.getRollingTimeWindowIntervalInMs());
            data.put("propertyValue_requestCacheEnabled", hysterixSettings.isRequestCacheEnabled());
            data.put("propertyValue_requestLogEnabled", hysterixSettings.isLogRequestStatistics());
            data.put("reportingHosts", 1);

            activeEventSources.stream().filter(eventSource -> eventSource != null)
                    .forEach(eventSource -> eventSource.send(EventSource.Event.event(data)));
        }
    }

    private long getAverageExecutionTime(final Snapshot snapshot) {
        return Math.round(snapshot.getMean());
    }

    private long getAverageExecutionTimePercentile(final Snapshot snapshot, final double quantile) {
        return Math.round(snapshot.getValue(quantile));
    }

}
