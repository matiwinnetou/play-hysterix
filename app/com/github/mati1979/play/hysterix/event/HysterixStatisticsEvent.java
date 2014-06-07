package com.github.mati1979.play.hysterix.event;

import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatistics;

/**
 * Created by mati on 06/06/2014.
 */
public class HysterixStatisticsEvent {

    private final HysterixCommandEvent event;
    private final HysterixGlobalStatistics stats;

    public HysterixStatisticsEvent(final HysterixCommandEvent event, final HysterixGlobalStatistics stats) {
        this.event = event;
        this.stats = stats;
    }

    public HysterixCommandEvent getEvent() {
        return event;
    }

    public HysterixGlobalStatistics getStats() {
        return stats;
    }

}
