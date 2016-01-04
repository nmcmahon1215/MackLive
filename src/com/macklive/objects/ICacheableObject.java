package com.macklive.objects;

import com.macklive.storage.CacheManager;

/**
 * Describes a cacheable object.
 */
public interface ICacheableObject {

    /**
     * Caches the object.
     */
    public void cache(CacheManager cm);
}
