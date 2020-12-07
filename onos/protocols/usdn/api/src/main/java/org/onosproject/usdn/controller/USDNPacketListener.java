package org.onosproject.usdn.controller;

import org.onosproject.usdn.protocol.USDNMessage;

public interface USDNPacketListener {
    public void handlePacket(USDNMessage message);
}
