package com.github.mati1979.play.hysterix;

import com.github.mati1979.play.hysterix.circuit.HysterixCircuitBreaker;
import com.github.mati1979.play.hysterix.event.HysterixCommandEvent;
import play.libs.F;

import java.util.Optional;
import java.util.UUID;

public abstract class HysterixCommand<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixCommand.class);

    protected final String httpRequestId = UUID.randomUUID().toString();

    protected final HysterixRequestContext hysterixRequestContext;

    protected final HysterixResponseMetadata metadata = new HysterixResponseMetadata();

    protected HysterixCommand(final HysterixRequestContext hysterixRequestContext) {
        this.hysterixRequestContext = hysterixRequestContext;
    }

    //transform response into domain object, client is responsible to call web service
    //this method is used only if needed, execute will tryCall out if the response can come from a cache
    protected abstract F.Promise<T> run();

    public String getCommandId() {
        return httpRequestId;
    }

    public HysterixCircuitBreaker getHysterixCircuitBreaker() {
        if (hysterixRequestContext.getHysterixContext().getHysterixSettings().isCircuitBreakerEnabled()) {
            return hysterixRequestContext.getHysterixContext().getHysterixCircuitBreakerHolder().getCircuitBreaker(this);
        }

        return HysterixCircuitBreaker.NULL;
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

        return tryCall().map(response -> onSuccess(response)).recoverWith(t -> onRecover(t));
    }

    public HysterixResponseMetadata getMetadata() {
        return metadata;
    }

    public Optional<F.Promise<T>> getFallbackTo() {
        return Optional.empty();
    }

    private F.Promise<T> tryCall() {
        logger.debug("tryCall");
        if (!getHysterixCircuitBreaker().allowRequest()) {
            logger.debug("request not allowed - short circuit:" + getCommandKey());
            metadata.markShortCircuited();
            return F.Promise.throwing(new RuntimeException("circuit breaker closed!"));
        }

        logger.debug("request allowed..." + getCommandKey());

        if (isRequestCachingDisabled() || !getRequestCacheKey().isPresent()) {
            logger.debug("Caching disabled - commandKey:" + getCommandKey());

            return callRemote();
        }

        return tryCache();
    }

    private F.Promise<T> tryCache() {
        final String requestCacheKey = getRequestCacheKey().get();
        logger.debug(String.format("Trying to use request cache, requestCacheKey:%s", requestCacheKey));

        final HysterixRequestCacheHolder hysterixRequestCacheHolder = hysterixRequestContext.getHysterixRequestCacheHolder();
        final HysterixHttpRequestsCache<T> cache = hysterixRequestCacheHolder.getOrCreate(requestCacheKey);

        return cache.addRequest(httpRequestId, this).execute(httpRequestId).map(cacheResp -> {
            if (cacheResp.isCacheHit()) {
                getMetadata().markResponseFromCache();
            }

            return cacheResp.getData();
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
        getMetadata().markSuccess();
        getHysterixCircuitBreaker().markSuccess();

        executionComplete();

        return HysterixResponse.create(response, metadata);
    }

    private void executionComplete() {
        if (metadata.getStopwatch().isRunning()) {
            metadata.getStopwatch().stop();
        }

        logger.debug("Execution complete, url:" + getRemoteUrl().orElse("?"));
        hysterixRequestContext.getHysterixRequestLog().addExecutedCommand(this);
        hysterixRequestContext.getHysterixContext().getEventBus().post(new HysterixCommandEvent(this));
    }

    private F.Promise<HysterixResponse<T>> onRecover(final Throwable t) throws Throwable {
        logger.warn("Remote call failed, url:" + getRemoteUrl().orElse("?"), t);
        final HysterixSettings hysterixSettings = hysterixRequestContext.getHysterixContext().getHysterixSettings();
        if (t instanceof java.util.concurrent.TimeoutException) {
            logger.warn("Timeout from service, url:" + getRemoteUrl().orElse("?"));
            metadata.markTimeout();
        }
        if (hysterixSettings.isFallbackEnabled()) {
            logger.debug("onRecover - fallback enabled.");

            if (!metadata.isError()) {
                metadata.markFailure();
            }

            return getFallbackTo().map(response -> onRecoverSuccess(response))
                    .orElseThrow(() -> onRecoverFailure(t));
        }

        if (!metadata.isError()) {
            metadata.markExceptionThrown();
        }

        logger.error("Remote call failed, url:" + getRemoteUrl().orElse("?"), t);

        throw t;
    }

    private F.Promise<HysterixResponse<T>> onRecoverSuccess(final F.Promise<T> response) {
        logger.debug("Successfully recovered remote call failure, command:" + getCommandKey() + ",url:" + getRemoteUrl().orElse("?"));

        metadata.markFallbackSuccess();
        executionComplete();

        return response.map(data -> HysterixResponse.create(data, metadata));
    }

    private Throwable onRecoverFailure(final Throwable t) {
        logger.error("Recovery from remote call failure, url:" + getRemoteUrl().orElse("?"));
        metadata.markFallbackFailure();
        metadata.markExceptionThrown();

        executionComplete();
        return t;
    }

    private boolean isRequestCachingEnabled() {
        final HysterixSettings hysterixSettings = hysterixRequestContext.getHysterixContext().getHysterixSettings();

        return hysterixSettings.isRequestCacheEnabled() && getCacheKey().isPresent();
    }

    private boolean isRequestCachingDisabled() {
        return !isRequestCachingEnabled();
    }

    @Override
    public String toString() {
        return "HysterixCommand{" +
                "httpRequestId='" + httpRequestId + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
