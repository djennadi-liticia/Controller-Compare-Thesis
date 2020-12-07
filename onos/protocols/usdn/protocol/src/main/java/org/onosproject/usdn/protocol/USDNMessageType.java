package org.onosproject.usdn.protocol;

import org.onosproject.net.sensorpacket.SensorPacketTypeRegistry.SensorPacketType;

public interface USDNMessageType {
    int getNetworkPacketType();

    SensorPacketType getSensorPacketType();
}
