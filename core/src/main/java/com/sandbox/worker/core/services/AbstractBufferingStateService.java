package com.sandbox.worker.core.services;

import com.sandbox.worker.models.interfaces.BufferingStateService;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBufferingStateService implements BufferingStateService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBufferingStateService.class);

    private final Map<String, BufferedItem> bufferedValues = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "stateBuffer"));
    private ScheduledFuture writeFuture;
    private int writeDelaySeconds = 0;

    public void start(int writeIntervalSeconds, int writeDelaySeconds) {
        LOG.info("Starting flush on interval: {} with delay: {}", writeIntervalSeconds, writeDelaySeconds);
        writeFuture = executor.scheduleAtFixedRate(() -> flush(), writeDelaySeconds, writeIntervalSeconds, TimeUnit.SECONDS);
    }

    public void flush() {
        //loop over all buffered entries
        for (Iterator<Map.Entry<String, BufferedItem>> it = bufferedValues.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, BufferedItem> entry = it.next();
            String key = entry.getKey();
            BufferedItem value = entry.getValue();
            //check buffered entry time versus configured delay
            if (writeDelaySeconds == 0 || (System.currentTimeMillis() - value.getBufferedTime()) > writeDelaySeconds * 1000) {
                //state been buffered for long enough
                try {
                    setSandboxState(key, value.supplier.get());
                    it.remove();
                } catch (IllegalStateException e) {
                    LOG.warn("Error writing state for {}, invalid supplier?\n{}", key, e);
                }
            }
        }
    }

    @Override
    public void flush(String sandboxId) {
        BufferedItem item = bufferedValues.remove(sandboxId);
        if (item != null) {
            setSandboxState(sandboxId, item.getSupplier().get());
        }
    }

    @Override
    public void notifyPossibleChange(String sandboxId, Supplier<String> supplier) {
        bufferedValues.put(sandboxId, new BufferedItem(sandboxId, supplier));
    }

    public static class BufferedItem {
        String sandboxId;
        Supplier<String> supplier;
        long bufferedTime = System.currentTimeMillis();

        public BufferedItem(String sandboxId, Supplier<String> supplier) {
            this.sandboxId = sandboxId;
            this.supplier = supplier;
        }

        public String getSandboxId() {
            return sandboxId;
        }

        public Supplier<String> getSupplier() {
            return supplier;
        }

        public long getBufferedTime() {
            return bufferedTime;
        }
    }
}
