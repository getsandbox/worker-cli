package com.sandbox.runtime.js.models;

/**
 * Created by drew on 6/08/2014.
 */

import com.sandbox.runtime.models.HTTPResponse;

/**
 * Interface for the callback in Sandbox.define
 * Nashorn / Java converts the anonymous callback function
 * into anonymous class and assigns the callback to the single
 * defined method on the interface.
 */
public interface ISandboxDefineCallback {
    public void run(Object req, HTTPResponse res);
}
