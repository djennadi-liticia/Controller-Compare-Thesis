package org.onosproject.usdn.controller.driver;

import org.onosproject.usdn.protocol.USDNMessage;
import org.onosproject.usdn.protocol.USDNNode;
import org.onosproject.usdn.protocol.USDNnodeId;

public interface USDNAgent {
    public boolean addConnectedNode(USDNnodeId id, USDNNode node);

    public void removeConnectedNode(USDNnodeId id);

    public void processMessage(USDNMessage message);
}
