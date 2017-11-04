package com.sandbox.runtime.js.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NashornUtils {

    protected  static Logger logger = LoggerFactory.getLogger(NashornUtils.class);

    public abstract String readFile(String filename);

    public abstract boolean hasFile(String filename);

}
