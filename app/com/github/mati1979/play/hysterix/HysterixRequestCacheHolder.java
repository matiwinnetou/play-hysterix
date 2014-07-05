package com.github.mati1979.play.hysterix;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HysterixRequestCacheHolder {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestCacheHolder.class);

    private Map<String, HysterixHttpRequestsCache> caches = new ConcurrentHashMap<>();

    public <T> HysterixHttpRequestsCache<T> getOrCreate(final String requestCacheKey) {
        return caches.computeIfAbsent(requestCacheKey, k -> new HysterixHttpRequestsCache<T>(k));
    }

}
