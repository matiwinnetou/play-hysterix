package com.github.mati1979.play.hysterix;

import play.libs.F;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class HysterixCommand<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixCommand.class);

    protected final AtomicReference<HysterixContext> hysterixContext;

    protected HysterixResponseMetadata metadata = new HysterixResponseMetadata();

    //transform response into domain object, client is responsible to call web service
    //this method is used only if needed, execute will work out if the response can come from a cache
    protected abstract F.Promise<T> run();

    protected HysterixCommand(final HysterixContext context) {
        this.hysterixContext = new AtomicReference<>(context);
    }

    public abstract String getCommandKey();

    public Optional<String> getCommandGroupKey() {
        return Optional.empty();
    }

    public Optional<String> getCacheKey() {
        return Optional.empty();
    }

    public F.Promise<HysterixResponse<T>> execute() {
        metadata.getStopwatch().start();

        return work().map(response -> onSuccess(response)).recover(t -> onRecover(t));
    }

    public HysterixResponseMetadata getMetadata() {
        return metadata;
    }

    public Optional<T> getFallback() {
        return Optional.empty();
    }

    //checks cache or does invoke run method
    private F.Promise<T> work() {
        Optional<F.Promise<T>> fromCache = getFromCache();
        if (fromCache.isPresent()) {
            return fromCache.get();
        }

        return run();
    }

    private HysterixResponse<T> onSuccess(final T response) {
        logger.debug("onSuccess..." + response.hashCode());
        metadata.getStopwatch().stop();
        metadata.markSuccess();
        putToCache(response);
        executionComplete();

        return HysterixResponse.create(response, metadata);
    }

    private void executionComplete() {
        hysterixContext.get().getHysterixRequestLog().addExecutedCommand(this);
    }

    private Optional<F.Promise<T>> getFromCache() {
        logger.debug("getting from cache...");
        if (isRequestCachingEnabled()) {
            final HysterixRequestCacheHolder hysterixRequestCacheHolder = hysterixContext.get().getHysterixRequestCacheHolder();

            logger.debug("getFromCache-hysterixRequestCacheHolder:" + hysterixRequestCacheHolder.hashCode());
            logger.debug("isRequestCachingEnabled:" + isRequestCachingEnabled());
            logger.debug("getCommandKey:" + getCommandKey());
            final HysterixRequestCache<T> cache = hysterixRequestCacheHolder.getOrCreate(getCommandKey());
            final String key = getCacheKey().get();
            logger.debug("cacheKey:" + key);
            logger.debug("cacheSize:" + cache.size());
            final Optional<T> possibleValue = cache.get(key);
            logger.debug("possibleValuePresent?:" + possibleValue.isPresent());
            if (possibleValue.isPresent()) {
                final T value = (T) possibleValue.get();
                metadata.markResponseFromCache();
                return Optional.of(F.Promise.pure(value));
            }
        }

        return Optional.empty();
    }

    private boolean putToCache(final T t) {
        logger.debug("putting to cache...");

        if (isRequestCachingEnabled()) {
            final HysterixRequestCacheHolder hysterixRequestCacheHolder = hysterixContext.get().getHysterixRequestCacheHolder();
            logger.debug("putToCache-hysterixRequestCacheHolder:" + hysterixRequestCacheHolder.hashCode());
            final String key = getCacheKey().get();
            logger.debug("cacheKey:" + key);
            logger.debug("getCommandKey:" + getCommandKey());

            final HysterixRequestCache<T> cache = hysterixRequestCacheHolder.getOrCreate(getCommandKey());
            cache.put(key, t);

            return true;
        }

        return false;
    }

    private void onFailure(final Throwable t) {
        logger.warn("onFailure handling in hysterix", t);
        metadata.getStopwatch().stop();
        metadata.markFailure();
        executionComplete();
    }

    private HysterixResponse<T> onRecover(final Throwable t) throws Throwable {
        onFailure(t);
        logger.warn("onRecover:" + t.getMessage());
        final HysterixSettings hysterixSettings = hysterixContext.get().getHysterixSettings();

        if (hysterixSettings.isFallbackEnabled()) {
            logger.warn("onRecover - fallback enabled");

            return getFallback().map(response -> onRecoverSuccess(response))
                    .orElseThrow(() -> onRecoverFailure(t));
        }

        throw t;
    }

    private HysterixResponse<T> onRecoverSuccess(final T t) {
        metadata.markFallbackSuccess();
        executionComplete();
        putToCache(t);

        return HysterixResponse.create(t, metadata);
    }

    private Throwable onRecoverFailure(final Throwable t) {
        if (t instanceof TimeoutException) {
            metadata.markTimeout();
        }

        metadata.markFailure();
        metadata.markFallbackFailure();

        return t;
    }

    private boolean isRequestCachingEnabled() {
        final HysterixSettings hysterixSettings = hysterixContext.get().getHysterixSettings();

        return hysterixSettings.isRequestCacheEnabled() && getCacheKey().isPresent();
    }


}
