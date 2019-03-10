package com.sandbox.runtime.config;

import com.sandbox.runtime.models.config.RuntimeConfig;

import org.junit.Test;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.io.ClassPathResource;

public class CLIBootstrapTest {

    @Test
    public void testConfig() throws Exception {
        String[] args = new String[]{"--config=" + new ClassPathResource("/runtimeConfig.json").getFile()};
        RuntimeConfig config = CLIBootstrap.getConfig(new SimpleCommandLinePropertySource(args));
        config.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingConfig() throws Exception {
        String[] args = new String[]{"--config=/tmp/junk.json"};
        CLIBootstrap.getConfig(new SimpleCommandLinePropertySource(args));
    }
}
