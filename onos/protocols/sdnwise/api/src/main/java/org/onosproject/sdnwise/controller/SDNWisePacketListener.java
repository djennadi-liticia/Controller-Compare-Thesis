package org.onosproject.sdnwise.controller;

import org.onosproject.sdnwise.protocol.SDNWiseMessage;


public interface SDNWisePacketListener {
    public void handlePacket(SDNWiseMessage message);
}
