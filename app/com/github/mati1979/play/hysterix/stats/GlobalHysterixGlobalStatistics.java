package com.github.mati1979.play.hysterix.stats;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.github.mati1979.play.hysterix.HysterixSettings;

/**
 * Created by mszczap on 01.06.14.
 */
public class GlobalHysterixGlobalStatistics extends AbstractHysterixGlobalStatistics {

    public GlobalHysterixGlobalStatistics(final HysterixSettings hysterixSettings, final String key) {
        super(hysterixSettings, key);
    }

    protected Histogram createHistogram() {
        return new Histogram(new ExponentiallyDecayingReservoir());
    }

}
