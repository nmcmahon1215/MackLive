package com.macklive.serialize;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtility {

    private static Gson gs;

    public static Gson getGson() {
        if (gs == null) {
            gs = new GsonBuilder().registerTypeAdapter(Date.class, new DateAdapter()).create();
        }
        return gs;
    }
}
