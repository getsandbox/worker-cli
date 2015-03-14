package com.sandbox.runtime.converters;

import java.util.regex.Pattern;

/**
 * Created by nickhoughton on 13/03/2015.
 */
public abstract class RequestConverter {

    Pattern jsonPattern = Pattern.compile("^application\\/([\\w!#\\$%&\\*`\\-\\.\\^~]*\\+)?json.*$", Pattern.CASE_INSENSITIVE);
    Pattern xmlPattern = Pattern.compile("(text\\/xml|application\\/([\\w!#\\$%&\\*`\\-\\.\\^~]+\\+)?xml).*$", Pattern.CASE_INSENSITIVE);

    public Pattern getJsonPattern() {
        return jsonPattern;
    }

    public Pattern getXmlPattern() {
        return xmlPattern;
    }

}
