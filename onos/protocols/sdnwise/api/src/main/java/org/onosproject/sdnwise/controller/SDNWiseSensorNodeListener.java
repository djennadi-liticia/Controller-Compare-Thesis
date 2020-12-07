package org.onosproject.sdnwise.controller;

import org.onosproject.sdnwise.protocol.SDNWiseNodeId;

public interface SDNWiseSensorNodeListener {
    public void sensorNodeAdded(SDNWiseNodeId nodeId);

    public void sensorNodeRemoved(SDNWiseNodeId nodeId);
}
