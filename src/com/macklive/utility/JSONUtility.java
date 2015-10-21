package com.macklive.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class for quickly creating JSON strings as return values for REST calls
 */
public class JSONUtility {

    Map<String, Object> values;
    
    public JSONUtility(){
        values = new HashMap<String, Object>();
    }
    
    /**
     * Adds a property to the JSON object.
     * @param key Key for the object.
     * @param value Value for the given key
     */
    public void addProperty(String key, Object value){
        values.put(key, value);
    }
    
    /**
     * Generates the JSON string.
     * @return A JSON representation of the Keys and values of the map
     */
    public String getJSON(){
        Set<String> keyset = values.keySet();
        String result = "{ ";
        for (String key : keyset){
            result += "\"" + key + "\":";
            Object val = values.get(key);
            result += "\"" + val.toString() + "\",";
        }
        result += " }";
        return result;
    }
    
    /**
     * Defaults the string output to be the JSON output.
     * @return a string representation of the current properties
     */
    public String toString(){
        return this.getJSON();
    }
}
