package com.macklive.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.macklive.objects.IBusinessObject;

/**
 * A class for quickly creating JSON strings as return values for REST calls
 */
public class JSONUtility {

    private Map<String, Object> values;
    
    public JSONUtility(){
        values = new HashMap<String, Object>();
    }
    
    /**
     * Returns a JSON Utility representation of the given object.
     * @param obj Object to convert into a JSONUtility
     * @return A JSONUtiltiy filled with the appropriate properties to represent the object.
     */
    public static JSONUtility build(IBusinessObject obj){
        Entity e = obj.getEntity();
        JSONUtility jsu = new JSONUtility();
        for (String field : e.getProperties().keySet()){
            jsu.addProperty(field, e.getProperty(field));
        }
        jsu.addProperty("id", e.getKey().getId());
        return jsu;
    };
    
    /**
     * Overrides getJSON to allow specifying a Key. This avoids the need 
     * to lookup an item in the data store in some cases.
     * @param obj Object to convert into a JSONUtility
     * @param key Key used as an identifier for the object in the data store.
     * @return A JSONUtility filled with the appropriate properties to represent the Object.
     */
    public static JSONUtility build(IBusinessObject obj, Key key){
        JSONUtility jsu = JSONUtility.build(obj);
        jsu.addProperty("id", key.getId());
        return jsu;
    }
    
    /**
     * Adds a property to the JSON object.
     * @param key Key for the object.
     * @param value Value for the given key
     */
    public void addProperty(String key, Object value){
        if (value != null){
            values.put(key, value);
        }
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
            if (val instanceof JSONUtility || val instanceof Collection){
                result += val.toString();
            } else {
                result += "\"" + val.toString() + "\"";
            }
            
            result += ", ";
        }
        result = result.substring(0, result.length() - 2);
        result += " }";
        return result;
    }
    
    /**
     * Defaults the string output to be the JSON output.
     * @return a string representation of the current properties
     */
    @Override
    public String toString(){
        return this.getJSON();
    }
}
