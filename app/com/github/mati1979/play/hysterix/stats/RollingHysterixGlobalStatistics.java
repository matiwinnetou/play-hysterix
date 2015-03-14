package com.github.mati1979.play.hysterix.stats;

import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.github.mati1979.play.hysterix.HysterixSettings;

import java.util.concurrent.TimeUnit;

public class RollingHysterixGlobalStatistics extends AbstractHysterixGlobalStatistics {

    public RollingHysterixGlobalStatistics(final HysterixSettings hysterixSettings, final String key) {
        super(hysterixSettings, key);
    }

    protected Reservoir createReservoir() {
        return new SlidingTimeWindowReservoir(hysterixSettings.getRollingTimeWindowIntervalInMs(), TimeUnit.MILLISECONDS);
    }

}
