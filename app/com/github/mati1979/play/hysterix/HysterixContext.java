package com.github.mati1979.play.hysterix;

import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatisticsHolder;
import com.google.common.eventbus.EventBus;

public class HysterixContext {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixContext.class);

    private final HysterixRequestCacheHolder hysterixRequestCacheHolder;
    private final HysterixSettings hysterixSettings;
    private final HysterixRequestLog hysterixRequestLog;

    private final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder;

    public HysterixContext(final HysterixRequestCacheHolder hysterixRequestCacheHolder,
                           final HysterixSettings hysterixSettings,
                           final HysterixRequestLog hysterixRequestLog,
                           final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder) {
        this.hysterixRequestCacheHolder = hysterixRequestCacheHolder;
        this.hysterixSettings = hysterixSettings;
        this.hysterixRequestLog = hysterixRequestLog;
        this.hysterixGlobalStatisticsHolder = hysterixGlobalStatisticsHolder;
    }

    public HysterixRequestCacheHolder getHysterixRequestCacheHolder() {
        return hysterixRequestCacheHolder;
    }

    public HysterixSettings getHysterixSettings() {
        return hysterixSettings;
    }

    public HysterixRequestLog getHysterixRequestLog() {
        return hysterixRequestLog;
    }

    public HysterixGlobalStatisticsHolder getHysterixGlobalStatisticsHolder() {
        return hysterixGlobalStatisticsHolder;
    }

    public static HysterixContext create(final HysterixSettings hysterixSettings) {
        logger.debug("Creating new HysterixContext:" + hysterixSettings);

        final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder = new HysterixGlobalStatisticsHolder(HysterixEventBus.EVENT_BUS, hysterixSettings);
        final HysterixRequestLog hysterixRequestLog = new HysterixRequestLog(hysterixSettings, HysterixEventBus.EVENT_BUS);

        return new HysterixContext(new HysterixRequestCacheHolder(), hysterixSettings, hysterixRequestLog, hysterixGlobalStatisticsHolder);
    }

    public static HysterixContext create(final HysterixSettings hysterixSettings, final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder) {
        logger.debug("Creating new HysterixContext:" + hysterixSettings);

        final HysterixRequestLog hysterixRequestLog = new HysterixRequestLog(hysterixSettings, HysterixEventBus.EVENT_BUS);

        return new HysterixContext(new HysterixRequestCacheHolder(), hysterixSettings, hysterixRequestLog, hysterixGlobalStatisticsHolder);
    }

}
