package org.onosproject.usdn.protocol;

import com.github.usdn.packet.NodeStatusUpdatePacket;
import com.github.usdn.packet.RPLPacket;
import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.NodeAddress;
import com.google.common.base.MoreObjects;
import org.onlab.packet.IpAddress;
import org.onosproject.net.PortNumber;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.usdn.packet.USDNnetworkPacket.*;
import static org.slf4j.LoggerFactory.getLogger;

public class USDNMessage {
    private static final Logger log = getLogger(USDNMessage.class);

    private int length;
    private int id;
    private USDNMessageType messageType;
    private USDNnodeId source;
    private USDNnodeId destination;
    private int ttl;
    private USDNversion version;
    private byte[] rawDataPayload;
    private USDNnodeId nxHop;

    public static USDNnetworkPacket setPayload(USDNnetworkPacket np, byte[] payload) {
        byte[] packet = np.toByteArray();
        System.arraycopy(payload, 0, packet, DFLT_HDR_LEN, payload.length);
        np.setArray(packet);
        return np;
    }

    public static byte[] getPayload(USDNnetworkPacket np) {
        byte[] packet = np.toByteArray();
        return Arrays.copyOfRange(packet, DFLT_HDR_LEN, np.getLen());
    }
    public static USDNMessage fromPayload(ByteBuffer byteBuffer) {
        byte[] data = byteBuffer.array();
        USDNMessage usdnmessage = new USDNMessage();
        usdnmessage.setRawDataPayload(data);

        usdnmessage.setLength(10 + data.length);

        return usdnmessage;
    }
    public static USDNMessage fromByteBuffer(ByteBuffer byteBuffer) {
        byte[] data = byteBuffer.array();
        USDNnetworkPacket networkPacket = new USDNnetworkPacket(data);

        return getMessageFromPacket(networkPacket);
    }
    public static USDNMessage getMessageFromPacket(USDNnetworkPacket networkPacket,
                                                   IpAddress ipAddress, PortNumber port) {

        USDNMessage usdnMessage = null;
        int sensorMessageType = networkPacket.getTyp();
        if (sensorMessageType < 0) {
            sensorMessageType = ((byte) networkPacket.getTyp()) & 0xFF;
        }
//        int networkPacketType = networkPacket.getType();
        if (sensorMessageType > 127) {
            sensorMessageType = sensorMessageType - 128;
        }

        USDNMessageType messageType = USDNBuiltinMessageType.getType(networkPacket.getTyp());

        if (sensorMessageType == REG_PROXY) {
            log.info("Received DP CONNECTION PACKET {}", Arrays.toString(networkPacket.toByteArray()));
            usdnMessage = new USDNDPConnectionMessage(networkPacket, ipAddress, port);
        } else if (sensorMessageType == NODE_STATUS_UPDATE) {
            NodeStatusUpdatePacket reportPacket = new NodeStatusUpdatePacket(networkPacket);
            usdnMessage = new USDNNodeStatusMessage(reportPacket.getDistance(), reportPacket.getBattery(),
                    reportPacket.getNeigborsSize(),ipAddress, port);
            HashMap<NodeAddress, Byte> neighborsMap = reportPacket.getNeighbors();
            if ((neighborsMap != null) && (neighborsMap.size() > 0)) {
                for (Map.Entry<NodeAddress, Byte> entry : neighborsMap.entrySet()) {
                    NodeAddress nodeAddress = entry.getKey();
                    byte rssi = entry.getValue();
                    ((USDNNodeStatusMessage) usdnMessage).addNeighborRSSI(
                            new USDNnodeId(networkPacket.getNet(), nodeAddress.getArray()),
                            (rssi & 0xFF));
                }
            }
        } else {
            return getMessageFromPacket(networkPacket);
        }

        if (usdnMessage != null) {
            usdnMessage.setMessageType(messageType);
            usdnMessage.setId(networkPacket.getNet());
            usdnMessage.setLength(networkPacket.getLen());
            usdnMessage.setSource(new USDNnodeId(
                    networkPacket.getNet(), networkPacket.getSrc().getArray()));
            usdnMessage.setDestination(new USDNnodeId(
                    networkPacket.getNet(), networkPacket.getDst().getArray()));
            usdnMessage.setTtl(networkPacket.getTtl());
            usdnMessage.setVersion(USDNversion.USDN);
            usdnMessage.setRawDataPayload(getPayload(networkPacket));
            usdnMessage.setNxHop(new USDNnodeId(networkPacket.getNet(), networkPacket.getNxh().getArray()));
        }

        return usdnMessage;
    }
    public static USDNMessage getMessageFromPacket(USDNnetworkPacket networkPacket) {
        USDNMessage usdnMessage = null;
        int sensorMessageType = networkPacket.getTyp();
//        int networkPacketType = networkPacket.getType();
        if (sensorMessageType < 0) {
            sensorMessageType = ((byte) networkPacket.getTyp()) & 0xFF;
        }
        if (sensorMessageType > 127) {
            sensorMessageType = sensorMessageType - 128;
        }

        USDNMessageType messageType = USDNBuiltinMessageType.getType(sensorMessageType);

        switch (sensorMessageType) {
            case DATA:
                usdnMessage = new USDNDataMessage(getPayload(networkPacket));
                break;
            case RPL:
                RPLPacket beaconPacket = new RPLPacket(networkPacket);
                usdnMessage = new USDNRPLMessage(beaconPacket.getDistance(), beaconPacket.getBattery());
                break;
//            case MULTICAST_GROUP_JOIN:
//                byte[] multicastPayload = networkPacket.getPayload();
//                sdnWiseMessage = new SDNWiseMulticastReportMessage(
//                        multicastPayload[0], new byte[] {multicastPayload[1], multicastPayload[2]});
//                break;
//            case MULTICAST_DATA:
//                log.info("Getting multicast data message from packet {}",
//                        Arrays.toString(networkPacket.toByteArra
            case FLOWTABLE_QUERY:
                usdnMessage = new USDNQueryMessage();
                break;
            case FLOWTABLE_SET:
                USDNnodeId src = new USDNnodeId(networkPacket.getNet(), networkPacket.getSrc().getArray());
                USDNnodeId dst = new USDNnodeId(networkPacket.getNet(), networkPacket.getDst().getArray());
                usdnMessage = new USDNFlowTableSetMessage(src, dst);
                break;
            default:
                usdnMessage = new USDNAppMessage(getPayload(networkPacket));
//                if (networkPacket.getType() >= 128) {
//                    sensorMessageType = DATA_REQUEST;
//                    sdnWiseMessage = new SDNWiseDataMessage(networkPacket.getPayload());
//                }
                break;
        }

        if (usdnMessage != null) {
            usdnMessage.setMessageType(messageType);
            usdnMessage.setId(networkPacket.getNet());
            usdnMessage.setLength(networkPacket.getLen());
            usdnMessage.setSource(new USDNnodeId(
                    networkPacket.getNet(), networkPacket.getSrc().getArray()));
            usdnMessage.setDestination(new USDNnodeId(
                    networkPacket.getNet(), networkPacket.getDst().getArray()));
            usdnMessage.setTtl(networkPacket.getTtl());
            usdnMessage.setVersion(USDNversion.USDN);
            usdnMessage.setRawDataPayload(getPayload(networkPacket));
            usdnMessage.setNxHop(new USDNnodeId(networkPacket.getNet(),
                    networkPacket.getNxh().getArray()));
        }


        return usdnMessage;
    }
    public USDNnodeId getNxHop() {
        return nxHop;
    }
    public void setNxHop(USDNnodeId nxHop) {
        this.nxHop = nxHop;
    }

