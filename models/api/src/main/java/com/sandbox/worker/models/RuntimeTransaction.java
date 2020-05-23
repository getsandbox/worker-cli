package com.sandbox.worker.models;

import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.List;

public class RuntimeTransaction {

    @ApiModelProperty(value = "The source sandbox name.")
    String sandboxName;

    RuntimeRequest request;
    List<RuntimeResponse> responses;
    String consoleOutput;

    public RuntimeTransaction() {
    }

    public RuntimeTransaction(RuntimeRequest request, String consoleOutput, RuntimeResponse... responses) {
        this(request, consoleOutput, responses.length == 0 || responses[0] == null ? null : Arrays.asList(responses));
    }

    public RuntimeTransaction(RuntimeRequest request, String consoleOutput, List<RuntimeResponse> responses) {
        this.sandboxName = request.getSandboxName();
        this.request = request;
        this.consoleOutput = consoleOutput;
        this.responses = responses;

        //calculate duration
        if (this.responses != null) {
            for (RuntimeResponse response : this.responses){
                if(response.getRespondedTimestamp() != null){
                    response.setDurationMillis(response.getRespondedTimestamp() - request.getReceivedTimestamp());
                }
            }
        }
    }

    public String getSandboxName() {
        return sandboxName;
    }

    public RuntimeRequest getRequest() {
        return request;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }

    public List<RuntimeResponse> getResponses() {
        return responses;
    }

}
