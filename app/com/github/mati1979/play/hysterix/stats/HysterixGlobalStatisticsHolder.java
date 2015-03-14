package com.github.mati1979.play.hysterix.stats;

import com.github.mati1979.play.hysterix.HysterixCommand;
import com.github.mati1979.play.hysterix.HysterixSettings;
import com.github.mati1979.play.hysterix.event.HysterixCommandEvent;
import com.github.mati1979.play.hysterix.event.HysterixStatisticsEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HysterixGlobalStatisticsHolder {

    private final Map<String, RollingHysterixGlobalStatistics> rollingCache = new ConcurrentHashMap<>();
    private final Map<String, GlobalHysterixGlobalStatistics> globalCache = new ConcurrentHashMap<>();

    private final HysterixSettings hysterixSettings;
    private final EventBus eventBus;

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

        return rollingCache.computeIfAbsent(key, k -> new RollingHysterixGlobalStatistics(hysterixSettings, k));
    }

    public GlobalHysterixGlobalStatistics getGlobalMetrics(final HysterixCommand hysterixCommand) {
        final String commandGroupKey = (String) hysterixCommand.getCommandGroupKey().orElse("");

        return getGlobalMetrics(commandGroupKey, hysterixCommand.getCommandKey());
    }

    public GlobalHysterixGlobalStatistics getGlobalMetrics(final String commandGroupKey, final String commandKey) {
        final String key = String.format("%s.%s", commandGroupKey, commandKey);

        return globalCache.computeIfAbsent(key, k -> new GlobalHysterixGlobalStatistics(hysterixSettings, k));
    }

    public Collection<RollingHysterixGlobalStatistics> getAllTimeWindowed() {
        return Collections.unmodifiableCollection(rollingCache.values());
    }

    public Collection<GlobalHysterixGlobalStatistics> getAllGlobal() {
        return Collections.unmodifiableCollection(globalCache.values());
    }

    private final class Subscriber {

        @Subscribe
        public void onEvent(final HysterixCommandEvent event) {
            final HysterixCommand hysterixCommand = event.getHysterixCommand();
            final RollingHysterixGlobalStatistics timeWindowedStats = getTimeWindowedMetrics(hysterixCommand);
            final GlobalHysterixGlobalStatistics globalStats = getGlobalMetrics(hysterixCommand);

            if (hysterixSettings.isLogGlobalStatistics()) {
                timeWindowedStats.notify(event.getHysterixCommand().getMetadata());
                globalStats.notify(event.getHysterixCommand().getMetadata());
            }

            eventBus.post(new HysterixStatisticsEvent(event, timeWindowedStats, globalStats));
        }

    }

}
