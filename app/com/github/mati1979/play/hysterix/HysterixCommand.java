package com.github.mati1979.play.hysterix;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import play.libs.F;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class HysterixCommand<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixCommand.class);

    protected final AtomicBoolean isExecutionComplete = new AtomicBoolean(false);

    protected List<HysterixEventType> executionEvents = Collections.synchronizedList(Lists.newArrayList());

    private AtomicReference<Stopwatch> stopwatch = new AtomicReference(new Stopwatch());

    protected final HysterixSettings hysterixSettings;
    protected final HysterixRequestLog hysterixRequestLog;
    protected final HysterixRequestCacheHolder hysterixRequestCacheHolder;

    //transform response into domain object, client is responsible to call web service
    //this method is used only if needed, execute will work out if the response can come from a cache
    protected abstract F.Promise<T> run();

    protected HysterixCommand(final HysterixRequestCacheHolder hysterixRequestCacheHolder,
                              final HysterixRequestLog hysterixRequestLog,
                              final HysterixSettings hysterixSettings) {
        this.hysterixRequestCacheHolder = hysterixRequestCacheHolder;
        this.hysterixSettings = hysterixSettings;
        this.hysterixRequestLog = hysterixRequestLog;
    }

    protected HysterixCommand(final HysterixContext context) {
        this.hysterixRequestCacheHolder = context.getHysterixRequestCacheHolder();
        this.hysterixSettings = context.getHysterixSettings();
        this.hysterixRequestLog = context.getHysterixRequestLog();
    }

    public abstract String getCommandKey();

    public Optional<String> getCommandGroupKey() {
        return Optional.empty();
    }

    public Optional<String> getCacheKey() {
        return Optional.empty();
    }

    public F.Promise<T> execute() {
        stopwatch.get().start();

        return work().map(response -> onSuccess(response)).recover(t -> onRecover(t));
    }

    //checks cache or does invoke run method
    private F.Promise<T> work() {
        Optional<F.Promise<T>> fromCache = getFromCache();
        if (fromCache.isPresent()) {
            return fromCache.get();
        }

        return run();
    }

    private T onSuccess(final T response) {
        stopwatch.get().stop();
        executionEvents.add(HysterixEventType.SUCCESS);
        putToCache(response);
        executionComplete();

        return response;
    }

    private void executionComplete() {
        isExecutionComplete.set(true);
        hysterixRequestLog.addExecutedCommand(this);
    }

    private Optional<F.Promise<T>> getFromCache() {
        if (isRequestCachingEnabled()) {
            final HysterixRequestCache<T> cache = hysterixRequestCacheHolder.getOrCreate(getCommandKey());
            final String key = getCacheKey().get();
            final Optional<T> possibleValue = cache.get(key);
            if (possibleValue.isPresent()) {
                final T value = (T) possibleValue.get();
                executionEvents.add(HysterixEventType.RESPONSE_FROM_CACHE);
                return Optional.of(F.Promise.pure(value));
            }
        }

        return Optional.empty();
    }

    private boolean putToCache(final T t) {
        if (isRequestCachingEnabled()) {
            final String key = getCacheKey().get();
            final HysterixRequestCache<T> cache = hysterixRequestCacheHolder.getOrCreate(getCommandKey());
            cache.put(key, t);

            return true;
        }

        return false;
    }

    private void onFailure(final Throwable t) {
        stopwatch.get().stop();
        logger.debug("onFailure:" + t);
        executionEvents.add(HysterixEventType.FAILURE);
        executionComplete();
    }

    private T onRecover(final Throwable t) throws Throwable {
        onFailure(t);
        logger.debug("onRecover:" + t);

        if (hysterixSettings.isFallbackEnabled()) {
            logger.debug("onRecover - fallback enabled");
            return getFallback().map(response -> onRecoverSuccess(response))
                    .orElseThrow(() -> onRecoverFailure(t));
        }

        throw t;
    }

    private T onRecoverSuccess(final T t) {
        executionEvents.add(HysterixEventType.FALLBACK_SUCCESS);
        executionComplete();
        return t;
    }

    private Throwable onRecoverFailure(final Throwable t) {
        if (t instanceof TimeoutException) {
            executionEvents.add(HysterixEventType.TIMEOUT);
        }
        executionEvents.add(HysterixEventType.FAILURE);
        executionEvents.add(HysterixEventType.FALLBACK_FAILURE);

        return t;
    }

    protected boolean isRequestCachingEnabled() {
        return hysterixSettings.isRequestCacheEnabled() && getCacheKey().isPresent();
    }

    public Optional<T> getFallback() {
        return Optional.empty();
    }

    public boolean isExecutionComplete() {
        return isExecutionComplete.get();
    }

    public boolean isSuccessfulExecution() {
        return executionEvents.contains(HysterixEventType.SUCCESS);
    }

    public boolean isFailedExecution() {
        return executionEvents.contains(HysterixEventType.FAILURE);
    }

    public boolean isResponseFromFallback() {
        return executionEvents.contains(HysterixEventType.FALLBACK_SUCCESS);
    }

    public boolean isResponseTimeout() {
        return executionEvents.contains(HysterixEventType.TIMEOUT);
    }

    public boolean isResponseFromCache() {
        return executionEvents.contains(HysterixEventType.RESPONSE_FROM_CACHE);
    }

    public long getExecutionTime(final TimeUnit timeUnit) {
        return stopwatch.get().elapsed(timeUnit);
    }

    public List<HysterixEventType> getExecutionEvents() {
        return Lists.newArrayList(executionEvents);
    }

}
