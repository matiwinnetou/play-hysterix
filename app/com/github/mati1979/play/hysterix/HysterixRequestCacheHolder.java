package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;

public class HysterixRequestCacheHolder {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestCacheHolder.class);

    private Map<String, HysterixHttpRequestsCache> caches = Maps.newConcurrentMap();

    public synchronized <T> HysterixHttpRequestsCache<T> getOrCreate(final String requestCacheKey) {
        final HysterixHttpRequestsCache<T> requestCache = caches.getOrDefault(requestCacheKey, new HysterixHttpRequestsCache<T>(requestCacheKey));
        caches.put(requestCacheKey, requestCache);

        return requestCache;
    }

}
