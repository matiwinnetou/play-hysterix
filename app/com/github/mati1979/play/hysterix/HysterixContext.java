package com.github.mati1979.play.hysterix;

import com.github.mati1979.play.hysterix.circuit.HysterixCircuitBreakerHolder;
import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatisticsHolder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Created by mszczap on 08.06.14.
 */
public class HysterixContext {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixContext.class);

    private final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder;
    private final HysterixCircuitBreakerHolder hysterixCircuitBreakerHolder;
    private final HysterixSettings hysterixSettings;
    private final EventBus eventBus;

    public HysterixContext(final HysterixCircuitBreakerHolder hysterixCircuitBreakerHolder,
                           final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder,
                           final HysterixSettings hysterixSettings,
                           final EventBus eventBus
                           ) {
        this.hysterixCircuitBreakerHolder = hysterixCircuitBreakerHolder;
        this.hysterixGlobalStatisticsHolder = hysterixGlobalStatisticsHolder;
        this.hysterixSettings = hysterixSettings;
        this.eventBus = eventBus;
    }

    public HysterixGlobalStatisticsHolder getHysterixGlobalStatisticsHolder() {
        return hysterixGlobalStatisticsHolder;
    }

    public HysterixCircuitBreakerHolder getHysterixCircuitBreakerHolder() {
        return hysterixCircuitBreakerHolder;
    }

    public HysterixSettings getHysterixSettings() {
        return hysterixSettings;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public static HysterixContext createDefault() {
        final EventBus eventBus = new EventBus(new EventBusExceptionLogger());
        final HysterixSettings hysterixSettings = new HysterixSettings();
        final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder = new HysterixGlobalStatisticsHolder(hysterixSettings, eventBus);
        final HysterixCircuitBreakerHolder hysterixCircuitBreakerHolder = new HysterixCircuitBreakerHolder(hysterixGlobalStatisticsHolder, hysterixSettings);

        return new HysterixContext(hysterixCircuitBreakerHolder, hysterixGlobalStatisticsHolder, hysterixSettings, eventBus);
    }

    public static HysterixContext create(final HysterixSettings hysterixSettings) {
        final EventBus eventBus = new EventBus(new EventBusExceptionLogger());
        final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder = new HysterixGlobalStatisticsHolder(hysterixSettings, eventBus);
        final HysterixCircuitBreakerHolder hysterixCircuitBreakerHolder = new HysterixCircuitBreakerHolder(hysterixGlobalStatisticsHolder, hysterixSettings);

        return new HysterixContext(hysterixCircuitBreakerHolder, hysterixGlobalStatisticsHolder, hysterixSettings, eventBus);
    }

    private final static class EventBusExceptionLogger implements SubscriberExceptionHandler {

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            logger.error("Hysterix EventBus exception", exception);
        }

    }

}
