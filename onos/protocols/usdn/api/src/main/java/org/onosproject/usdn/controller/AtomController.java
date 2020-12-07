package org.onosproject.usdn.controller;


import org.onosproject.net.DeviceId;
import org.onosproject.usdn.protocol.USDNMessage;
import org.onosproject.usdn.protocol.USDNNode;
import org.onosproject.usdn.protocol.USDNnodeId;

public interface AtomController {
    public Iterable<USDNNode> getNodes();

    public USDNNode getNode(DeviceId id);

    public void addListener(USDNnodeListener listener);

    public void removeListener(USDNnodeListener listener);

    public void addPacketListener(USDNPacketListener packetListener);

    public void addEventListener(USDNEventListener eventListener);

    public void removePacketListener(USDNPacketListener packetListener);

    public void addSensorNodeListener(USDNSensorNodeListener sensorNodeListener);

    public void removeSensorNodeListener(USDNSensorNodeListener sensorNodeListener);

    public void write(USDNnodeId nodeId, USDNMessage message);

    public void processPacket(USDNnodeId nodeId, USDNMessage message);


}
