package com.sandbox.runtime.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by nickhoughton on 6/08/2014.
 */
@Component
public class MapUtils {

    public Map<String, String> flattenMultiValue(Map<String, ?> multiMap, String... keysToIgnore) {

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
                    valuesList = CollectionUtils.arrayToList(keyValue);
                }else{
                    valuesList = (List)keyValue;
                }

                for (Object value : valuesList){
                    if(value instanceof String){
                        results.put(key, (String) value);
                    }else{
                        results.put(key, value.toString());
                    }
                }

            }else{
                if(keyValue instanceof String){
                    results.put(key, (String) keyValue);
                }else{
                    results.put(key, keyValue.toString());
                }
            }
        }

        return results;

    }

}
