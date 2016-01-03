package com.macklive.objects;

/**
 * Describes an object that can be stored in the cache.
 */
public abstract class AbsCacheableObject extends AbsBusinessObject
        implements IBusinessObject, ICacheableObject {


    @Override
    public boolean isCacheable() {
        return true;
    }
}
