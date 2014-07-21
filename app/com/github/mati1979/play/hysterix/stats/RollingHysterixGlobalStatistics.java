package com.github.mati1979.play.hysterix.stats;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.github.mati1979.play.hysterix.HysterixSettings;

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
