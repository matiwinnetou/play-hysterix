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

    private final Map<String, HysterixGlobalStatistics> cache = Maps.newConcurrentMap();

    private final HysterixSettings hysterixSettings;
    private final EventBus eventBus;

    private ReentrantLock lock = new ReentrantLock(true);

    public HysterixGlobalStatisticsHolder(final HysterixSettings hysterixSettings,
                                          final EventBus eventBus) {
        this.hysterixSettings = hysterixSettings;
        this.eventBus = eventBus;
        eventBus.register(new Subscriber());
    }

    public HysterixGlobalStatistics getHysterixCacheMetrics(final HysterixCommand hysterixCommand) {
        return getHysterixCacheMetrics((String) hysterixCommand.getCommandGroupKey().orElse(""), hysterixCommand.getCommandKey());
    }

    public HysterixGlobalStatistics getHysterixCacheMetrics(final String commandGroupKey, final String commandKey) {
        try {
            lock.lock();
            final String key = String.format("%s.%s", commandGroupKey, commandKey);
            final HysterixGlobalStatistics hysterixGlobalStatistics = cache.getOrDefault(key, new HysterixGlobalStatistics(hysterixSettings, key));

            cache.put(key, hysterixGlobalStatistics);

            return hysterixGlobalStatistics;
        } finally {
            lock.unlock();
        }
    }

    public Collection<HysterixGlobalStatistics> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

    private final class Subscriber {

        @Subscribe
        public void onEvent(final HysterixCommandEvent event) {
            final HysterixGlobalStatistics hysterixGlobalStatistics = getHysterixCacheMetrics(event.getHysterixCommand());
            if (hysterixSettings.isLogGlobalStatistics()) {
                hysterixGlobalStatistics.notify(event.getHysterixCommand().getMetadata());
            }
            eventBus.post(new HysterixStatisticsEvent(event, hysterixGlobalStatistics));
        }

    }

}
