package org.onosproject.usdn.protocol;

import org.onosproject.net.sensorpacket.SensorPacketTypeRegistry;

public class USDNExternalMessageType implements USDNMessageType {

    private SensorPacketTypeRegistry.SensorPacketType sensorPacketType;

    public USDNExternalMessageType(int packetType) {
        this.sensorPacketType = SensorPacketTypeRegistry.getPacketType(packetType, "EXTERNAL");
    }
    public USDNExternalMessageType(int packetType, String packetTypeName) {
        this.sensorPacketType = SensorPacketTypeRegistry.getPacketType(packetType, packetTypeName);
    }
    @Override
    public int getNetworkPacketType() {
        return sensorPacketType.originalId();
    }

    @Override
    public SensorPacketTypeRegistry.SensorPacketType getSensorPacketType() {
        return sensorPacketType;
    }
}
