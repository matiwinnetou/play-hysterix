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

/**
 * Created by mszczap on 01.06.14.
 */
public class HysterixGlobalStatisticsHolder {

    private final Map<String, HysterixGlobalStatistics> cache = Maps.newConcurrentMap();

    private final HysterixSettings hysterixSettings;

    private final EventBus eventBus;

    public HysterixGlobalStatisticsHolder(final EventBus eventBus, final HysterixSettings hysterixSettings) {
        this.eventBus = eventBus;
        this.hysterixSettings = hysterixSettings;
        eventBus.register(new Subscriber());
    }

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
