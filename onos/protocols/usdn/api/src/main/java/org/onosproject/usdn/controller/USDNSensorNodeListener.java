package org.onosproject.usdn.controller;

import org.onosproject.usdn.protocol.USDNnodeId;

public interface USDNSensorNodeListener {
    public void sensorNodeAdded(USDNnodeId nodeId);

    public void sensorNodeRemoved(USDNnodeId nodeId);

}
