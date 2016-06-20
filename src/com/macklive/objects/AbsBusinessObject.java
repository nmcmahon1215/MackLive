package com.macklive.objects;

import com.google.appengine.api.datastore.Key;
import com.google.gson.Gson;

public abstract class AbsBusinessObject implements IBusinessObject {

    protected Gson gs = GsonUtility.getGson();
    protected Key key;

    @Override
    public String toJSON() {
        return gs.toJson(this);
    }

    @Override
    public void setKey(Key k) {
        this.key = k;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public boolean hasOwner() {
        return true;
    }

}
