package com.github.mati1979.play.hysterix;

import play.libs.F;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mati on 24/05/2014.
 */
public class HysterixHttpRequestsCache<T> {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixHttpRequestsCache.class);

    private final String requestCacheKey;

    private AtomicReference<Optional<F.Promise<T>>> promise = new AtomicReference<>(Optional.empty());

    public HysterixHttpRequestsCache(final String requestCacheKey) {
        this.requestCacheKey = requestCacheKey;
    }

    //we can assume that commands coming here are already properly grouped commands
    public F.Promise<CacheResp<T>> execute(final HysterixCommand<T> command) {
        if (shouldNotCache(command)) {
            return command.callRemote().map(data -> new CacheResp(data, false));
        }

        return promise.get().map(dataP -> dataP.map(data -> new CacheResp<>(data, true)))
               .orElse(command.callRemote().map(data -> new CacheResp<>(data, false)));
    }

    private boolean shouldNotCache(final HysterixCommand<T> command) {
        return !command.getCacheKey().isPresent();
    }

    public static class CacheResp<T> {

        private T data;
        private boolean cacheHit = false;

        public CacheResp(final T data, final boolean cacheHit) {
            this.data = data;
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
