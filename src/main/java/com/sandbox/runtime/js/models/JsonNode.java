package com.sandbox.runtime.js.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.runtime.config.Context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by drew on 2/08/2014.
 */
public class JsonNode {

    static {
        mapper = Context.getObjectMapper();
    }

    static ObjectMapper mapper;

    private Object jsonObject;

    public JsonNode(String json) throws Exception {

        // sanity check
        String trimmedValue = json.trim();
        if (json == null || "".equals(trimmedValue)) {
            jsonObject = new HashMap<String, Object>();
        } else {
            try {
                //bit crap, but some incoming json is encoded already,
                if(trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) json = mapper.writeValueAsString(mapper.readValue(trimmedValue, JsonNode.class));

                //is either JSON, or crappy escaped JSON
                if (json.startsWith("{")){

                    TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
                    jsonObject = mapper.readValue(json, typeRef);

                } else if(json.startsWith("[")){

                    TypeReference<ArrayList<Object>> typeRef = new TypeReference<ArrayList<Object>>() {};
                    jsonObject = mapper.readValue(json, typeRef);
                }else{
                    jsonObject = new HashMap();
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

    public Object getJsonObject() {
        return jsonObject;
    }

}
