package com.sandbox.runtime.js.models;

import java.util.ArrayList;
import java.util.List;

public class Console {

    public Console() {
    }

    public Console(Console existingConsole) {
        this.messages = new ArrayList<>(messages);
    }

    private List<String> messages = new ArrayList<String>();

    public void write(String msg) {
        messages.add(msg);
    }

    public void clear() { messages.clear(); }

    public List<String> getMessages(){
        return messages;
    }
}