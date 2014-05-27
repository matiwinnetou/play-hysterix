package com.github.mati1979.play.hysterix;

/**
 * Created by mszczap on 27.05.14.
 */
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

}
