package com.github.mati1979.play.hysterix;

public class HysterixContext {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixContext.class);

    private HysterixRequestCacheHolder hysterixRequestCacheHolder;
    private HysterixSettings hysterixSettings;
    private HysterixRequestLog hysterixRequestLog;

    private static HysterixCacheMetricsHolder hysterixCacheMetricsHolder = new HysterixCacheMetricsHolder();

    public HysterixContext(final HysterixRequestCacheHolder hysterixRequestCacheHolder,
                           final HysterixSettings hysterixSettings,
                           final HysterixRequestLog hysterixRequestLog) {
        this.hysterixRequestCacheHolder = hysterixRequestCacheHolder;
        this.hysterixSettings = hysterixSettings;
        this.hysterixRequestLog = hysterixRequestLog;
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

    public static HysterixContext create(final HysterixSettings hysterixSettings) {
        logger.debug("Creating new HysterixContext:" + hysterixSettings);

        final HysterixRequestLog hysterixRequestLog = new HysterixRequestLog(hysterixSettings, hysterixCacheMetricsHolder);

        return new HysterixContext(new HysterixRequestCacheHolder(), hysterixSettings,hysterixRequestLog);
    }

}
