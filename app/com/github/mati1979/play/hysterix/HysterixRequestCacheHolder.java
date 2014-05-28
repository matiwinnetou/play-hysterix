package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;

public class HysterixRequestCacheHolder {

    private static final play.Logger.ALogger logger = play.Logger.of(HysterixRequestCacheHolder.class);

    private Map<String, HysterixHttpRequestsCache> caches = Maps.newConcurrentMap();

    public HysterixHttpRequestsCache getOrCreate(final String commandKey) {
        HysterixHttpRequestsCache requestCache = caches.get(commandKey);

        if (requestCache == null) {
            logger.debug("requestCache for key is empty,key:" + commandKey);
            requestCache = new HysterixHttpRequestsCache();
        }

        caches.put(commandKey, requestCache);

        return requestCache;
    }

}
