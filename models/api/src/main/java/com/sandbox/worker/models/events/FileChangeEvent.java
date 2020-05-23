package com.sandbox.worker.models.events;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class FileChangeEvent extends SandboxChangeEvent {

    private static final String type = "file_change_event";

    @NotNull(message="Repository ID is required")
    private String repoId;

    private List<String> createdFiles;

    private List<String> updatedFiles;

    private List<String> deletedFiles;

    public FileChangeEvent() {
    }

    public FileChangeEvent(String repoId, ChangeSource changeSource) {
        super(repoId, changeSource);
        this.repoId = repoId;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public List<String> getCreatedFiles() {
        if(createdFiles == null) createdFiles = new ArrayList<>();
        return createdFiles;
	}

	public void setCreatedFiles(List<String> createdFiles) {
		this.createdFiles = createdFiles;
	}

    public List<String> getUpdatedFiles() {
        if(updatedFiles == null) updatedFiles = new ArrayList<>();
        return updatedFiles;
    }

    public void setUpdatedFiles(List<String> updatedFiles) {
        this.updatedFiles = updatedFiles;
    }

    public List<String> getDeletedFiles() {
        if(deletedFiles == null) deletedFiles = new ArrayList<>();
        return deletedFiles;
    }

    public void setDeletedFiles(List<String> deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

    @Override
    public String getType() {
        return type;
    }
}
