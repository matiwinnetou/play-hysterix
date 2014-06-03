package com.github.mati1979.play.hysterix;

import play.libs.F;

import java.util.Optional;
import java.util.UUID;

public abstract class HysterixCommand<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixCommand.class);

    protected final String httpRequestId = UUID.randomUUID().toString();

    protected final HysterixContext hysterixContext;

    protected final HysterixResponseMetadata metadata = new HysterixResponseMetadata();

    protected HysterixCommand(final HysterixContext context) {
        this.hysterixContext = context;
    }

    //transform response into domain object, client is responsible to call web service
    //this method is used only if needed, execute will tryCache out if the response can come from a cache
    protected abstract F.Promise<T> run();

    public String getCommandId() {
        return httpRequestId;
    }

    public Optional<String> getCallingClient() {
        return Optional.empty();
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

        return tryCache().map(response -> onSuccess(response)).recoverWith(t -> onRecover(t));
    }

    public HysterixResponseMetadata getMetadata() {
        return metadata;
    }

    public Optional<F.Promise<T>> getFallbackTo() {
        return Optional.empty();
    }

    @Deprecated
    //use fallbackTo instead, if you need to return a simple value, use F.Promise.pure
    public Optional<T> getFallback() {
        return Optional.empty();
    }

    //checks cache or does invoke run method
    private F.Promise<T> tryCache() {
        if (isRequestCachingDisabled() || !getRequestCacheKey().isPresent()) {
            logger.debug("Caching disabled - commandKey:" + getCommandKey());

            return callRemote();
        }

        final String requestCacheKey = getRequestCacheKey().get();
        logger.debug(String.format("Trying to use request cache, requestCacheKey:%s", requestCacheKey));

        final HysterixRequestCacheHolder hysterixRequestCacheHolder = hysterixContext.getHysterixRequestCacheHolder();
        final HysterixHttpRequestsCache cache = hysterixRequestCacheHolder.getOrCreate(requestCacheKey);

        return cache.addRequest(httpRequestId, this).execute(httpRequestId).map(cacheResp -> {
            getMetadata().markSuccess();
            if (cacheResp.isCacheHit()) {
                getMetadata().markResponseFromCache();
            }

            return (T) cacheResp.getData();
        });
    }

    private Optional<String> getRequestCacheKey() {
        return getCacheKey().map(cacheKey -> String.format("%s.%s.%s", getCommandGroupKey().orElse("?"), getCommandKey(), cacheKey));
    }

    protected F.Promise<T> callRemote() {
        logger.debug("Calling remote system for command:" + getCommandKey() + ",url:" + getRemoteUrl().orElse("?"));
        return run();
    }

    private HysterixResponse<T> onSuccess(final T response) {
        logger.debug("Successful response url:" + getRemoteUrl().orElse("?"));
        executionComplete();

        return HysterixResponse.create(response, metadata);
    }

    private void executionComplete() {
        if (metadata.getStopwatch().isRunning()) {
            metadata.getStopwatch().stop();
        }
        hysterixContext.getHysterixRequestLog().addExecutedCommand(this);
        logger.debug("Execution complete, url:" + getRemoteUrl().orElse("?"));
    }

    private F.Promise<HysterixResponse<T>> onRecover(final Throwable t) throws Throwable {
        logger.warn("Remote call failed, url:" + getRemoteUrl().orElse("?"), t);
        final HysterixSettings hysterixSettings = hysterixContext.getHysterixSettings();
        metadata.markFailure();
        if (t instanceof java.util.concurrent.TimeoutException) {
            logger.warn("Timeout from service, url:" + getRemoteUrl().orElse("?"));
            metadata.markTimeout();
        }
        if (hysterixSettings.isFallbackEnabled()) {
            logger.debug("onRecover - fallback enabled.");

            return getFallbackTo().map(response -> onRecoverSuccess(response))
                    .orElseThrow(() -> onRecoverFailure(t));
        }

        metadata.markExceptionThrown();
        logger.error("Remote call failed, url:" + getRemoteUrl().orElse("?"), t);

        throw t;
    }

    private F.Promise<HysterixResponse<T>> onRecoverSuccess(final F.Promise<T> response) {
        logger.debug("Successfully recovered remote call failure, command:" + getCommandKey() + ",url:" + getRemoteUrl().orElse("?"));

        metadata.markFallbackSuccess();
        executionComplete();

        return response.map(r -> HysterixResponse.create(r, metadata));
    }

    private Throwable onRecoverFailure(final Throwable t) {
        logger.error("Recovery from remote call failure, url:" + getRemoteUrl().orElse("?"));
        metadata.markFallbackFailure();
        metadata.markExceptionThrown();

        executionComplete();
        return t;
    }

    private boolean isRequestCachingEnabled() {
        final HysterixSettings hysterixSettings = hysterixContext.getHysterixSettings();

        return hysterixSettings.isRequestCacheEnabled() && getCacheKey().isPresent();
    }

    private boolean isRequestCachingDisabled() {
        return !isRequestCachingEnabled();
    }

}
