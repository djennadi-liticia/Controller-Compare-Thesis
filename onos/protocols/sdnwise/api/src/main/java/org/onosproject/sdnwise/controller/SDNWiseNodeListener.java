package org.onosproject.sdnwise.controller;

import org.onosproject.sdnwise.protocol.SDNWiseNodeId;

public interface SDNWiseNodeListener {
    public void sensorNodeAdded(SDNWiseNodeId nodeId);
    public void sensorNodeRemoved(SDNWiseNodeId nodeId);

}
