package com.sandbox.worker.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sandbox.worker.models.enums.ActivityMessageTypeEnum;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "messageType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RequestActivityMessage.class, name = "request"),
        @JsonSubTypes.Type(value = LogActivityMessage.class, name = "log"),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id","createdTimestamp","sandboxId"})
public abstract class ActivityMessage {

    @JsonIgnore
    private String id;

    @ApiModelProperty(value = "Epoch time in milliseconds when the message was created")
    private long createdTimestamp = System.currentTimeMillis();

    @ApiModelProperty(
            hidden = true,
            name = "messageId",
            value = "A unique identifier for the activity message"
    )
    private String messageId;

    private ActivityMessageTypeEnum messageType;

    @ApiModelProperty(value = "The ID of the sandbox that generated this message")
    private String sandboxId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public ActivityMessageTypeEnum getMessageType() {
        return messageType;
    }

    public void setMessageType(ActivityMessageTypeEnum messageType) {
        this.messageType = messageType;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

}
