package com.github.mati1979.play.hysterix.circuit;

import com.github.mati1979.play.hysterix.HysterixCommand;
import com.github.mati1979.play.hysterix.HysterixSettings;
import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatisticsHolder;
import com.github.mati1979.play.hysterix.stats.RollingHysterixGlobalStatistics;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixCircuitBreakerHolder {

    private final Map<String, DefaultHysterixCircuitBreaker> cache = new ConcurrentHashMap();

    private final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder;
    private final HysterixSettings hysterixSettings;

    public HysterixCircuitBreakerHolder(final HysterixGlobalStatisticsHolder hysterixGlobalStatisticsHolder, final HysterixSettings hysterixSettings) {
        this.hysterixGlobalStatisticsHolder = hysterixGlobalStatisticsHolder;
        this.hysterixSettings = hysterixSettings;
    }

    public DefaultHysterixCircuitBreaker getCircuitBreaker(final HysterixCommand hysterixCommand) {
        return getCircuitBreaker((String) hysterixCommand.getCommandGroupKey().orElse(""), hysterixCommand.getCommandKey());
    }

    public DefaultHysterixCircuitBreaker getCircuitBreaker(final String commandGroupKey, final String commandKey) {
        final String key = String.format("%s.%s", commandGroupKey, commandKey);

        return cache.computeIfAbsent(key, k -> {
            final RollingHysterixGlobalStatistics hysterixCacheMetrics = hysterixGlobalStatisticsHolder.getTimeWindowedMetrics(commandGroupKey, commandKey);

            return new DefaultHysterixCircuitBreaker(commandGroupKey, commandKey, hysterixCacheMetrics, hysterixSettings);
        });
    }

    public Collection<DefaultHysterixCircuitBreaker> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

}
