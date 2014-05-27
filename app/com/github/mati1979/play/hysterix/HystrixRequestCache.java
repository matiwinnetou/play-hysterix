package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

/**
 * Created by mati on 26/05/2014.
 */
public class HystrixRequestCache {

    private Map<String, Object> cache = Maps.newConcurrentMap();

    public Object put(final String cacheKey, Object value) {
        return cache.putIfAbsent(cacheKey, value);
    }

    public Optional<Object> get(final String cacheKey) {
        return Optional.ofNullable(cache.get(cacheKey));
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }

}
