package com.sandbox.worker.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;

public class JSONUtils {

    public static Object parse(ObjectMapper mapper, String json) throws Exception {
        // sanity check
        String trimmedValue = json.trim();
        if (json == null || "".equals(trimmedValue)) {
            return new HashMap<String, Object>();
        } else {
            try {
                //bit crap, but some incoming json is encoded already,
                if(trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) json = mapper.writeValueAsString(mapper.readValue(trimmedValue, JsonNode.class));

                //is either JSON, or crappy escaped JSON
                if (json.startsWith("{")){
                    TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
                    return mapper.readValue(json, typeRef);

                } else if(json.startsWith("[")){

                    TypeReference<ArrayList<Object>> typeRef = new TypeReference<ArrayList<Object>>() {};
                    return mapper.readValue(json, typeRef);
                }else{
                    return new HashMap();
                }

            } catch (Exception e) {
                // parse error
                //                jsonObject = new JSParseBodyError("Failed to parse json body: " + e.getMessage(), "", json);
                System.out.println("Json body parse exception: " + e.getMessage());
                //                jsonObject = new HashMap<String, Object>();
                throw new Exception("Failed to parse json body");
            }
        }
    }

}
