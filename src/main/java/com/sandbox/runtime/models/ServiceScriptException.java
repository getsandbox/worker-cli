package com.sandbox.runtime.models;

/**
 * Created by drew on 7/08/2014.
 */
public class ServiceScriptException extends Exception {

    String filename;
    int lineNumber = -1;
    int columnNumber = -1;
    public ServiceScriptException() {
    }

    public ServiceScriptException(Exception cause, String filename, int line, int column) {
        super(cause.getMessage(),cause);
        this.filename = filename;
        this.lineNumber = line;
        this.columnNumber = column;

    }

    public ServiceScriptException(String message) {
        //bit crap, need a utility to strip out JS error names from error messages
        super(message.startsWith("Error:") ? message.substring(7) : message);
    }

    public ServiceScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceScriptException(Throwable cause) {
        super(cause);
    }

    public ServiceScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getFilename() {
        return filename;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public String getMessage() {
        if (filename == null){
            return String.format("%1$s", super.getMessage());
        }else if(lineNumber <= 0 && columnNumber <= 0){
            return String.format("%1$s: %2$s",filename, super.getMessage());
        }else if(lineNumber > 0 && columnNumber <= 0){
            return String.format("%1$s:%2$s %3$s",filename, lineNumber, super.getMessage());
        }else{
            return String.format("%1$s:%2$s:%3$s %4$s",filename, lineNumber, columnNumber, super.getMessage());
        }
    }

    @Override
    public String toString() {
        return getMessage();
    }
}