package com.github.mati1979.play.hysterix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import play.libs.F;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by mati on 24/05/2014.
 */
@NotThreadSafe
public class HysterixHttpRequestsCache {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixHttpRequestsCache.class);

    private final String clientGroupId;

    private Optional<HysterixCommand> realCommand = Optional.empty();

    private Optional<RealResponse> realResponse = Optional.empty();

    private Map<String, LazyPromise> lazyProxyPromises = Maps.newHashMap();

    private List<HysterixCommand> hystrixCommands = Lists.newArrayList();

    public HysterixHttpRequestsCache(final String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public synchronized HysterixHttpRequestsCache addRequest(final String requestId, final HysterixCommand command) {
        if (shouldNotCache(command)) {
            return this;
        }

        lazyProxyPromises.put(requestId, createLazyPromise(command));
        hystrixCommands.add(command);

        return this;
    }

    public synchronized F.Promise<CacheResp> execute(final String requestId) {
        if (realResponse.isPresent() && realResponse.get().isCompleted()) {
            final RealResponse response = realResponse.get();
            logger.debug("isCompleted, groupId:" + clientGroupId);
            if (response.successValue.isPresent()) {
                logger.debug("isCompleted, success:" + clientGroupId);
                final CacheResp cacheResp = new CacheResp(true);
                cacheResp.data = response.successValue.get();

                return F.Promise.pure(cacheResp);
            }
            if (response.failureValue.isPresent()) {
                logger.debug("isCompleted, failure:" + clientGroupId);
                return F.Promise.throwing(response.failureValue.get());
            }
        }

        if (realCommand.isPresent()) {
            return getLazyPromise(requestId);
        }

        return realGet(requestId);
    }

    private void redeemSuccess(final Object data) {
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

    private boolean shouldNotCache(final HysterixCommand command) {
        return !command.getCacheKey().isPresent();
    }

    public F.Promise<CacheResp> getLazyPromise(final String requestId) {
        return F.Promise.<CacheResp>wrap(asScalaPromise(requestId).future());
    }

    private scala.concurrent.Promise asScalaPromise(final String requestId) {
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

    private F.Promise realGet(final String requestId) {
        logger.debug(String.format("real get for requestId:%s, groupId:%s", requestId, clientGroupId));
        if (hystrixCommands.isEmpty()) {
            return F.Promise.throwing(new RuntimeException("You must first enqueue a holder via addRequest method!"));
        }

        final HysterixCommand command = hystrixCommands.iterator().next();
        final F.Promise realResponseP = command.callRemote();
        this.realCommand = Optional.of(command);

        realResponseP.onRedeem(response -> redeemSuccess(response));
        realResponseP.onFailure(t -> redeemFailure((Throwable)t));

        return getLazyPromise(requestId);
    }

    private LazyPromise createLazyPromise(final HysterixCommand hysterixCommand) {
        return new LazyPromise(scala.concurrent.Promise$.MODULE$.apply(), hysterixCommand);
    }

    private static class RealResponse {

        private Optional<Object> successValue = Optional.empty();
        private Optional<Throwable> failureValue = Optional.empty();

        public boolean isCompleted() {
            return successValue.isPresent() || failureValue.isPresent();
        }

    }

    private static class LazyPromise {

        private scala.concurrent.Promise scalaPromise;
        private HysterixCommand hysterixCommand;
        private boolean callbackExecuted = false;

        private LazyPromise(scala.concurrent.Promise scalaPromise, final HysterixCommand hysterixCommand) {
            this.scalaPromise = scalaPromise;
            this.hysterixCommand = hysterixCommand;
        }

    }

    public static class CacheResp {

        private Object data;
        private boolean cacheHit = false;

        public CacheResp(final boolean cacheHit) {
            this.cacheHit = cacheHit;
        }

        public boolean isCacheHit() {
            return cacheHit;
        }

        public Object getData() {
            return data;
        }

    }

}
