package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixGlobalStatisticsHolder {

    private final Map<String, HysterixGlobalStatistics> cache = Maps.newConcurrentMap();

    public synchronized HysterixGlobalStatistics getHysterixCacheMetrics(final HysterixCommand hysterixCommand) {
        final String commandGroupKey = (String) hysterixCommand.getCommandGroupKey().orElse("");

        return getHysterixCacheMetrics(commandGroupKey, hysterixCommand.getCommandKey());
    }

    public synchronized HysterixGlobalStatistics getHysterixCacheMetrics(final String commandGroupKey, final String commandKey) {
        final String key = String.format("%s.%s", commandGroupKey, commandKey);
        final HysterixGlobalStatistics hysterixGlobalStatistics = cache.getOrDefault(key, new HysterixGlobalStatistics(key));

        cache.put(key, hysterixGlobalStatistics);

        return hysterixGlobalStatistics;
    }

    public Collection<HysterixGlobalStatistics> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

}
