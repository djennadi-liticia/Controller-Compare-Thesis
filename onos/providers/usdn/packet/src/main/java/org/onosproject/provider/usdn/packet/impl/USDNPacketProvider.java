package org.onosproject.provider.usdn.packet.impl;

import com.google.common.collect.Lists;
import org.onlab.packet.Data;
import org.onlab.packet.Ethernet;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.*;
import org.onosproject.net.packet.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.sensor.*;
import org.onosproject.net.sensorflow.*;
import org.onosproject.net.sensorpacket.DefaultSensorInboundPacket;
import org.onosproject.net.sensorpacket.SensorPacketTypeRegistry;
import org.onosproject.usdn.controller.AtomController;
import org.onosproject.usdn.controller.USDNPacketListener;
import org.onosproject.usdn.protocol.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class USDNPacketProvider extends AbstractProvider
        implements PacketProvider, LinkProvider, SensorNodeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(USDNPacketProvider.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketProviderRegistry packetProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected AtomController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkProviderRegistry linkProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SensorNodeProviderRegistry sensorNodeProviderRegistry;


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SensorNodeService sensorNodeService;

    private PacketProviderService providerService;
    private LinkProviderService linkProviderService;
    private SensorNodeProviderService sensorNodeProviderService;

    private InternalDeviceListener deviceListener = new InternalDeviceListener();

    private USDNPacketListener packetListener = new InternalPacketProvider();

    private Map<String, Long> sensorPortsUsed = new ConcurrentHashMap<>();
    private List<DeviceIdPair> sensorPairs = Lists.newCopyOnWriteArrayList();

    private Map<DeviceId, LinkDescription> linkDescriptions = new ConcurrentHashMap<>();

    private Object lock = new Object();
    private Object linkLock = new Object();

    public USDNPacketProvider() {
        super(new ProviderId("usdn", "org.onosproject.provider.usdn"));
        LOG.info("Initializing USDN Packet Provider");
    }

    @Activate
    public void activate() {
        sensorNodeProviderService = sensorNodeProviderRegistry.register(this);
        providerService = packetProviderRegistry.register(this);
        linkProviderService = linkProviderRegistry.register(this);
        deviceService.addListener(deviceListener);
        controller.addPacketListener(packetListener);
        LOG.info("Started USDN Packet Provider");
    }

    @Deactivate
    public void deactivate() {
        sensorNodeProviderRegistry.unregister(this);
        packetProviderRegistry.unregister(this);
        controller.removePacketListener(packetListener);
        providerService = null;
        linkProviderService = null;
        linkService = null;
        sensorNodeProviderService = null;
        LOG.info("Stopped USDN Packet Provider");
    }
    @Override
    public void emit(OutboundPacket packet) {
        DeviceId deviceId = packet.sendThrough();
//        SDNWiseNodeId sdnWiseNodeId = SDNWiseNodeId.dpid(deviceId.uri());
//        LOG.info("Emitting to device with id {}", deviceId.uri());
        USDNNode node = controller.getNode(deviceId);

//        SDNWiseMessage message = SDNWiseMessage.fromByteBuffer(packet.data());
        USDNMessage message = USDNMessage.fromPayload(packet.data());
        SensorNode dstNode = sensorNodeService.getSensorNode(
                SensorNodeId.sensorNodeId(node.getId().generateMacAddress(), node.getId().netId()));
        SensorNode associatedSink = dstNode.associatedSink();
        message.setNxHop(USDNnodeId.fromMacAddress(associatedSink.mac()));
        message.setTtl(100);

        SensorTrafficTreatment localTrafficTreatment = (SensorTrafficTreatment) packet.treatment();

        if (localTrafficTreatment != null) {
            List<SensorFlowInstruction> instructions = localTrafficTreatment.sensorFlowInstructions();
            if ((instructions != null) && (instructions.size() > 0)) {
                for (SensorFlowInstruction instruction : instructions) {
                    switch (instruction.getSensorFlowInstructionType()) {
                        case SET_SRC_ADDR:
                            SensorFlowSetSrcAddrInstruction srcAddrInstruction =
                                    (SensorFlowSetSrcAddrInstruction) instruction;
                            int netId = srcAddrInstruction.getSrcAddr().getNetId();
                            USDNnodeId srcNodeId = new USDNnodeId(netId,
                                    srcAddrInstruction.getSrcAddr().getAddr());
//                            LOG.info("Setting source to node {}", srcNodeId.uri());
                            message.setSource(srcNodeId);
//                            message.setMessageType(SensorMessageType.MULTICAST_DATA);
//                            networkPacket.setType((byte) 10);
                            break;
                        case SET_DST_ADDR:
                            SensorFlowSetDstAddrInstruction dstAddrInstruction =
                                    (SensorFlowSetDstAddrInstruction) instruction;
                            netId = dstAddrInstruction.getDstAddr().getNetId();
                            USDNnodeId dstNodeId = new USDNnodeId(netId,
                                    dstAddrInstruction.getDstAddr().getAddr());
//                            LOG.info("Setting destination to node {}", dstNodeId.uri());
                            message.setDestination(dstNodeId);
//                            message.setMessageType(SensorMessageType.MULTICAST_DATA);
                            break;
                        case SET_PACKET_TYPE:
                            SensorFlowPacketTypeInstruction packetTypeInstruction =
                                    (SensorFlowPacketTypeInstruction) instruction;
                            SensorPacketTypeRegistry.SensorPacketType messageType = packetTypeInstruction.packetType();
                            message.setMessageType(USDNBuiltinMessageType.getType(messageType.originalId()));
//                            LOG.info("Set message type to emit to {} and packet type to {}", message.getMessageType()
//                                    .getSensorPacketType().originalId(),
//                                    message.getMessageType().getNetworkPacketType());
                            break;
                        default:
                            LOG.warn("Cannot handle instruction {}", instruction.getSensorFlowInstructionType());
                    }
                }
            }
            if (message == null) {
                LOG.info("The message is null");
            }
        } else {
            LOG.info("Getting plain data message");
//            message = SDNWiseDataMessage.getMessageFromPacket(networkPacket);
        }
        if (node == null) {
            LOG.info("Do not have the node {}", deviceId);
        }
        //TODO: FIXME
//        node = controller.getNode(DeviceId.deviceId("sdnwise:00:00:00:01:00:02"));
        node.sendMsg(message);
    }

    @Override
    public void triggerProbe(SensorNode sensorNode) {

    }
    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            if (event.type().equals(DeviceEvent.Type.DEVICE_ADDED)) {
                Device device = event.subject();
                LOG.info("Device {} is now alive; looking for links", device.id());
                LinkDescription linkDescription = linkDescriptions.get(device.id());
                if (linkDescription != null) {
                    LOG.info("Found link {}", linkDescription);
                    linkProviderService.linkDetected(linkDescription);
                    linkDescriptions.remove(device.id());
                }
            }
        }
    }

    private class InternalPacketProvider implements USDNPacketListener {
        @Override
        public void handlePacket(USDNMessage message) {
            InboundPacket inboundPacket = null;
            USDNnodeId incomingNodeId = message.getSource();
            USDNnodeId destinationNodeId = message.getDestination();
            LOG.info("Received message with Type {}, SRC {} and DST {}",
                    message.getMessageType().getSensorPacketType().originalId(),
                    incomingNodeId.uri(), destinationNodeId.uri());
            DeviceId incomingDeviceId = DeviceId.deviceId(incomingNodeId.uri());
            USDNNode incomingNode = controller.getNode(incomingDeviceId);
            Long curPortNumber = sensorPortsUsed.get(incomingDeviceId.uri().toString());
            long portConnection = 0;
            if (curPortNumber != null) {
                portConnection = curPortNumber.longValue();
            }
            portConnection++;
            ConnectPoint connectPoint = new ConnectPoint(incomingDeviceId, DeviceIdPair.CONTROLLER_PORT);
            sensorPortsUsed.put(incomingDeviceId.uri().toString(), portConnection);

            Ethernet ethernet = new Ethernet();

            if (destinationNodeId != null) {
                ethernet.setDestinationMACAddress(destinationNodeId.generateMacAddress());
            }
            ethernet.setVlanID((short) incomingNodeId.netId());
            USDNCorePacketContext sdnWiseCorePacketContext = null;

            USDNMessageType messageType = message.getMessageType();

            if (messageType.equals(USDNBuiltinMessageType.RPL)) {
                LOG.warn("Cannot handle RPL packets");
            } else if (messageType.equals(USDNBuiltinMessageType.CONFIG)) {
                LOG.warn("Cannot handle CONFIG packets");
            } else if (messageType.equals(USDNBuiltinMessageType.DATA)) {
                ethernet.setSourceMACAddress(incomingNodeId.generateMacAddress());
                inboundPacket = new DefaultSensorInboundPacket(messageType.getSensorPacketType(), connectPoint,
                        ethernet, ByteBuffer.wrap(message.serialize()), Optional.empty());
                sdnWiseCorePacketContext = new USDNCorePacketContext(System.currentTimeMillis(),
                        inboundPacket, null, false, incomingNode);
                providerService.processPacket(sdnWiseCorePacketContext);
            } else if (messageType.equals(USDNBuiltinMessageType.OPEN_PATH)) {
                ethernet.setSourceMACAddress(incomingNodeId.generateMacAddress())
                        .setDestinationMACAddress(message.getDestination().generateMacAddress())
                        .setPayload(new Data(message.getRawDataPayload()));
                inboundPacket = new DefaultSensorInboundPacket(messageType.getSensorPacketType(), connectPoint,
                        ethernet, ByteBuffer.wrap(message.serialize()),Optional.empty());
                sdnWiseCorePacketContext = new USDNCorePacketContext(System.currentTimeMillis(),
                        inboundPacket, null, false, incomingNode);
                providerService.processPacket(sdnWiseCorePacketContext);
            } else if (messageType.equals(USDNBuiltinMessageType.REG_PROXY)) {
                USDNDPConnectionMessage sdnWiseDPConnectionMessage =
                        (USDNDPConnectionMessage) message;
                handleDPConnectionMessage(sdnWiseDPConnectionMessage);
                DeviceId connectionSwitchId = sdnWiseDPConnectionMessage.dpid();
                ethernet.setSourceMACAddress(incomingNodeId.generateMacAddress());
                ConnectPoint switchConnectPoint = new ConnectPoint(
                        connectionSwitchId, sdnWiseDPConnectionMessage.portNumber());
                LinkDescription sensorSwitchLinkDescription = new DefaultLinkDescription(
                        connectPoint, switchConnectPoint, Link.Type.DIRECT);
                if (deviceService.getDevice(incomingDeviceId) != null) {
                    linkProviderService.linkDetected(sensorSwitchLinkDescription);
                } else {
                    linkDescriptions.put(incomingDeviceId, sensorSwitchLinkDescription);
                }
                sensorSwitchLinkDescription = new DefaultLinkDescription(
                        switchConnectPoint, connectPoint, Link.Type.DIRECT);
                linkProviderService.linkDetected(sensorSwitchLinkDescription);
                if (deviceService.getDevice(incomingDeviceId) != null) {
                    linkProviderService.linkDetected(sensorSwitchLinkDescription);
                } else {
                    linkDescriptions.put(incomingDeviceId, sensorSwitchLinkDescription);
                }
//                linkProviderService.linkDetected(sensorSwitchLinkDescription);

                LOG.info("Connecting SINK {} with OFSwitch {}",
                        connectPoint.deviceId(), switchConnectPoint.deviceId());
            } else if (messageType.equals(USDNBuiltinMessageType.NODESTATUS)) {
                ethernet.setSourceMACAddress(incomingNodeId.generateMacAddress());
                USDNNodeStatusMessage reportMessage = (USDNNodeStatusMessage) message;

//                LOG.info("Received REPORT message from node {}", Arrays.toString(message.getSource().address()));
                Map<USDNnodeId, Integer> rssis = reportMessage.getNeighborRSSI();
                if ((rssis != null) && (rssis.size() > 0)) {
                    for (Map.Entry<USDNnodeId, Integer> rssiEntry : rssis.entrySet()) {
                        USDNnodeId neighborNodeId = rssiEntry.getKey();

//                        if (!(sensorNeighborExists(SensorNodeId.sensorNodeId(
//                                        incomingNodeId.generateMacAddress(), incomingNodeId.netId()),
//                                SensorNodeId.sensorNodeId(
//                                        neighborNodeId.generateMacAddress(), neighborNodeId.netId())))) {
//
                        incomingNode.setRSSI(rssiEntry.getKey(), rssiEntry.getValue());
                        DeviceId neighborDeviceId = DeviceId.deviceId(neighborNodeId.uri());
//
//                            Long neighborCurPortNumber = sensorPortsUsed.get(neighborDeviceId.uri().toString());
//                            long neighborPortNumber = 0;
//                            if (neighborCurPortNumber != null) {
//                                neighborPortNumber = neighborCurPortNumber.longValue();
//                            }
//                            neighborPortNumber++;
//                            ConnectPoint neighborConnectPoint = new ConnectPoint(neighborDeviceId,
//                                    PortNumber.portNumber(neighborPortNumber));
//
//                            sensorPortsUsed.put(neighborDeviceId.uri().toString(), neighborPortNumber);

//                            LOG.info("About to get into the critical section");
                        synchronized (lock) {
                            DeviceIdPair deviceIdPairToCheck =
                                    new DeviceIdPair(connectPoint.deviceId(), neighborDeviceId);
                            DeviceIdPair deviceIdPair = null;
                            for (DeviceIdPair pair : sensorPairs) {
                                if (pair.equals(deviceIdPairToCheck)) {
                                    deviceIdPair = pair;
                                    break;
                                }
                            }
                            if (deviceIdPair == null) {
                                deviceIdPairToCheck.confirmPorts();
                                deviceIdPair = deviceIdPairToCheck;
                                sensorPairs.add(deviceIdPairToCheck);
                            }

                            SparseAnnotations linkAnnotations = DefaultAnnotations.builder()
                                    .set(neighborNodeId.toString(), rssiEntry.getValue().toString())
                                    .build();
                            LinkDescription linkDescription = new DefaultLinkDescription(
                                    deviceIdPair.getConnectPoint1(), deviceIdPair.getConnectPoint2(),
                                    Link.Type.DIRECT, linkAnnotations);

                            if (deviceService.getDevice(incomingDeviceId) != null) {
//                                    LOG.info("Device {} is there; creating link now...");
                                linkProviderService.linkDetected(linkDescription);
                            } else {
//                                    LOG.info("Device {} is not there; storing and waiting...");
                                linkDescriptions.put(deviceIdPair.getConnectPoint1().deviceId(), linkDescription);
                            }

//                                linkAnnotations = DefaultAnnotations.builder()
//                                        .set(incomingNodeId.toString(), rssiEntry.getValue().toString())
//                                        .build();
//                                linkDescription = new DefaultLinkDescription(
//                                        deviceIdPair.getConnectPoint2(), deviceIdPair.getConnectPoint1(),
//                                        Link.Type.DIRECT, linkAnnotations);
//                                linkProviderService.linkDetected(linkDescription);
                        }
//                        } else {
//                            LOG.info("Node {} is neighbor of {} already", neighborNodeId, incomingNodeId);
//                        }
                    }
                } else {
                    LOG.info("Node {} appears to have no neighbors", incomingNodeId);
                }

                handleReportMessage(reportMessage);
            } else if (messageType.equals(USDNBuiltinMessageType.FTQ)) {
                LOG.info("Received REQUEST message {}", Arrays.toString(message.serialize()));
                ethernet.setSourceMACAddress(incomingNodeId.generateMacAddress());
                ethernet.setDestinationMACAddress(message.getDestination().generateMacAddress());
                ethernet.setPayload(new Data(message.getRawDataPayload()));
                inboundPacket = new DefaultSensorInboundPacket(messageType.getSensorPacketType(), connectPoint,
                        ethernet, ByteBuffer.wrap(message.getNetworkPacket().toByteArray()),Optional.empty());
                sdnWiseCorePacketContext = new USDNCorePacketContext(System.currentTimeMillis(),
                        inboundPacket, null, false, incomingNode);
                providerService.processPacket(sdnWiseCorePacketContext);
            } else if (messageType.equals(USDNBuiltinMessageType.FTS)) {
                USDNFlowTableSetMessage responseMessage = (USDNFlowTableSetMessage) message;
                ethernet.setSourceMACAddress(incomingNodeId.generateMacAddress());
                ethernet.setDestinationMACAddress(message.getDestination().generateMacAddress());
                ethernet.setPayload(new Data(responseMessage.getRawDataPayload()));
                inboundPacket = new DefaultSensorInboundPacket(messageType.getSensorPacketType(), connectPoint,
                        ethernet, ByteBuffer.wrap(responseMessage.serialize()), Optional.empty());
                sdnWiseCorePacketContext = new USDNCorePacketContext(System.currentTimeMillis(),
                        inboundPacket, null, false, incomingNode);
                providerService.processPacket(sdnWiseCorePacketContext);
            } else {
//                LOG.info("Received MISC message {}", Arrays.toString(message.serialize()));
                ethernet.setSourceMACAddress(incomingNodeId.generateMacAddress())
                        .setDestinationMACAddress(message.getDestination().generateMacAddress())
                        .setPayload(new Data(message.getRawDataPayload()));
                inboundPacket = new DefaultSensorInboundPacket(messageType.getSensorPacketType(), connectPoint,
                        ethernet, ByteBuffer.wrap(message.serialize()), Optional.empty());
                sdnWiseCorePacketContext = new USDNCorePacketContext(System.currentTimeMillis(),
                        inboundPacket, null, false, incomingNode);
                providerService.processPacket(sdnWiseCorePacketContext);
            }
        }

        private boolean sensorNeighborExists(SensorNodeId sensor, SensorNodeId neighbor) {
            boolean isNeighborAlready = false;
            Map<SensorNodeId, Integer> neighborhood = sensorNodeService.getSensorNodeNeighbors(sensor);
            if (neighborhood != null) {
                Set<SensorNodeId> nodes = neighborhood.keySet();
                if (nodes.contains(neighbor)) {
                    isNeighborAlready = true;
                } else {
                    neighborhood = sensorNodeService.getSensorNodeNeighbors(neighbor);
                    if (neighborhood != null) {
                        nodes = neighborhood.keySet();
                        if (nodes.contains(sensor)) {
                            isNeighborAlready = true;
                        }
                    }
                }

            }
            return isNeighborAlready;
        }

        private void handleDPConnectionMessage(USDNDPConnectionMessage sdnWiseDPConnectionMessage) {
            SensorNodeId sensorNodeId = SensorNodeId.sensorNodeId(
                    sdnWiseDPConnectionMessage.getSource().generateMacAddress(),
                    sdnWiseDPConnectionMessage.getSource().netId());

            SensorNodeDesciption sensorNodeDesciption =
                    new DefaultSensorNodeDescription(sensorNodeId.mac(), sdnWiseDPConnectionMessage.sinkMac(),
                            sdnWiseDPConnectionMessage.sinkIp(), sdnWiseDPConnectionMessage.sinkPort(),
                            sdnWiseDPConnectionMessage.sinkConnectionIp(),
                            sdnWiseDPConnectionMessage.sinkConnectionPort(), sdnWiseDPConnectionMessage.dpid(),
                            sensorNodeId.netId(), sdnWiseDPConnectionMessage.getSource().address(),
                            new HashMap<>(), Float.MAX_VALUE);

            DeviceId deviceId = DeviceId.deviceId(sdnWiseDPConnectionMessage.getSource().uri());
            sensorNodeProviderService.sensorNodeDetected(sensorNodeId, deviceId, sensorNodeDesciption);
            LOG.info("Detected node " + sensorNodeId.toString());
        }

        private void handleReportMessage(USDNNodeStatusMessage sdnWiseReportMessage) {
            SensorNodeId sensorNodeId = SensorNodeId.sensorNodeId(
                    sdnWiseReportMessage.getSource().generateMacAddress(),
                    sdnWiseReportMessage.getSource().netId());
            Map<USDNnodeId, Integer> neighbors = sdnWiseReportMessage.getNeighborRSSI();
            Map<SensorNodeId, Integer> sensorNeighbors = new HashMap<>();
            if ((neighbors != null) && (neighbors.size() > 0)) {
                for (Map.Entry<USDNnodeId, Integer> neighbor : neighbors.entrySet()) {
                    USDNnodeId id = neighbor.getKey();
                    SensorNodeId neighborSensorNodeId = SensorNodeId.sensorNodeId(
                            id.generateMacAddress(), id.netId());
                    sensorNeighbors.put(neighborSensorNodeId, neighbor.getValue());
                }
            }
            SensorNodeDesciption sensorNodeDesciption =
                    new DefaultSensorNodeDescription(sensorNodeId.mac(), null, null, null,
                            sdnWiseReportMessage.sinkIp(), sdnWiseReportMessage.sinkPort(), null,
                            sensorNodeId.netId(), sdnWiseReportMessage.getSource().address(),
                            sensorNeighbors, (float) sdnWiseReportMessage.getBatteryLevel());

            DeviceId deviceId = DeviceId.deviceId(sdnWiseReportMessage.getSource().uri());
            sensorNodeProviderService.sensorNodeDetected(sensorNodeId, deviceId, sensorNodeDesciption);
//            LOG.info("Detected node " + sensorNodeId.toString());
        }

    }

    class SDNWiseLinkDiscovery implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {

        }
    }
}


