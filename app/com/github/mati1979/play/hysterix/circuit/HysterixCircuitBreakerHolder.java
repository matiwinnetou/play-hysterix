package com.github.mati1979.play.hysterix.circuit;

import com.github.mati1979.play.hysterix.HysterixCommand;
import com.github.mati1979.play.hysterix.HysterixSettings;
import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatistics;
import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatisticsHolder;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixCircuitBreakerHolder {

    private final Map<String, DefaultHysterixCircuitBreaker> cache = Maps.newConcurrentMap();

    private final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder;
    private final HysterixSettings hysterixSettings;

    public HysterixCircuitBreakerHolder(final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder, final HysterixSettings hysterixSettings) {
        this.hysterixGlobalStatisticsHolder = hysterixGlobalStatisticsHolder;
        this.hysterixSettings = hysterixSettings;
    }

    public synchronized DefaultHysterixCircuitBreaker getCircuitBreaker(final HysterixCommand hysterixCommand) {
        final String commandGroupKey = (String) hysterixCommand.getCommandGroupKey().orElse("");

        return getCircuitBreaker(commandGroupKey, hysterixCommand.getCommandKey());
    }

    public synchronized DefaultHysterixCircuitBreaker getCircuitBreaker(final String commandGroupKey, final String commandKey) {
        final String key = String.format("%s.%s", commandGroupKey, commandKey);
        final HysterixGlobalStatistics hysterixCacheMetrics = hysterixGlobalStatisticsHolder.getHysterixCacheMetrics(commandGroupKey, commandKey);
        final DefaultHysterixCircuitBreaker defaultHysterixCircuitBreaker = cache.getOrDefault(key, new DefaultHysterixCircuitBreaker(commandGroupKey, commandKey, hysterixCacheMetrics, hysterixSettings));

        cache.put(key, defaultHysterixCircuitBreaker);

        return defaultHysterixCircuitBreaker;
    }

    public Collection<DefaultHysterixCircuitBreaker> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

}
