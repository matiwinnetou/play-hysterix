package com.github.mati1979.play.hysterix;

public class HysterixContext {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixContext.class);

    private final HysterixRequestCacheHolder hysterixRequestCacheHolder;
    private final HysterixSettings hysterixSettings;
    private final HysterixRequestLog hysterixRequestLog;

    private final HysterixCacheMetricsHolder hysterixCacheMetricsHolder;

    public HysterixContext(final HysterixRequestCacheHolder hysterixRequestCacheHolder,
                           final HysterixSettings hysterixSettings,
                           final HysterixRequestLog hysterixRequestLog,
                           final HysterixCacheMetricsHolder hysterixCacheMetricsHolder) {
        this.hysterixRequestCacheHolder = hysterixRequestCacheHolder;
        this.hysterixSettings = hysterixSettings;
        this.hysterixRequestLog = hysterixRequestLog;
        this.hysterixCacheMetricsHolder = hysterixCacheMetricsHolder;
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

    public HysterixCacheMetricsHolder getHysterixCacheMetricsHolder() {
        return hysterixCacheMetricsHolder;
    }

    public static HysterixContext create(final HysterixSettings hysterixSettings) {
        logger.debug("Creating new HysterixContext:" + hysterixSettings);

        final HysterixCacheMetricsHolder hysterixCacheMetricsHolder = new HysterixCacheMetricsHolder();
        final HysterixRequestLog hysterixRequestLog = new HysterixRequestLog(hysterixSettings, hysterixCacheMetricsHolder);

        return new HysterixContext(new HysterixRequestCacheHolder(), hysterixSettings, hysterixRequestLog, hysterixCacheMetricsHolder);
    }

    public static HysterixContext create(final HysterixSettings hysterixSettings, final HysterixCacheMetricsHolder hysterixCacheMetricsHolder) {
        logger.debug("Creating new HysterixContext:" + hysterixSettings);

        final HysterixRequestLog hysterixRequestLog = new HysterixRequestLog(hysterixSettings, hysterixCacheMetricsHolder);

        return new HysterixContext(new HysterixRequestCacheHolder(), hysterixSettings, hysterixRequestLog, hysterixCacheMetricsHolder);
    }

}
