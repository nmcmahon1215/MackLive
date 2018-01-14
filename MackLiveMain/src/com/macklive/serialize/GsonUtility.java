package com.macklive.serialize;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.*;

public class GsonUtility {

    private static Gson gs;

    public static Gson getGson() {
        if (gs == null) {
            gs = new GsonBuilder()
                    .registerTypeAdapter(Long.class, (JsonSerializer<Long>) (src, typeOfSrc, context) -> new JsonPrimitive(Long.toString(src)))
                    .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> new JsonPrimitive(Long.toString(src.getTime())))
                    .create();
        }
        return gs;
    }
}
