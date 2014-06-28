package com.github.mati1979.play.hysterix.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mati1979.play.hysterix.HysterixContext;
import com.github.mati1979.play.hysterix.event.HysterixStatisticsEvent;
import com.google.common.eventbus.Subscribe;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by mati on 06/06/2014.
 */
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
                final boolean hasNulls = activeEventSources.stream().filter(eventSource -> eventSource == null).findAny().isPresent();
                if (hasNulls) {
                    activeEventSources = new CopyOnWriteArrayList(activeEventSources.stream().filter(eventS -> eventS != null).collect(Collectors.toList()));
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

            data.put("type", "HystrixCommand");
            data.put("name", event.getEvent().getHysterixCommand().getCommandKey());
            data.put("group", (String) event.getEvent().getHysterixCommand().getCommandGroupKey().orElse(""));
            data.put("currentTime", event.getEvent().getCurrentTime());
            data.put("errorPercentage", event.getTimeWindowedMetrics().getErrorPercentage());
            data.put("isCircuitBreakerOpen", event.getEvent().getHysterixCommand().getHysterixCircuitBreaker().isOpen());
            data.put("errorCount", event.getTimeWindowedMetrics().getErrorCount());
            data.put("requestCount", event.getTimeWindowedMetrics().getTotalCount());
            data.put("rollingCountCollapsedRequests", event.getTimeWindowedMetrics().getResponsesFromCacheCount());
            data.put("rollingCountExceptionsThrown", event.getTimeWindowedMetrics().getExceptionsThrownCount());
            data.put("rollingCountFailure", event.getTimeWindowedMetrics().getFailureCount());
            data.put("rollingCountFallbackFailure", event.getTimeWindowedMetrics().getFallbackFailureCount());
            data.put("rollingCountFallbackRejection", 0); //TODO, think over when do we reject fallback?
            data.put("rollingCountFallbackSuccess", event.getTimeWindowedMetrics().geFallbackSuccessCount());
            data.put("rollingCountResponsesFromCache", event.getTimeWindowedMetrics().getResponsesFromCacheCount());
            data.put("rollingCountSemaphoreRejected", 0); //TODO only when semaphore implemented
            data.put("rollingCountShortCircuited", event.getTimeWindowedMetrics().getShortCircuitedCount());
            data.put("rollingCountSuccess", event.getTimeWindowedMetrics().getSuccessWithoutRequestCacheCount());
            data.put("rollingCountThreadPoolRejected", 0);
            data.put("rollingCountTimeout", event.getTimeWindowedMetrics().getTimeoutCount());
            data.put("currentConcurrentExecutionCount", 0); //TODO
            data.put("latencyExecute_mean", event.getTimeWindowedMetrics().getAverageExecutionTime());

            final ObjectNode percentiles = Json.newObject();
            percentiles.put("0", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.0D));
            percentiles.put("25", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.25D));
            percentiles.put("50", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.50D));
            percentiles.put("75", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.75D));
            percentiles.put("90", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.90D));
            percentiles.put("95", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.95D));
            percentiles.put("99", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.99D));
            percentiles.put("99.5", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(0.995D));
            percentiles.put("100", event.getTimeWindowedMetrics().getAverageExecutionTimePercentile(1.0D));

            data.put("latencyExecute", percentiles);

            data.put("latencyTotal_mean", event.getTimeWindowedMetrics().getAverageExecutionTime());
            data.put("latencyTotal", percentiles);

            data.put("propertyValue_circuitBreakerRequestVolumeThreshold", hysterixContext.getHysterixSettings().getCircuitBreakerRequestVolumeThreshold());
            data.put("propertyValue_circuitBreakerSleepWindowInMilliseconds", hysterixContext.getHysterixSettings().getCircuitBreakerSleepWindowInMilliseconds());
            data.put("propertyValue_circuitBreakerErrorThresholdPercentage", hysterixContext.getHysterixSettings().getCircuitBreakerErrorThresholdPercentage());
            data.put("propertyValue_circuitBreakerForceOpen", false);
            data.put("propertyValue_circuitBreakerForceClosed", hysterixContext.getHysterixSettings().isCircuitBreakerForceClosed());
            data.put("propertyValue_circuitBreakerEnabled", hysterixContext.getHysterixSettings().isCircuitBreakerEnabled());
            data.put("propertyValue_executionIsolationStrategy", "THREAD");
            data.put("propertyValue_executionIsolationThreadTimeoutInMilliseconds", "2000");
            data.put("propertyValue_executionIsolationThreadInterruptOnTimeout", true);
            data.putNull("propertyValue_executionIsolationThreadPoolKeyOverride");
            data.put("propertyValue_executionIsolationSemaphoreMaxConcurrentRequests", 20);
            data.put("propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests", 20);
            data.put("propertyValue_metricsRollingStatisticalWindowInMilliseconds", hysterixContext.getHysterixSettings().getRollingTimeWindowIntervalInMs());
            data.put("propertyValue_requestCacheEnabled", hysterixContext.getHysterixSettings().isRequestCacheEnabled());
            data.put("propertyValue_requestLogEnabled", hysterixContext.getHysterixSettings().isLogRequestStatistics());
            data.put("reportingHosts", 1);
            activeEventSources.stream().filter(eventSource -> eventSource != null).forEach(eventSource -> eventSource.send(EventSource.Event.event(data)));
        }

    }

}
