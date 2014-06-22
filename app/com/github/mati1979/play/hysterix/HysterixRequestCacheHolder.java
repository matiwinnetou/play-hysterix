package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class HysterixRequestCacheHolder {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestCacheHolder.class);

    private Map<String, HysterixHttpRequestsCache> caches = Maps.newHashMap();

    private ReentrantLock lock = new ReentrantLock(true);

    public <T> HysterixHttpRequestsCache<T> getOrCreate(final String requestCacheKey) {
        try {
            lock.lock();
            final HysterixHttpRequestsCache<T> requestCache = caches.getOrDefault(requestCacheKey, new HysterixHttpRequestsCache<T>(requestCacheKey));
            caches.put(requestCacheKey, requestCache);

            return requestCache;
        } finally {
            lock.unlock();
        }
    }

}
