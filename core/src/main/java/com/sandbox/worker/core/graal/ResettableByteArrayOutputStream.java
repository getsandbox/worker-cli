package com.sandbox.worker.core.graal;

import java.io.ByteArrayOutputStream;

public class ResettableByteArrayOutputStream extends ByteArrayOutputStream {
    //reset stream to new state
    public void reset() {
        this.buf = new byte[32];
        this.count = 0;
    }
}
