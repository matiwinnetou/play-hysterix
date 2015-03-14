package com.github.mati1979.play.hysterix.stats;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Reservoir;
import com.github.mati1979.play.hysterix.HysterixSettings;

public class GlobalHysterixGlobalStatistics extends AbstractHysterixGlobalStatistics {

    public GlobalHysterixGlobalStatistics(final HysterixSettings hysterixSettings, final String key) {
        super(hysterixSettings, key);
    }

    protected Reservoir createReservoir() {
        return new ExponentiallyDecayingReservoir();
    }

}
