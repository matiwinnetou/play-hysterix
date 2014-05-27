package com.github.mati1979.play.hysterix;

public class HysterixContext {

    private HysterixRequestCacheHolder hysterixRequestCacheHolder;
    private HysterixSettings hysterixSettings;
    private HysterixRequestLog hysterixRequestLog;

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
        return new HysterixContext(new HysterixRequestCacheHolder(), hysterixSettings, new HysterixRequestLog());
    }

    public static HysterixContext empty() {
        return new HysterixContext(new HysterixRequestCacheHolder(), new HysterixSettings(), new HysterixRequestLog());
    }

}
