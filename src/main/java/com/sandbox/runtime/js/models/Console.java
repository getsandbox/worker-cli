package com.sandbox.runtime.js.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drew on 30/07/2014.
 */
public class Console {

    private List<String> _messages = new ArrayList<String>();

    public void write(String msg) {
        _messages.add(msg);
    }

    public List<String> _getMessages(){
        return _messages;
    }
}