package com.macklive.objects;

import com.google.appengine.api.datastore.Key;
import com.google.gson.Gson;

public abstract class AbsBusinessObject implements IBusinessObject {

    private Gson gs = new Gson();
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

}
