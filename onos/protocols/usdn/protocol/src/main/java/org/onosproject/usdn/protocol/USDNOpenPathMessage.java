package org.onosproject.usdn.protocol;

import com.github.usdn.flowtable.FlowtableStructure;
import com.github.usdn.packet.OpenPathPacket;
import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.NodeAddress;
import com.google.common.collect.Lists;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.sensorflow.SensorTrafficSelector;
import org.onosproject.usdn.protocol.util.WindowCriterionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.MoreObjects.toStringHelper;
import static org.onosproject.usdn.protocol.USDNBuiltinMessageType.OPEN_PATH;

public class USDNOpenPathMessage extends USDNMessage {
    List<FlowtableStructure> conditions = null;
    Function<byte[], List<NodeAddress>> transform;
    private USDNNode destination;
    private byte[] path;

    public USDNOpenPathMessage(USDNNode destination) {
        this.setMessageType(OPEN_PATH);
        this.destination = destination;

        super.setId(destination.getId().netId());
//        super.setSource(destination.getId());
        super.setDestination(destination.getId());

        transform = bytes -> {
            List<NodeAddress> nodeAddresses = Lists.newArrayList();
            int i = 0;
            while (i < bytes.length) {
                NodeAddress nodeAddress = new NodeAddress(new byte[]{bytes[i], bytes[i + 1]});
                nodeAddresses.add(nodeAddress);
                i += 2;
            }
            return nodeAddresses;
        };
    }

    public byte[] getPath() {
        return this.path;
    }

    public void setPath(byte[] path) {
        this.path = path;
    }
    public void setTrafficSelection(SensorTrafficSelector trafficSelection) {
        conditions = new ArrayList<>();
        WindowCriterionUtil windowCriterionUtil = new WindowCriterionUtil();
        Set<Criterion> criteria = trafficSelection.criteria();
        if ((criteria != null) && (criteria.size() > 0)) {
            for (Criterion criterion : criteria) {
                conditions.addAll(windowCriterionUtil.getWindow(criterion));
            }
        }
    }
    @Override
    public USDNnetworkPacket getNetworkPacket() {
        NodeAddress dstAddr = new NodeAddress(new byte[]{path[0], path[1]});
        NodeAddress srcAddr = new NodeAddress(super.getSource().address());
        OpenPathPacket openPathPacket = new OpenPathPacket(destination.getId().netId(), srcAddr,
                dstAddr, transform.apply(path));
        if (conditions != null) {
            openPathPacket.setWindows(conditions);
        }
        super.setRawDataPayload(getPayload(openPathPacket));
        openPathPacket.setNxh(new NodeAddress(getNxHop().address()));
        return openPathPacket;
    }

    @Override
    public byte[] serialize() {
        return getNetworkPacket().toByteArray();
    }
    @Override
    public String toString() {
        return toStringHelper("OPEN_PATH")
                .add("message", Arrays.toString(serialize()))
                .toString();
    }

}
