package com.github.mati1979.play.hysterix;

import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatisticsHolder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.yammer.metrics.MetricRegistry;

/**
 * Created by mszczap on 08.06.14.
 */
public class HysterixContext {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixCommand.class);

    private final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder;
    private final HysterixSettings hysterixSettings;
    private final EventBus eventBus;
    private final MetricRegistry metricRegistry;

    public HysterixContext(final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder,
                           final HysterixSettings hysterixSettings,
                           final EventBus eventBus,
                           final MetricRegistry metricRegistry) {
        this.hysterixGlobalStatisticsHolder = hysterixGlobalStatisticsHolder;
        this.hysterixSettings = hysterixSettings;
        this.eventBus = eventBus;
        this.metricRegistry = metricRegistry;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public HysterixGlobalStatisticsHolder getHysterixGlobalStatisticsHolder() {
        return hysterixGlobalStatisticsHolder;
    }

    public HysterixSettings getHysterixSettings() {
        return hysterixSettings;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static HysterixContext createDefault() {
        final EventBus eventBus = new EventBus(new EventBusExceptionLogger());
        final MetricRegistry registry = new MetricRegistry("hysterix");
        final HysterixSettings hysterixSettings = new HysterixSettings();
        final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder = new HysterixGlobalStatisticsHolder(hysterixSettings, registry, eventBus);

        return new HysterixContext(hysterixGlobalStatisticsHolder, hysterixSettings, eventBus, registry);
    }

    public static HysterixContext create(final HysterixSettings hysterixSettings) {
        final EventBus eventBus = new EventBus(new EventBusExceptionLogger());
        final MetricRegistry registry = new MetricRegistry("hysterix");

        final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder = new HysterixGlobalStatisticsHolder(hysterixSettings, registry, eventBus);

        return new HysterixContext(hysterixGlobalStatisticsHolder, hysterixSettings, eventBus, registry);
    }

    private final static class EventBusExceptionLogger implements SubscriberExceptionHandler {

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            logger.error("Hysterix EventBus exception", exception);
        }

    }

}
