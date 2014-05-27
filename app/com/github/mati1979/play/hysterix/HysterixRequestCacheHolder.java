package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;

public class HysterixRequestCacheHolder {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestCacheHolder.class);

    private Map<String, HysterixRequestCache> caches = Maps.newConcurrentMap();

    public <T> HysterixRequestCache<T> getOrCreate(final String key) {
        HysterixRequestCache requestCache = caches.get(key);

        if (requestCache == null) {
            logger.debug("requestCache for key is empty,key:" + key);
            requestCache = new HysterixRequestCache<T>();
        }

        caches.put(key, requestCache);

        logger.debug("cache.size:" + caches.size());

        return requestCache;
    }

}
