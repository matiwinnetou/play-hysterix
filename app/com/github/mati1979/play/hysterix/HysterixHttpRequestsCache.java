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

    private Map<String, ClientsGroup> groupIdToClients = Maps.newHashMap();

    private Map<String, ClientsGroup> requestIdToClients = Maps.newHashMap();

    public HysterixHttpRequestsCache() {
    }

    public synchronized HysterixHttpRequestsCache addRequest(final String requestId, final HysterixCommand command) {
        if (shouldNotCache(command)) {
            return this;
        }

        final String clientsGroupId = (String) command.getCacheKey().get();

        final ClientsGroup clientsGroup = getOrCreateClientsGroup(clientsGroupId);
        requestIdToClients.put(requestId, clientsGroup);

        clientsGroup.lazyProxyPromises.put(requestId, createLazyPromise(command));
        clientsGroup.hystrixCommands.add(command);

        groupIdToClients.put(clientsGroupId, clientsGroup);

        return this;
    }

    public HysterixHttpRequest createRequest(final HysterixCommand command) {
        return HysterixHttpRequest.create(this, command);
    }

    public synchronized F.Promise<CacheResp> execute(final String requestId) {
        final ClientsGroup clientsGroup = requestIdToClients.get(requestId);

        if (clientsGroup.realResponse.isPresent() && clientsGroup.realResponse.get().isCompleted()) {
            final RealResponse response = clientsGroup.realResponse.get();
            logger.debug("isCompleted, groupId:" + clientsGroup.groupId);
            if (response.successValue.isPresent()) {
                logger.debug("isCompleted, success:" + clientsGroup.groupId);
                final CacheResp cacheResp = new CacheResp(true);
                cacheResp.data = response.successValue.get();

                return F.Promise.pure(cacheResp);
            }
            if (response.failureValue.isPresent()) {
                logger.debug("isCompleted, failure:" + clientsGroup.groupId);
                return F.Promise.throwing(response.failureValue.get());
            }
        }

        if (clientsGroup.realRequest.isPresent()) {
            return clientsGroup.getLazyPromise(requestId);
        }

        return realGet(requestId, clientsGroup);
    }

    private boolean shouldNotCache(final HysterixCommand command) {
        return !command.getCacheKey().isPresent();
    }

    private ClientsGroup getOrCreateClientsGroup(final String clientGroupId) {
        return groupIdToClients.getOrDefault(clientGroupId, new ClientsGroup(clientGroupId));
    }

    private F.Promise realGet(final String requestId, final ClientsGroup clientsGroup) {
        logger.debug(String.format("real get for requestId:%s, groupId:%s", requestId, clientsGroup.groupId));
        if (clientsGroup.hystrixCommands.isEmpty()) {
            return F.Promise.throwing(new RuntimeException("You must first enqueue a holder via addRequest method!"));
        }

        final HysterixCommand realCommand = clientsGroup.hystrixCommands.iterator().next();
        final F.Promise realResponseP = realCommand.callRemote();
        final RealRequest realRequest = new RealRequest(realCommand, realResponseP, requestId);
        clientsGroup.realRequest = Optional.of(realRequest);

        realResponseP.onRedeem(response -> clientsGroup.redeemSuccess(response));
        realResponseP.onFailure(t -> clientsGroup.redeemFailure((Throwable)t));

        groupIdToClients.put(clientsGroup.groupId, clientsGroup);

        return clientsGroup.getLazyPromise(requestId);
    }

    private LazyPromise createLazyPromise(final HysterixCommand hysterixCommand) {
        return new LazyPromise(scala.concurrent.Promise$.MODULE$.apply(), hysterixCommand);
    }

    private static class ClientsGroup {

        private final String groupId;
        private Optional<RealRequest> realRequest = Optional.empty();
        private Optional<RealResponse> realResponse = Optional.empty();

        private Map<String, LazyPromise> lazyProxyPromises = Maps.newHashMap();

        private  List<HysterixCommand> hystrixCommands = Lists.newArrayList();

        private ClientsGroup(final String groupId) {
            this.groupId = groupId;
        }

        public F.Promise<CacheResp> getLazyPromise(final String requestId) {
            return F.Promise.<CacheResp>wrap(asScalaPromise(requestId).future());
        }

        private void redeemSuccess(final Object data) {
            final RealResponse response = new RealResponse();
            response.successValue = Optional.of(data);
            this.realResponse = Optional.of(response);

            lazyProxyPromises.values().stream().filter(p -> !p.callbackExecuted && realRequest.isPresent()).forEach(lazyPromise -> {
                final RealRequest realReq = realRequest.get();
                final CacheResp cacheResp = new CacheResp(true);
                if (realReq.realCommand == lazyPromise.hysterixCommand) {
                    logger.debug("real success, caching resp -> false:command:" + realReq.realCommand.getCommandKey());
                    cacheResp.cacheHit = false;
                }
                cacheResp.data = data;
                lazyPromise.callbackExecuted = true;
                lazyPromise.scalaPromise.success(cacheResp);
            });
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

        private scala.concurrent.Promise asScalaPromise(final String requestId) {
            return lazyProxyPromises.get(requestId).scalaPromise;
        }

    }

    private static class RealRequest {

        private F.Promise realPromise;
        private HysterixCommand realCommand;
        private String requestId;

        private RealRequest(HysterixCommand realCommand, F.Promise realPromise, String requestId) {
            this.realPromise = realPromise;
            this.realCommand = realCommand;
            this.requestId = requestId;
        }

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
