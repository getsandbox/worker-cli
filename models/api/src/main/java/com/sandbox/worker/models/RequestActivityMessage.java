package com.sandbox.worker.models;

import com.sandbox.worker.models.enums.ActivityMessageTypeEnum;
import io.swagger.annotations.ApiModelProperty;

public class RequestActivityMessage extends ActivityMessage {

    @ApiModelProperty(hidden = false, name="messageObject", value = "The details of the message when type is 'request'")
    private Object runtimeTransaction;

    public RequestActivityMessage() {
    }

    //TODO Tighten this up after migration
    public RequestActivityMessage(String sandboxId, Object runtimeTransaction) {
        setSandboxId(sandboxId);
        setMessageType(ActivityMessageTypeEnum.request);
        this.runtimeTransaction = runtimeTransaction;
    }

    public Object getRuntimeTransaction() {
        return runtimeTransaction;
    }

}
