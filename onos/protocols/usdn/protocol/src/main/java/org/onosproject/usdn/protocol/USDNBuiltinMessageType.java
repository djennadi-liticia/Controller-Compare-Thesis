package org.onosproject.usdn.protocol;

import com.github.usdn.packet.USDNnetworkPacket;
import org.onosproject.net.sensorpacket.SensorPacketTypeRegistry.SensorPacketType;

import static org.onosproject.net.sensorpacket.SensorPacketTypeRegistry.getPacketType;


public  enum USDNBuiltinMessageType implements USDNMessageType{
    DATA(getPacketType(com.github.usdn.packet.USDNnetworkPacket.DATA, "DATA")),
    RPL(getPacketType(com.github.usdn.packet.USDNnetworkPacket.RPL, "RPL")),
    NODESTATUS(getPacketType(USDNnetworkPacket.NODE_STATUS_UPDATE, "NODESTATUS")),
    OPEN_PATH(getPacketType(USDNnetworkPacket.OPEN_PATH, "OPEN_PATH")),
    FTQ(getPacketType(USDNnetworkPacket.FLOWTABLE_QUERY, "FTQ")),
    FTS(getPacketType(USDNnetworkPacket.FLOWTABLE_SET, "RESPONSE")),
    CONFIG(getPacketType(com.github.usdn.packet.USDNnetworkPacket.CONFIG, "CONFIG")),
    REG_PROXY(getPacketType(com.github.usdn.packet.USDNnetworkPacket.REG_PROXY, "REG_PROXY"));


    SensorPacketType packetType;

    USDNBuiltinMessageType(SensorPacketType data) {
        this.packetType = data;
    }
    public static USDNMessageType getType(int networkPacketType) {
        for (USDNBuiltinMessageType messageType : values()) {
            if (messageType.packetType.originalId() == networkPacketType) {
                return messageType;
            }
        }

        return new USDNExternalMessageType(networkPacketType);
    }
    public int getNetworkPacketType() {
        return this.packetType.originalId();
    }

    public SensorPacketType getSensorPacketType() {
        return this.packetType;
    }
}
