package com.sandbox.runtime.services;

import com.sandbox.runtime.models.ActivityMessage;
import com.sandbox.runtime.models.ActivityMessageTypeEnum;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InMemoryActivityStoreTest {

    @Test
    public void testStoreMaximum() throws Exception {
        InMemoryActivityStore store = new InMemoryActivityStore(100);
        for (int x=0; x < 200; x++){
            store.add(new ActivityMessage("1", ActivityMessageTypeEnum.log, "message"));
        }

        assertEquals(100, store.getAll().size());

    }
}