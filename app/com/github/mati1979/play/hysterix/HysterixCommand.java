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
    //this method is used only if needed, execute will tryCache out if the response can come from a cache
    protected abstract F.Promise<T> run();

    protected HysterixCommand(final HysterixContext context) {
        this.hysterixContext = new AtomicReference<>(context);
    }

    public HysterixSettings getHysterixSettings() {
        return hysterixContext.get().getHysterixSettings();
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

        return tryCache().map(response -> onSuccess(response)).recover(t -> onRecover(t));
    }

    public HysterixResponseMetadata getMetadata() {
        return metadata;
    }

    public Optional<T> getFallback() {
        return Optional.empty();
    }

    //checks cache or does invoke run method
    private F.Promise<T> tryCache() {
        if (isRequestCachingEnabled()) {
            final HysterixRequestCacheHolder hysterixRequestCacheHolder = hysterixContext.get().getHysterixRequestCacheHolder();
            final HttpRequestsCache<T> cache = hysterixRequestCacheHolder.getOrCreate(getCommandKey());

            return cache.createRequest(this).executeRequest();
        }

        return run();
    }

    private HysterixResponse<T> onSuccess(final T response) {
        logger.debug("onSuccess..." + response.hashCode());
        metadata.getStopwatch().stop();
        metadata.markSuccess();
        executionComplete();

        return HysterixResponse.create(response, metadata);
    }

    private void executionComplete() {
        hysterixContext.get().getHysterixRequestLog().addExecutedCommand(this);
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
