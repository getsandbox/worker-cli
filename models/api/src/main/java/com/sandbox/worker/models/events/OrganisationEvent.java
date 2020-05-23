package com.sandbox.worker.models.events;

public abstract class OrganisationEvent extends Event {

    private String orgId;

    public OrganisationEvent() {
    }

    public OrganisationEvent(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
