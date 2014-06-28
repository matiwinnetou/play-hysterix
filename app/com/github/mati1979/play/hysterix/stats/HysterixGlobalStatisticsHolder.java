package com.github.mati1979.play.hysterix.stats;

import com.github.mati1979.play.hysterix.HysterixCommand;
import com.github.mati1979.play.hysterix.HysterixSettings;
import com.github.mati1979.play.hysterix.event.HysterixCommandEvent;
import com.github.mati1979.play.hysterix.event.HysterixStatisticsEvent;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixGlobalStatisticsHolder {

    private final Map<String, RollingHysterixGlobalStatistics> rollingCache = Maps.newConcurrentMap();
    private final Map<String, GlobalHysterixGlobalStatistics> globalCache = Maps.newConcurrentMap();

    private final HysterixSettings hysterixSettings;
    private final EventBus eventBus;

    private ReentrantLock rollingLock = new ReentrantLock(true);
    private ReentrantLock globalLock = new ReentrantLock(true);

    public HysterixGlobalStatisticsHolder(final HysterixSettings hysterixSettings,
                                          final EventBus eventBus) {
        this.hysterixSettings = hysterixSettings;
        this.eventBus = eventBus;
        eventBus.register(new Subscriber());
    }

    public RollingHysterixGlobalStatistics getTimeWindowedMetrics(final HysterixCommand hysterixCommand) {
        return getTimeWindowedMetrics((String) hysterixCommand.getCommandGroupKey().orElse(""), hysterixCommand.getCommandKey());
    }

    public RollingHysterixGlobalStatistics getTimeWindowedMetrics(final String commandGroupKey, final String commandKey) {
        final String key = String.format("%s.%s", commandGroupKey, commandKey);

        try {
            rollingLock.lock();
            final RollingHysterixGlobalStatistics hysterixGlobalStatistics = rollingCache.getOrDefault(key, new RollingHysterixGlobalStatistics(hysterixSettings, key));

            rollingCache.put(key, hysterixGlobalStatistics);

            return hysterixGlobalStatistics;
        } finally {
            rollingLock.unlock();
        }
    }

    public GlobalHysterixGlobalStatistics getGlobalMetrics(final HysterixCommand hysterixCommand) {
        return getGlobalMetrics((String) hysterixCommand.getCommandGroupKey().orElse(""), hysterixCommand.getCommandKey());
    }

    public GlobalHysterixGlobalStatistics getGlobalMetrics(final String commandGroupKey, final String commandKey) {
        final String key = String.format("%s.%s", commandGroupKey, commandKey);

        try {
            globalLock.lock();
            final GlobalHysterixGlobalStatistics globalHysterixGlobalStatistics = globalCache.getOrDefault(key, new GlobalHysterixGlobalStatistics(hysterixSettings, key));

            globalCache.put(key, globalHysterixGlobalStatistics);

            return globalHysterixGlobalStatistics;
        } finally {
            globalLock.unlock();
        }
    }

    public Collection<RollingHysterixGlobalStatistics> getAll() {
        return Collections.unmodifiableCollection(rollingCache.values());
    }

    private final class Subscriber {

        @Subscribe
        public void onEvent(final HysterixCommandEvent event) {
            final RollingHysterixGlobalStatistics timeWindowedStats = getTimeWindowedMetrics(event.getHysterixCommand());
            final GlobalHysterixGlobalStatistics globalStats = getGlobalMetrics(event.getHysterixCommand());
            if (hysterixSettings.isLogGlobalStatistics()) {
                timeWindowedStats.notify(event.getHysterixCommand().getMetadata());
                globalStats.notify(event.getHysterixCommand().getMetadata());
            }
            eventBus.post(new HysterixStatisticsEvent(event, timeWindowedStats, globalStats));
        }

    }

}
