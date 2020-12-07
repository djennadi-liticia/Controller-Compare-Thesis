package org.onosproject.usdn.controller.driver;


import org.jboss.netty.channel.Channel;
import org.onosproject.usdn.protocol.USDNMessage;
import org.onosproject.usdn.protocol.USDNNode;

import java.util.List;

public interface USDNNodeDriver extends USDNNode {
    public void setAgent(USDNAgent agent);

    public void startDriverHandshake();

    public boolean isDriverHandshakeComplete();

    public boolean connectNode();

    public void removeConnectedNode();

    public int getNextTransactionId();

    public void setChannel(Channel channel);

    public void setConnected(boolean connected);

    public void write(USDNMessage msg);

    public void write(List<USDNMessage> msgs);
}
