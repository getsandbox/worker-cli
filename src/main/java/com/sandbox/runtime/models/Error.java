package com.sandbox.runtime.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used as a standard way to represent an error payload.
 * 
 * @author jayden
 * 
 */
@JsonInclude(Include.NON_NULL)
public class Error {
    private String displayMessage;
    private String detailedMessage;
    private String field;

    public Error() {
    }

    //display message is the few words error message shown to users
    //detailed message an optional amount of extra detail about the error, not generally shown to a UI user. Example is a JS validation error that is the failed code snippet
    //optional field if its a DTO validation error it would have an offending field name.
    public Error(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public Error(String displayMessage, String detailedMessage) {
        this.displayMessage = displayMessage;
        this.detailedMessage = detailedMessage;
    }

    public Error(String displayMessage, String detailedMessage, String field) {
        this.displayMessage = displayMessage;
        this.detailedMessage = detailedMessage;
        this.field = field;
    }

    @JsonProperty("detailed_message")
    public String getDetailedMessage() {
        return detailedMessage;
    }

    @JsonProperty("detailed_message")
    public void setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
    }

    @JsonProperty("message")
    public String getDisplayMessage() {
        return displayMessage;
    }

    @JsonProperty("message")
    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }

}
