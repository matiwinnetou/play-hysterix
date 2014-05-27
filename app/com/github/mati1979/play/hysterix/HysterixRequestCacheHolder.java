package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by mszczap on 27.05.14.
 */
public class HysterixRequestCacheHolder {

    private Map<String, HysterixRequestCache> caches = Maps.newConcurrentMap();

    public <T> HysterixRequestCache<T> getOrCreate(final String key) {
        final HysterixRequestCache requestCache = caches.getOrDefault(key, new HysterixRequestCache<T>());

        caches.putIfAbsent(key, requestCache);

        return requestCache;
    }

}
