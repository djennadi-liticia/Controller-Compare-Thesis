package org.onosproject.usdn.controller.impl;

import com.github.usdn.packet.USDNnetworkPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.onlab.packet.IpAddress;
import org.onosproject.net.PortNumber;
import org.onosproject.usdn.controller.driver.USDNNodeDriver;
import org.onosproject.usdn.protocol.USDNMessage;
import org.onosproject.usdn.protocol.USDNnodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class USDNChannelHandler extends IdleStateAwareChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(USDNChannelHandler.class);
    private final Controller controller;
    private USDNNodeDriver driver;
    private Channel channel;
    byte[] prevRecvBuf = null;

    public USDNChannelHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx,
                                 ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        log.info("New sensor node connection from {}", channel.getRemoteAddress());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
                                    ChannelStateEvent e) throws Exception {
//        log.info("Sensor node disconnected callback for node:{}. Cleaning up..." + getSensorNodeInfoString());
//        driver.removeConnectedNode();
    }
    private String getSensorNodeInfoString() {
        if (driver != null) {
            return driver.toString();
        }
        String channelString;
        if (channel == null || channel.getRemoteAddress() == null) {
            channelString = "?";
        } else {
            channelString = channel.getRemoteAddress().toString();
        }

        return String.format("[%s]", channelString);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        InetAddress address = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress();
        int port = ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getPort();
        IpAddress ipAddress = IpAddress.valueOf(address);
        PortNumber portNumber = PortNumber.portNumber(port);

//        log.info("Received the packet from node with IP {} and Port {}", ipAddress.toString(), port);

        if (e.getMessage() instanceof List) {
            List<USDNnetworkPacket> networkPackets = (List<USDNnetworkPacket>) e.getMessage();
            for (USDNnetworkPacket networkPacket : networkPackets) {
                this.processNetworkPacket(networkPacket, ipAddress, portNumber);
            }
        } else if (e.getMessage() instanceof USDNnetworkPacket) {
            USDNnetworkPacket networkPacket = (USDNnetworkPacket) e.getMessage();
        } else if (e.getMessage() instanceof byte[]) {
            byte[] bytes = (byte[]) e.getMessage();
            log.info(new String(Arrays.toString(bytes)));
        } else {
            ChannelBuffer buf = (ChannelBuffer) e.getMessage();
            ByteBuffer byteBuffer = buf.toByteBuffer();
            List<USDNnetworkPacket> networkPackets = buildNetworkPackets(byteBuffer);
            try {
//                NetworkPacket networkPacket = new NetworkPacket(byteBuffer.array());
//                processNetworkPacket(networkPacket);
                if (networkPackets != null) {
                    networkPackets.forEach(networkPacket -> processNetworkPacket(networkPacket, ipAddress, portNumber));
                }
            } catch (Exception ex) {
                log.info(Arrays.toString(byteBuffer.array()));
                ex.printStackTrace();
            }

//            log.error("Unknown message type: ");
//            while (buf.readable()) {
//
//            }
        }
    }
    private List<USDNnetworkPacket> buildNetworkPackets(ByteBuffer buf) {
        byte[] packets = buf.array();
        List<USDNnetworkPacket> networkPackets = new ArrayList<>();

        int i = 0;
        while (i < packets.length) {
            int length;
            if (prevRecvBuf != null) {
                length = prevRecvBuf[1];
//                log.info("Processing with previously received packets overall length is {}", length);
                byte[] intermediatePacket = new byte[length];

                for (int j = 0; j < prevRecvBuf.length; j++) {
                    intermediatePacket[j] = prevRecvBuf[j];
                }
                for (int j = prevRecvBuf.length; j < length; j++) {
                    intermediatePacket[j] = packets[i];
                    i++;
                }
                prevRecvBuf = null;
                USDNnetworkPacket networkPacket = new USDNnetworkPacket(intermediatePacket);
                networkPackets.add(networkPacket);
            } else {
                length = packets[i + 1];
                byte[] intermediatePacket = new byte[length];
                try {
                    if ((i + length) > packets.length) {
                        int availableLength = packets.length - i;
                        prevRecvBuf = new byte[availableLength];
                        for (int j = 0; j < availableLength; j++) {
                            prevRecvBuf[j] = packets[i];
                            i++;
                        }
                    } else {
                        for (int j = 0; j < length; j++) {
                            intermediatePacket[j] = packets[i];
                            i++;
                        }
//                        log.info("No previous packets; attempting to create packet with length {}",
//                                intermediatePacket[0]);
                        USDNnetworkPacket networkPacket = new USDNnetworkPacket(intermediatePacket);
                        networkPackets.add(networkPacket);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    log.info("Exception on Packet {}", Arrays.toString(packets));
                }

            }
        }

        return networkPackets;
    }

    private void processNetworkPacket(USDNnetworkPacket networkPacket, IpAddress ipAddress, PortNumber portNumber) {
        log.info("Received message {}", networkPacket.toString());
        USDNMessage usdnMessage = USDNMessage.getMessageFromPacket(networkPacket, ipAddress, portNumber);
        if (usdnMessage != null) {
            USDNnodeId nodeId = usdnMessage.getSource();
//        DeviceId nodeId = sdnWiseMessage.getSource();
            USDNNodeDriver nodeDriver = controller.getDriver(nodeId, channel);
            this.driver = controller.getDriver(nodeId, channel);
            boolean nodeConnected = nodeDriver.connectNode();
            log.info("Found a message of type {}", usdnMessage.getMessageType().getSensorPacketType().originalId());
        }
}
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof IOException) {
            log.error("Disconnecting node {} due to IOError: {}",
                    getSensorNodeInfoString(), e.getCause().getMessage());
            StackTraceElement[] stackTraceElements = e.getCause().getStackTrace();
            for (int i = 0; i < stackTraceElements.length; i++) {
                log.error(stackTraceElements[i].toString());
            }
        } else {
            log.error("Error while processing message from sensor node "
                    + getSensorNodeInfoString(), e.getCause());
//            ctx.getChannel().close();
        }
    }
}
