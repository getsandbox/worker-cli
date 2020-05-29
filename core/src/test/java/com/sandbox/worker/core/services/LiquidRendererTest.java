package com.sandbox.worker.core.services;

import com.sandbox.worker.core.js.ScriptFunctions;
import com.sandbox.worker.models.SandboxIdentifier;
import com.sandbox.worker.models.interfaces.RepositoryService;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiquidRendererTest {

    @Test
    void shouldRenderWithCustomInclude() {
        LiquidRenderer renderer = new LiquidRenderer(1024*1024);
        Map<String, Object> context = new HashMap<>();
        context.put("__service", new ScriptFunctions(new SandboxIdentifier("1"), new RepositoryService() {
            @Override
            public String getRepositoryFile(String fullSandboxId, String filename) {
                return "{{ template }} template!";
            }

            @Override
            public boolean hasRepositoryFile(String fullSandboxId, String filename) {
                return true;
            }
        }));
        String result = renderer.render("template", () -> "{% include 'template' with 'true' %}", context);
        assertEquals("true template!", result);
    }
}
