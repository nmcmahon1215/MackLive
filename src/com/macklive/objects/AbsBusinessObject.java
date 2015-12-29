package com.macklive.objects;

import com.google.gson.Gson;

public abstract class AbsBusinessObject implements IBusinessObject {

    private Gson gs = new Gson();

    @Override
    public String toJSON() {
        return gs.toJson(this);
    }

}
