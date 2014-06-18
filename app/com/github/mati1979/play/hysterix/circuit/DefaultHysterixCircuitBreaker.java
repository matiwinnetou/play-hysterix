package com.github.mati1979.play.hysterix.circuit;

import com.github.mati1979.play.hysterix.HysterixSettings;
import com.github.mati1979.play.hysterix.stats.HysterixGlobalStatistics;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mati on 17/06/2014.
 */
public class DefaultHysterixCircuitBreaker implements HysterixCircuitBreaker {

    private final String commandGroupKey;
    private final String commandKey;

    private final HysterixSettings hysterixSettings;
    private final HysterixGlobalStatistics hysterixGlobalStatistics;

    /* track whether this circuit is open/closed at any given point in time (default to false==closed) */
    private AtomicBoolean circuitOpen = new AtomicBoolean(false);

    /* when the circuit was marked open or was last allowed to try a 'singleTest' */
    private AtomicLong circuitOpenedOrLastTestedTime = new AtomicLong();

    public DefaultHysterixCircuitBreaker(final String commandGroupKey,
                                         final String commandKey,
                                         final HysterixGlobalStatistics hysterixGlobalStatistics,
                                         final HysterixSettings hystrixSettings) {
        this.commandGroupKey = commandGroupKey;
        this.commandKey = commandKey;
        this.hysterixGlobalStatistics = hysterixGlobalStatistics;
        this.hysterixSettings = hystrixSettings;
    }

    public String getCommandGroupKey() {
        return commandGroupKey;
    }

    public String getCommandKey() {
        return commandKey;
    }

    @Override
    public void markSuccess() {
        if (circuitOpen.get()) {
            hysterixGlobalStatistics.clearStats();
            circuitOpen.set(false);
        }
    }

    @Override
    public boolean allowRequest() {
        if (hysterixSettings.isCircuitBreakerForceClosed()) {
            // we still want to allow isOpen() to perform it's calculations so we simulate normal behavior
            isOpen();
            // properties have asked us to ignore errors so we will ignore the results of isOpen and just allow all traffic through
            return true;
        }

        return !isOpen() || allowSingleTest();
    }

    @Override
    public boolean allowSingleTest() {
        long timeCircuitOpenedOrWasLastTested = circuitOpenedOrLastTestedTime.get();
        // 1) if the circuit is open
        // 2) and it's been longer than 'sleepWindow' since we opened the circuit
        if (circuitOpen.get() && System.currentTimeMillis() > timeCircuitOpenedOrWasLastTested + hysterixSettings.getCircuitBreakerSleepWindowInMilliseconds()) {
            // We push the 'circuitOpenedTime' ahead by 'sleepWindow' since we have allowed one request to try.
            // If it succeeds the circuit will be closed, otherwise another singleTest will be allowed at the end of the 'sleepWindow'.
            if (circuitOpenedOrLastTestedTime.compareAndSet(timeCircuitOpenedOrWasLastTested, System.currentTimeMillis())) {
                // if this returns true that means we set the time so we'll return true to allow the singleTest
                // if it returned false it means another thread raced us and allowed the singleTest before we did
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isOpen() {
        if (circuitOpen.get()) {
            // if we're open we immediately return true and don't bother attempting to 'close' ourself as that is left to allowSingleTest and a subsequent successful test to close
            return true;
        }

        // check if we are past the statisticalWindowVolumeThreshold
        if (hysterixGlobalStatistics.getTotalCount() < hysterixSettings.getCircuitBreakerRequestVolumeThreshold()) {
            // we are not past the minimum volume threshold for the statisticalWindow so we'll return false immediately and not calculate anything
            return false;
        }

        if (hysterixGlobalStatistics.getErrorPercentage() < hysterixSettings.getCircuitBreakerErrorThresholdPercentage()) {
            return false;
        }

        // our failure rate is too high, trip the circuit
        if (circuitOpen.compareAndSet(false, true)) {
            // if the previousValue was false then we want to set the currentTime
            // How could previousValue be true? If another thread was going through this code at the same time a race-condition could have
            // caused another thread to set it to true already even though we were in the process of doing the same
            circuitOpenedOrLastTestedTime.set(System.currentTimeMillis());
            return true;
        }

        return false;
    }

}
