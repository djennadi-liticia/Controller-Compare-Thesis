package org.onosproject.usdn.protocol;

import com.github.usdn.packet.USDNnetworkPacket;

import java.util.Arrays;

import static com.google.common.base.MoreObjects.toStringHelper;

public class USDNDataMessage extends USDNMessage {

    private byte[] appData;
    private USDNnetworkPacket networkPacket;

    public USDNDataMessage() {
        super();
    }
    public USDNDataMessage(byte[] appData) {
        super();
        this.setAppData(appData);
    }
    public byte[] getAppData() {
        return appData;
    }
    public void setAppData(byte[] appData) {
//        byte[] intermediate = new byte[appData.length];
//        int size = 0;
//        for (int i = 0; i < intermediate.length; i++) {
//            if (appData[i] != (byte) 0) {
//                intermediate[i] = appData[i];
//                size++;
//            }
//        }
//        this.appData = new byte[size];
        this.appData = new byte[appData.length];
        System.arraycopy(appData, 0, this.appData, 0, appData.length);
//        this.appData = appData;
    }
    @Override
    public byte[] serialize() {
        this.networkPacket = this.getNetworkPacket();
        byte[] finalPacket = new byte[networkPacket.getLen()];
        System.arraycopy(networkPacket.toByteArray(), 0, finalPacket, 0, finalPacket.length);
        return finalPacket;
    }
    @Override
    public USDNnetworkPacket getNetworkPacket() {
        this.networkPacket = super.getNetworkPacket();
        this.networkPacket = setPayload(networkPacket, appData);
//        int curLen = networkPacket.getLen();
        this.networkPacket.setLen((byte) (10 + appData.length));
        return networkPacket;
    }
    @Override
    public String toString() {
        return toStringHelper("USDN Data Message")
                .add("NetworkPacket", Arrays.toString(getNetworkPacket().toByteArray()))
                .toString();
    }
}
