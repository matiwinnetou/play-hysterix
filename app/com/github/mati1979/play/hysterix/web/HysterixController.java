package com.github.mati1979.play.hysterix.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mati1979.play.hysterix.HysterixContext;
import com.github.mati1979.play.hysterix.event.HysterixStatisticsEvent;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

/**
 * Created by mati on 06/06/2014.
 */
public class HysterixController extends Controller {

    private final HysterixContext hysterixContext;
    private List<EventSource> activeEventSources;

    public HysterixController(final HysterixContext hysterixContext) {
        this.hysterixContext = hysterixContext;
        activeEventSources = Lists.newArrayList();
        hysterixContext.getEventBus().register(new Subscriber());
    }

    public Result index() {
        final EventSource eventSource = new EventSource() {
            @Override
            public void onConnected() {
                activeEventSources.add(this);
                onDisconnected(() -> activeEventSources.remove(this));
            }
        };

        System.out.println("acTiveSize:" + activeEventSources.size());

        return ok(eventSource);
    }

    private class Subscriber {

        @Subscribe
        public void onEvent(final HysterixStatisticsEvent event) {
            final ObjectNode data = Json.newObject();

            data.put("type", "HystrixCommand");
            data.put("name", event.getEvent().getHysterixCommand().getCommandKey());
            data.put("group", (String) event.getEvent().getHysterixCommand().getCommandGroupKey().orElse(""));
            data.put("currentTime", event.getEvent().getCurrentTime());
            data.put("errorPercentage", event.getStats().getErrorPercentage());
            data.put("isCircuitBreakerOpen", false);
            data.put("errorCount", event.getStats().getErrorCount());
            data.put("requestCount", event.getStats().getTotalCount());
            data.put("rollingCountCollapsedRequests", 0);
            data.put("rollingCountExceptionsThrown", event.getStats().getRollingCountExceptionsThrown());
            data.put("rollingCountFailure", event.getStats().getRollingCountFailure());
            data.put("rollingCountFallbackFailure", event.getStats().getRollingCountFailure());
            data.put("rollingCountFallbackRejection", 0);
            data.put("rollingCountFallbackSuccess", event.getStats().getRollingCountFallbackSuccess());
            data.put("rollingCountResponsesFromCache", event.getStats().getRollingCountResponsesFromCache());
            data.put("rollingCountSemaphoreRejected", 0);
            data.put("rollingCountShortCircuited", 0);
            data.put("rollingCountSuccess", event.getStats().getRollingCountSuccess());
            data.put("rollingCountThreadPoolRejected", 0);
            data.put("rollingCountTimeout", event.getStats().getRollingTimeoutCount());
            data.put("currentConcurrentExecutionCount", 0);
            data.put("latencyExecute_mean", event.getStats().getAverageExecutionTime());

            final ObjectNode percentiles = Json.newObject();
            percentiles.put("0", 0);
            percentiles.put("25", 0);
            percentiles.put("50", 0);
            percentiles.put("75", 0);
            percentiles.put("90", 0);
            percentiles.put("95", 0);
            percentiles.put("99", 0);
            percentiles.put("99.5", 0);
            percentiles.put("100", 0);

            data.put("latencyExecute", percentiles);

            data.put("latencyTotal_mean", event.getStats().getAverageExecutionTime());
            data.put("latencyTotal", percentiles);

            data.put("propertyValue_circuitBreakerRequestVolumeThreshold", 0);
            data.put("propertyValue_circuitBreakerSleepWindowInMilliseconds", 0);
            data.put("propertyValue_circuitBreakerErrorThresholdPercentage", 0);
            data.put("propertyValue_circuitBreakerForceOpen", false);
            data.put("propertyValue_circuitBreakerForceClosed", false);
            data.put("propertyValue_circuitBreakerEnabled", false);
            data.put("propertyValue_executionIsolationStrategy", "THREAD");
            data.put("propertyValue_executionIsolationThreadTimeoutInMilliseconds", "2000");
            data.put("propertyValue_executionIsolationThreadInterruptOnTimeout", true);
            data.putNull("propertyValue_executionIsolationThreadPoolKeyOverride");
            data.put("propertyValue_executionIsolationSemaphoreMaxConcurrentRequests", 20);
            data.put("propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests", 20);
            data.put("propertyValue_metricsRollingStatisticalWindowInMilliseconds", 10000);
            data.put("propertyValue_requestCacheEnabled", hysterixContext.getHysterixSettings().isRequestCacheEnabled());
            data.put("propertyValue_requestLogEnabled", hysterixContext.getHysterixSettings().isLogRequestStatistics());
            data.put("reportingHosts", 1);
            activeEventSources.stream().forEach(eventSource -> eventSource.send(EventSource.Event.event(data)));
        }

    }

}
