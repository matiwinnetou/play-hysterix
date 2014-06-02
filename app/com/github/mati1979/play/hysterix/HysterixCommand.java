package com.github.mati1979.play.hysterix;

import play.libs.F;

import java.util.Optional;
import java.util.UUID;

public abstract class HysterixCommand<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixCommand.class);

    protected final String httpRequestId = UUID.randomUUID().toString();

    protected final HysterixContext hysterixContext;

    protected HysterixResponseMetadata metadata = new HysterixResponseMetadata();

    //transform response into domain object, client is responsible to call web service
    //this method is used only if needed, execute will tryCache out if the response can come from a cache
    protected abstract F.Promise<T> run();

    protected HysterixCommand(final HysterixContext context) {
        this.hysterixContext = context;
    }

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

    private HysterixResponse<T> onRecover(final Throwable t) throws Throwable {
        logger.warn("Remote call failed, url:" + getRemoteUrl().orElse("?"), t);
        final HysterixSettings hysterixSettings = hysterixContext.getHysterixSettings();
        metadata.markFailure();
        if (t instanceof java.util.concurrent.TimeoutException) {
            logger.warn("Timeout from service, url:" + getRemoteUrl().orElse("?"));
            metadata.markTimeout();
        }
        if (hysterixSettings.isFallbackEnabled()) {
            logger.debug("onRecover - fallback enabled.");

            return getFallback().map(response -> onRecoverSuccess(response))
                    .orElseThrow(() -> onRecoverFailure(t));
        }

        throw t;
    }

    private HysterixResponse<T> onRecoverSuccess(final T t) {
        logger.debug("Successfully recovered remote call failure, command:" + getCommandKey() + ",url:"
                + getRemoteUrl().orElse("?") + ",message:" + t.toString());

        metadata.markFallbackSuccess();
        executionComplete();

        return HysterixResponse.create(t, metadata);
    }

    private Throwable onRecoverFailure(final Throwable t) {
        logger.error("Recovery from remote call failure, url:" + getRemoteUrl().orElse("?"));
        metadata.markFallbackFailure();
        metadata.markExceptionThrown(); //TODO what is different about this and fallback failure?

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
