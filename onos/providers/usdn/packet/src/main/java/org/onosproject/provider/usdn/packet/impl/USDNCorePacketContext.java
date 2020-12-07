package org.onosproject.provider.usdn.packet.impl;

import com.github.usdn.packet.USDNnetworkPacket;
import org.onosproject.net.packet.DefaultPacketContext;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.usdn.protocol.USDNMessage;
import org.onosproject.usdn.protocol.USDNNode;

public class USDNCorePacketContext extends DefaultPacketContext {

    private final USDNNode usdnNode;

    protected USDNCorePacketContext(long time, InboundPacket inPkt,
                                       OutboundPacket outPkt, boolean block,
                                       USDNNode usdnNode) {
        super(time, inPkt, outPkt, block);
        this.usdnNode = usdnNode;
    }

    @Override
    public void send() {
        USDNnetworkPacket networkPacket = new USDNnetworkPacket(inPacket().unparsed().array());
        USDNMessage usdnMessage = USDNMessage.getMessageFromPacket(networkPacket);
        usdnMessage.writeTo(usdnNode);
    }
}
