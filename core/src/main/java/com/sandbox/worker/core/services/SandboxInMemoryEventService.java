package com.sandbox.worker.core.services;

import com.sandbox.worker.models.events.Event;
import com.sandbox.worker.models.interfaces.SandboxEventEmitterService;

public class SandboxInMemoryEventService extends InMemoryEventService<Event> implements SandboxEventEmitterService {
}
