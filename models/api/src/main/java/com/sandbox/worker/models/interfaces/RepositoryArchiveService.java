package com.sandbox.worker.models.interfaces;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface RepositoryArchiveService {

    void set(String fullSandboxId, InputStream zipArchive);

    InputStream getStream(String fullSandboxId);

    boolean exists(String fullSandboxId);

    File getUnpackedDirectory(String fullSandboxId) throws IOException;
}
