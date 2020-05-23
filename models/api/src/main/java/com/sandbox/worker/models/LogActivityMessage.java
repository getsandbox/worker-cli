package com.sandbox.worker.models;

import com.sandbox.worker.models.enums.ActivityMessageTypeEnum;
import io.swagger.annotations.ApiModelProperty;

public class LogActivityMessage extends ActivityMessage {

    @ApiModelProperty(value = "The details of the message when type is 'log'")
    private String message;

    public LogActivityMessage() {
    }

    public LogActivityMessage(String sandboxId, String message) {
        setSandboxId(sandboxId);
        setMessageType(ActivityMessageTypeEnum.log);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
