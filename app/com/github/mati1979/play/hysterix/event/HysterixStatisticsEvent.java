package com.github.mati1979.play.hysterix.event;

import com.github.mati1979.play.hysterix.stats.GlobalHysterixGlobalStatistics;
import com.github.mati1979.play.hysterix.stats.RollingHysterixGlobalStatistics;

/**
 * Created by mati on 06/06/2014.
 */
public class HysterixStatisticsEvent {

    private final HysterixCommandEvent event;
    private final RollingHysterixGlobalStatistics timeWindowStats;
    private final GlobalHysterixGlobalStatistics globalStats;

    public HysterixStatisticsEvent(final HysterixCommandEvent event,
                                   final RollingHysterixGlobalStatistics timeWindowStats,
                                   final GlobalHysterixGlobalStatistics globalStats) {
        this.event = event;
        this.globalStats = globalStats;
        this.timeWindowStats = timeWindowStats;
    }

    public HysterixCommandEvent getEvent() {
        return event;
    }

    public RollingHysterixGlobalStatistics getTimeWindowedMetrics() {
        return timeWindowStats;
    }

    public GlobalHysterixGlobalStatistics getGlobalMetrics() {
        return globalStats;
    }

    @Override
    public String toString() {
        return "HysterixStatisticsEvent{" +
                "event=" + event +
                ", timeWindowedStats=" + timeWindowStats +
                ", globalStats=" + globalStats +
                '}';
    }

}
