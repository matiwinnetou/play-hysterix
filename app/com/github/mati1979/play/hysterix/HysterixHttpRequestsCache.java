package com.github.mati1979.play.hysterix;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import play.libs.F;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
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

        clientsGroup.lazyProxyPromises.put(requestId, createLazyPromise());
        clientsGroup.hystrixCommands.add(command);

        groupIdToClients.put(clientsGroupId, clientsGroup);

        return this;
    }

    public HysterixHttpRequest createRequest(final HysterixCommand command) {
        return HysterixHttpRequest.create(this, command);
    }

    public synchronized F.Promise execute(final String requestId) {
        final ClientsGroup clientsGroup = requestIdToClients.get(requestId);

        if (clientsGroup.isCompleted()) {
            logger.debug("isCompleted, groupId:" + clientsGroup.groupId);
            if (clientsGroup.successValue.isPresent()) {
                logger.debug("isCompleted, success:" + clientsGroup.groupId);
                return F.Promise.pure(clientsGroup.successValue.get());
            }
            if (clientsGroup.failureValue.isPresent()) {
                logger.debug("isCompleted, failure:" + clientsGroup.groupId);
                return F.Promise.throwing(clientsGroup.failureValue.get());
            }
        }

        if (clientsGroup.realPromise.isPresent()) {
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

        final HysterixCommand next = clientsGroup.hystrixCommands.iterator().next();
        clientsGroup.stopwatch.start();
        final F.Promise realResponseP = next.callRemote();
        clientsGroup.realPromise = Optional.of(realResponseP);

        realResponseP.onRedeem(response -> clientsGroup.redeemSuccess(response));
        realResponseP.onFailure(t -> clientsGroup.redeemFailure((Throwable)t));

        groupIdToClients.put(clientsGroup.groupId, clientsGroup);

        return clientsGroup.getLazyPromise(requestId);
    }

    private scala.concurrent.Promise createLazyPromise() {
        return scala.concurrent.Promise$.MODULE$.apply();
    }

    private static class ClientsGroup {

        private final String groupId;
        private Optional<F.Promise> realPromise = Optional.empty();
        private Collection<HysterixCommand> hystrixCommands = Lists.newArrayList();
        private Map<String, scala.concurrent.Promise> lazyProxyPromises = Maps.newHashMap();
        private Optional<Object> successValue = Optional.empty();
        private Optional<Throwable> failureValue = Optional.empty();

        private Stopwatch stopwatch = new Stopwatch();

        private ClientsGroup(final String groupId) {
            this.groupId = groupId;
        }

        public F.Promise getLazyPromise(final String requestId) {
            return F.Promise.wrap(asScalaPromise(requestId).future());
        }

        public boolean isCompleted() {
            return successValue.isPresent() || failureValue.isPresent();
        }

        private void redeemSuccess(final Object data) {
            this.successValue = Optional.of(data);
            stopwatch.stop();
            lazyProxyPromises.values().stream().forEach(p -> p.success(data));
        }

        private void redeemFailure(final Throwable t) {
            this.failureValue = Optional.of(t);
            stopwatch.stop();
            lazyProxyPromises.values().stream().forEach(p -> p.failure(t));
        }

        private scala.concurrent.Promise asScalaPromise(final String requestId) {
            return lazyProxyPromises.get(requestId);
        }

    }

}
