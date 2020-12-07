package org.onosproject.usdn.protocol;

public class USDNAppMessage extends USDNMessage {
    private byte[] payload;

    public USDNAppMessage(byte[] payload) {
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    public byte[] payload() {
        return this.payload;
    }
}
