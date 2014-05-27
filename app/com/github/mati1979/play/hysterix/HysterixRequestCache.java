package com.github.mati1979.play.hysterix;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

/**
 * Created by mati on 26/05/2014.
 */
public class HysterixRequestCache<T> {

    private Map<String, T> cache = Maps.newConcurrentMap();

    public void put(final String cacheKey, T value) {
        cache.putIfAbsent(cacheKey, value);
    }

    public Optional<T> get(final String cacheKey) {
        return Optional.ofNullable(cache.get(cacheKey));
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }

}
