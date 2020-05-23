package com.sandbox.worker.core.js.models;

import com.sandbox.worker.models.enums.RuntimeVersion;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;

import static com.sandbox.worker.models.enums.RuntimeVersion.*;

public class WorkerVersionContext {

    private RuntimeVersion version;
    private String name;
    private String librariesPath;
    private List<ImmutablePair<String, String>> libraryNames;
    private Map<String, String> optionOverrides;

    public WorkerVersionContext(RuntimeVersion version, String name, String librariesPath, List<ImmutablePair<String, String>> libraryNames, Map<String, String> optionOverrides) {
        this.version = version;
        this.name = name;
        this.librariesPath = librariesPath;
        this.libraryNames = libraryNames;
        this.optionOverrides = optionOverrides;
    }

    public RuntimeVersion getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getLibrariesPath() {
        return librariesPath;
    }

    public List<ImmutablePair<String, String>> getLibraryNames() {
        return libraryNames;
    }

    public Map<String, String> getOptionOverrides() {
        return optionOverrides;
    }

    public static WorkerVersionContext get(RuntimeVersion version) {
        switch (version) {
            case VERSION_1:
                return new WorkerVersionContext(VERSION_1, "version_1",
                        "com/sandbox/runtime/v2/js/version_1/",
                        Arrays.asList(new ImmutablePair("amanda", "amanda-0.4.8.min.js"), new ImmutablePair("_", "lodash-2.4.1.min.js"), new ImmutablePair("moment", "moment-2.8.2.min.js"), new ImmutablePair("validator", "validator.min.js")),
                        new HashMap() {{
                            put("js.ecmascript-version", "6");
                        }});

            case VERSION_2:
                return new WorkerVersionContext(VERSION_2, "version_2", "com/sandbox/runtime/v2/js/version_2/",
                        Arrays.asList(new ImmutablePair("amanda", "amanda-0.4.8.min.js"), new ImmutablePair("_", "lodash-4.2.1.min.js"), new ImmutablePair("moment", "moment-2.11.2.min.js"), new ImmutablePair("validator", "validator-4.7.2.min.js")),
                        new HashMap() {{
                            put("js.ecmascript-version", "6");
                        }});

            case VERSION_3:
                return new WorkerVersionContext(VERSION_3, "version_3", "com/sandbox/runtime/v2/js/version_3/",
                        Arrays.asList(new ImmutablePair("Ajv", "ajv-6.10.0.min.js"), new ImmutablePair("_", "lodash-4.17.11.min.js"), new ImmutablePair("moment", "moment-2.24.0.min.js"), new ImmutablePair("validator", "validator-10.11.0.min.js")),
                        new HashMap() {{
                            put("js.ecmascript-version", "11");
                        }});
        }

        throw new IllegalArgumentException("Unsupported version:" + version);
    }
}
