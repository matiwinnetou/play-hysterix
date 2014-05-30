package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;

public class HysterixRequestCacheHolder {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestCacheHolder.class);

    private Map<String, HysterixHttpRequestsCache> caches = Maps.newConcurrentMap();

    public HysterixHttpRequestsCache getOrCreate(final String requestCacheKey) {
        HysterixHttpRequestsCache requestCache = caches.get(requestCacheKey);

        if (requestCache == null) {
            logger.debug("requestCache for key is empty,key:" + requestCacheKey);
            requestCache = new HysterixHttpRequestsCache(requestCacheKey);
        }

        caches.put(requestCacheKey, requestCache);

        return requestCache;
    }

}
