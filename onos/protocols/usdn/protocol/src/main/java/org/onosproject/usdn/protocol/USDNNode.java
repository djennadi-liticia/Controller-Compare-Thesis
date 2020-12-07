package org.onosproject.usdn.protocol;

import java.util.List;

public interface USDNNode {
    public void sendMsg(USDNMessage msg);

    public void sendMsg(List<USDNMessage> msgs);

    public void handleMessage(USDNMessage fromNode);

    public String getID();

    public USDNnodeId getId();

    public String manufacturerDescription();

    public String hardwareDescription();

    public String softwareDescription();

    public String serialNumber();

    public boolean isConnected();

    public void disconnectNode();

    public double batteryLevel();

    public double getTransmissionPowerLevel();

    public void setTransmissionPowerLevel(double level);

    public double getRSSI(USDNnodeId neighbor);

    public void setRSSI(USDNnodeId neighbor, int rssi);

    public USDNnodeState getNodeState();

    public USDNversion getVersion();
}

