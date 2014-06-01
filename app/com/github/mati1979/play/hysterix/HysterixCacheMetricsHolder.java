package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixCacheMetricsHolder {

    private final Map<String, HysterixCacheMetrics> cacheMetricsMap = Maps.newConcurrentMap();

    public synchronized HysterixCacheMetrics getHysterixCacheMetrics(final HysterixCommand hysterixCommand) {
        final String commandGroupKey = (String) hysterixCommand.getCommandGroupKey().orElse("");

        return getHysterixCacheMetrics(commandGroupKey, hysterixCommand.getCommandKey());
    }

    public synchronized HysterixCacheMetrics getHysterixCacheMetrics(final String commandGroupKey, final String commandKey) {
        final String key = String.format("%s.%s", commandGroupKey, commandKey);
        final HysterixCacheMetrics hysterixCacheMetrics = cacheMetricsMap.getOrDefault(key, new HysterixCacheMetrics(key));

        cacheMetricsMap.put(key, hysterixCacheMetrics);

        return hysterixCacheMetrics;
    }

}
