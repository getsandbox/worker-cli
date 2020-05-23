package com.sandbox.worker.models.interfaces;

import com.sandbox.worker.models.ActivityMessage;

public interface ActivityStore {
    void add(ActivityMessage message);
}
