package org.onosproject.usdn.controller;

import org.onosproject.usdn.protocol.USDNMessage;
import org.onosproject.usdn.protocol.USDNnodeId;

public interface USDNEventListener {

    public void handleMessage(USDNnodeId usdnNodeId, USDNMessage usdnMessage);
}
