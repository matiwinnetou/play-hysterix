package com.github.mati1979.play.hysterix.stats;

import com.github.mati1979.play.hysterix.HysterixSettings;
import com.yammer.metrics.ExponentiallyDecayingReservoir;
import com.yammer.metrics.Histogram;

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
