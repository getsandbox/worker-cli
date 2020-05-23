package com.sandbox.worker;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapUtils {

    public static Map<String, String> flattenMultiValue(Map<String, ?> multiMap, String... keysToIgnore) {
        if(multiMap == null) return null;

        Map<String,String> results = new HashMap<String, String>();
        List keysToIgnoreList = Arrays.asList(keysToIgnore);

        Set<String> keys = multiMap.keySet();
        for (String key : keys){
            //skip the final match rubbish, dont want it.
            if(keysToIgnoreList.contains(key)) continue;

            Object keyValue = multiMap.get(key);
            if(keyValue.getClass().isArray() || keyValue instanceof List){
                //turn array values into a list from whatever it is in atm
                List valuesList = null;
                if(!(keyValue instanceof List)){
                    valuesList = Arrays.asList((Object[])keyValue);
                }else{
                    valuesList = (List)keyValue;
                }

                for (Object value : valuesList){
                    if(value instanceof String){
                        results.put(key, decodedURIValues((String) value));
                    }else{
                        results.put(key, decodedURIValues(value.toString()));
                    }
                }

            }else{
                if(keyValue instanceof String){
                    results.put(key, decodedURIValues((String) keyValue));
                }else{
                    results.put(key, decodedURIValues(keyValue.toString()));
                }
            }
        }

        return results;

    }

    private static String decodedURIValues(String value){
        try {
            return (value == null || value.isEmpty()) ? value : URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

}
