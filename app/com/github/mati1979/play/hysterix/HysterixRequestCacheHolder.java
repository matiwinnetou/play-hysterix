package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;

public class HysterixRequestCacheHolder {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestCacheHolder.class);

    private Map<String, HysterixHttpRequestsCache> caches = Maps.newConcurrentMap();

    public HysterixHttpRequestsCache getOrCreate(final String cacheKey) {
        HysterixHttpRequestsCache requestCache = caches.get(cacheKey);

        if (requestCache == null) {
            logger.debug("requestCache for key is empty,key:" + cacheKey);
            requestCache = new HysterixHttpRequestsCache(cacheKey);
        }

        caches.put(cacheKey, requestCache);

        return requestCache;
    }

}
