package com.macklive.objects;

import com.google.gson.Gson;

public class BusinessObjectFactory {

    private static Gson gs = new Gson();

    private BusinessObjectFactory() {

    }

    public static AbsBusinessObject getBusinessObject(String JSON,
            Class<?> type) {
        return gs.fromJson(JSON, type);
    }
}
