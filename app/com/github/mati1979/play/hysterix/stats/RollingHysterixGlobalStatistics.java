package com.github.mati1979.play.hysterix.stats;

import com.github.mati1979.play.hysterix.HysterixSettings;
import com.yammer.metrics.Histogram;
import com.yammer.metrics.SlidingTimeWindowReservoir;

import java.util.concurrent.TimeUnit;

/**
 * Created by mszczap on 01.06.14.
 */
public class RollingHysterixGlobalStatistics extends AbstractHysterixGlobalStatistics {

    public RollingHysterixGlobalStatistics(final HysterixSettings hysterixSettings, final String key) {
        super(hysterixSettings, key);
    }

    protected Histogram createHistogram() {
        final long rollingTimeWindowIntervalInMs = hysterixSettings.getRollingTimeWindowIntervalInMs();

        return new Histogram(new SlidingTimeWindowReservoir(rollingTimeWindowIntervalInMs, TimeUnit.MILLISECONDS));
    }

}