    public byte[] getRawDataPayload() {
        return rawDataPayload;
    }

    public void setRawDataPayload(byte[] rawDataPayload) {
        this.rawDataPayload = rawDataPayload;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public USDNMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(USDNMessageType messageType) {
        this.messageType = messageType;
    }

    public USDNnodeId getSource() {
        return source;
    }

    public void setSource(USDNnodeId source) {
        this.source = source;
        this.setId(source.netId());
    }

    public USDNnodeId getDestination() {
        return destination;
    }

    public void setDestination(USDNnodeId destination) {
        this.setId(destination.netId());
        this.destination = destination;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public USDNversion getVersion() {
        return version;
    }

    public void setVersion(USDNversion version) {
        this.version = version;
    }
    public void writeTo(org.onosproject.usdn.protocol.USDNNode USDNnode) {
        USDNnode.sendMsg(this);
    }

    public byte[] serialize() {
        byte[] buf;

        if (getNetworkPackets() == null) {
            buf = getNetworkPacket().toByteArray();
        } else {
            buf = new byte[this.getLength()];
            List<USDNnetworkPacket> networkPackets = getNetworkPackets();
            int offset = 0;
            for (USDNnetworkPacket networkPacket : networkPackets) {
                byte[] arr = networkPacket.toByteArray();
                System.arraycopy(arr, 0, buf, offset, arr.length);
                offset = offset + arr.length;
            }
        }

        return buf;
    }
    public USDNnetworkPacket getNetworkPacket() {
        USDNnetworkPacket networkPacket = new USDNnetworkPacket(this.getId(), new NodeAddress(0), new NodeAddress(0));
        networkPacket.setTyp((byte) this.getMessageType().getNetworkPacketType());
        if (destination != null) {
            networkPacket.setDst(new NodeAddress(destination.address()));
        }
        if (source != null) {
            networkPacket.setSrc(new NodeAddress(source.address()));
            networkPacket.setNxh(new NodeAddress(source.address()));
        }
        networkPacket.setNet((byte) this.getId());
//        networkPacket.setLen((byte) 10);
        networkPacket.setTtl((byte) 100);
        networkPacket = setPayload(networkPacket, this.getRawDataPayload());
        networkPacket.setNxh(new NodeAddress(getNxHop().address()));

        return networkPacket;
    }
    public List<USDNnetworkPacket> getNetworkPackets() {
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(USDNMessage.class)
                .add("type", getMessageType())
                .add("src", source.toString())
                .add("dst", destination.toString())
                .toString();
    }


}
