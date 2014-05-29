package com.github.mati1979.play.hysterix;

import play.libs.F;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class HysterixCommand<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixCommand.class);

    protected final String httpRequestId = UUID.randomUUID().toString();

    protected final AtomicReference<HysterixContext> hysterixContext;

    protected HysterixResponseMetadata metadata = new HysterixResponseMetadata();

    //transform response into domain object, client is responsible to call web service
    //this method is used only if needed, execute will tryCache out if the response can come from a cache
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

    public Optional<String> getRemoteUrl() {
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
        if (isRequestCachingDisabled() || !getRequestCacheKey().isPresent()) {
            logger.debug("caching disabled:commandKey:" + getCommandKey(),"cacheKey:" + getCacheKey());
            return callRemote();
        }
        final String requestCacheKey = getRequestCacheKey().get();
        logger.debug(String.format("requestCacheKey:%s", requestCacheKey));

        final HysterixRequestCacheHolder hysterixRequestCacheHolder = hysterixContext.get().getHysterixRequestCacheHolder();
        final HysterixHttpRequestsCache cache = hysterixRequestCacheHolder.getOrCreate(requestCacheKey);

        return cache.addRequest(httpRequestId, this).execute(httpRequestId).map(cacheResp -> {
            if (cacheResp.isCacheHit()) {
                getMetadata().markResponseFromCache();
            } else {
                getMetadata().markSuccess();
            }
            return (T) cacheResp.getData();
        });
    }

    private Optional<String> getRequestCacheKey() {
        return getCacheKey().map(key -> String.format("%s.%s.%s", getCommandGroupKey().orElse("?"), getCommandKey(), key));
    }

    protected F.Promise<T> callRemote() {
        logger.debug("calling remote system for command:" + getCommandKey() + ",cacheKey:" + getCacheKey());
        return run();
    }

    private HysterixResponse<T> onSuccess(final T response) {
        logger.debug("onSuccess, command:" + getCommandKey() + ",key:" + getCacheKey());
        executionComplete();

        return HysterixResponse.create(response, metadata);
    }

    private void executionComplete() {
        metadata.getStopwatch().stop();
        hysterixContext.get().getHysterixRequestLog().addExecutedCommand(this);
    }

    private HysterixResponse<T> onRecover(final Throwable t) throws Throwable {
        logger.warn("onRecover handling in hysterix", t);
        final HysterixSettings hysterixSettings = hysterixContext.get().getHysterixSettings();

        if (hysterixSettings.isFallbackEnabled()) {
            logger.warn("onRecover - fallback enabled");

            return getFallback().map(response -> onRecoverSuccess(response))
                    .orElseThrow(() -> onRecoverFailure(t));
        }

        throw t;
    }

    //this is the end of path
    private HysterixResponse<T> onRecoverSuccess(final T t) {
        metadata.markFallbackSuccess();
        executionComplete();

        return HysterixResponse.create(t, metadata);
    }

    //this is the end of path
    private Throwable onRecoverFailure(final Throwable t) {
        metadata.markFailure();
        metadata.markFallbackFailure();
        if (t instanceof TimeoutException) {
            metadata.markTimeout();
        }

        executionComplete();
        return t;
    }

    private boolean isRequestCachingEnabled() {
        final HysterixSettings hysterixSettings = hysterixContext.get().getHysterixSettings();

        return hysterixSettings.isRequestCacheEnabled() && getCacheKey().isPresent();
    }

    private boolean isRequestCachingDisabled() {
        return !isRequestCachingEnabled();
    }

}
