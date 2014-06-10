package com.github.mati1979.play.hysterix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import play.libs.F;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by mati on 24/05/2014.
 */
@NotThreadSafe
public class HysterixHttpRequestsCache<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixHttpRequestsCache.class);

    private final String requestCacheKey;

    private Optional<HysterixCommand<T>> realCommand = Optional.empty();

    private Optional<RealResponse<T>> realResponse = Optional.empty();

    private Map<String, LazyPromise<T>> lazyProxyPromises = Maps.newConcurrentMap();

    private List<HysterixCommand<T>> hystrixCommands = Collections.synchronizedList(Lists.newArrayList());

    public HysterixHttpRequestsCache(final String requestCacheKey) {
        this.requestCacheKey = requestCacheKey;
    }

    public synchronized HysterixHttpRequestsCache<T> addRequest(final String requestId, final HysterixCommand<T> command) {
        if (shouldNotCache(command)) {
            return this;
        }

        lazyProxyPromises.put(requestId, createLazyPromise(command));
        hystrixCommands.add(command);

        return this;
    }

    public synchronized F.Promise<CacheResp<T>> execute(final String requestId) {
        if (realResponse.isPresent() && realResponse.get().isCompleted()) {
            final RealResponse<T> response = realResponse.get();
            logger.debug("Call has been already completed, requestCacheKey:" + requestCacheKey);
            if (response.successValue.isPresent()) {
                logger.debug("Completed with success, requestCacheKey:" + requestCacheKey);
                final CacheResp cacheResp = new CacheResp(true);
                cacheResp.data = response.successValue.get();

                return F.Promise.pure(cacheResp);
            }

            if (response.failureValue.isPresent()) {
                logger.debug("Call has been already completed with failure, requestCacheKey:" + requestCacheKey);
                return F.Promise.throwing(response.failureValue.get());
            }
        }

        if (realCommand.isPresent()) {
            return getLazyPromise(requestId);
        }

        return realGet(requestId);
    }

    private void redeemSuccess(final T data) {
        final RealResponse response = new RealResponse();
        response.successValue = Optional.of(data);
        this.realResponse = Optional.of(response);

        lazyProxyPromises.values().stream().filter(p -> !p.callbackExecuted && realCommand.isPresent()).forEach(lazyPromise -> {
            final CacheResp cacheResp = new CacheResp(true);
            if (realCommand.get() == lazyPromise.hysterixCommand) {
                logger.debug("real success, caching resp -> false:command:" + realCommand.get().getCommandKey());
                cacheResp.cacheHit = false;
            }
            cacheResp.data = data;
            lazyPromise.callbackExecuted = true;
            lazyPromise.scalaPromise.success(cacheResp);
        });
    }

    private boolean shouldNotCache(final HysterixCommand<T> command) {
        return !command.getCacheKey().isPresent();
    }

    public F.Promise<CacheResp<T>> getLazyPromise(final String requestId) {
        return F.Promise.wrap(asScalaPromise(requestId).future());
    }

    private scala.concurrent.Promise<CacheResp<T>> asScalaPromise(final String requestId) {
        return lazyProxyPromises.get(requestId).scalaPromise;
    }

    private void redeemFailure(final Throwable t) {
        final RealResponse response = new RealResponse();
        response.failureValue = Optional.of(t);
        this.realResponse = Optional.of(response);

        lazyProxyPromises.values().stream().filter(p -> !p.callbackExecuted).forEach(lazyPromise -> {
            lazyPromise.callbackExecuted = true;
            lazyPromise.scalaPromise.failure(t);
        });
    }

    private F.Promise<CacheResp<T>> realGet(final String requestId) {
        logger.debug(String.format("real get for requestId:%s, groupId:%s", requestId, requestCacheKey));
        if (hystrixCommands.isEmpty()) {
            return F.Promise.throwing(new RuntimeException("You must first enqueue a holder via addRequest method!"));
        }

        final HysterixCommand<T> command = hystrixCommands.iterator().next();
        final F.Promise<T> realResponseP = command.callRemote();
        this.realCommand = Optional.of(command);

        realResponseP.onRedeem(response -> redeemSuccess(response));
        realResponseP.onFailure(t -> redeemFailure((Throwable)t));

        return getLazyPromise(requestId);
    }

    private LazyPromise<T> createLazyPromise(final HysterixCommand<T> hysterixCommand) {
        return new LazyPromise(scala.concurrent.Promise$.MODULE$.apply(), hysterixCommand);
    }

    private static class RealResponse<T> {

        private Optional<T> successValue = Optional.empty();
        private Optional<Throwable> failureValue = Optional.empty();

        public boolean isCompleted() {
            return successValue.isPresent() || failureValue.isPresent();
        }

    }

    private static class LazyPromise<T> {

        private scala.concurrent.Promise<CacheResp<T>> scalaPromise;
        private HysterixCommand<T> hysterixCommand;
        private boolean callbackExecuted = false;

        private LazyPromise(scala.concurrent.Promise<CacheResp<T>> scalaPromise, final HysterixCommand<T> hysterixCommand) {
            this.scalaPromise = scalaPromise;
            this.hysterixCommand = hysterixCommand;
        }

    }

    public static class CacheResp<T> {

        private T data;
        private boolean cacheHit = false;

        public CacheResp(final boolean cacheHit) {
            this.cacheHit = cacheHit;
        }

        public boolean isCacheHit() {
            return cacheHit;
        }

        public T getData() {
            return data;
        }

    }

}
