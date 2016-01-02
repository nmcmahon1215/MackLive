package com.macklive.objects;

import com.google.gson.Gson;

public class GsonUtility {

    private static Gson gs;

    public static Gson getGson() {
        if (gs == null){
            gs = new Gson();
        }
        return gs;
    }
}
