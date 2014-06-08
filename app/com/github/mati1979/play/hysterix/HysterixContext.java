package com.github.mati1979.play.hysterix;

import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatisticsHolder;
import com.google.common.eventbus.EventBus;

/**
 * Created by mszczap on 08.06.14.
 */
public class HysterixContext {

    private final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder;
    private final HysterixSettings hysterixSettings;
    private final EventBus eventBus;

    public HysterixContext(HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder, HysterixSettings hysterixSettings, final EventBus eventBus) {
        this.hysterixGlobalStatisticsHolder = hysterixGlobalStatisticsHolder;
        this.hysterixSettings = hysterixSettings;
        this.eventBus = eventBus;
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
        final EventBus eventBus = new EventBus();
        final HysterixSettings hysterixSettings = new HysterixSettings();
        final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder = new HysterixGlobalStatisticsHolder(hysterixSettings, eventBus);

        return new HysterixContext(hysterixGlobalStatisticsHolder, hysterixSettings, eventBus);
    }

    public static HysterixContext create(final HysterixSettings hysterixSettings) {
        final EventBus eventBus = new EventBus();
        final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder = new HysterixGlobalStatisticsHolder(hysterixSettings, eventBus);

        return new HysterixContext(hysterixGlobalStatisticsHolder, hysterixSettings, eventBus);
    }

}
