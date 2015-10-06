package com.sandbox.runtime.js.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickhoughton on 23/10/2014.
 */
public class FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private static Map<String, String> cachedResources = new HashMap<>();

    public static String loadJSFromResource(String jsFile) {
        if(cachedResources.containsKey(jsFile)) return cachedResources.get(jsFile);

        try {
            InputStream is = new ClassPathResource("/com/sandbox/runtime/js/" + jsFile + ".js").getInputStream();
            String contents = IOUtils.toString(is);
            cachedResources.put(jsFile, contents);

            return contents;

        } catch (IOException e) {
            logger.error("Error retrieving JS resource",e);
            return null;
        }
    }

}
