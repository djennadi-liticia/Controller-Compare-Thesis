package org.onosproject.sdnwise.protocol;

public class SDNWiseAppMessage extends SDNWiseMessage {
    private byte[] payload;

    public SDNWiseAppMessage(byte[] payload) {
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    public byte[] payload() {
        return this.payload;
    }
}
