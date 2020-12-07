package org.onosproject.usdn.protocol;

import com.github.usdn.flowtable.AbstractAction;
import com.github.usdn.flowtable.FlowtableEntry;
import com.github.usdn.flowtable.FlowtableStructure;
import com.github.usdn.packet.FlowTableSetPacket;
import com.github.usdn.packet.USDNnetworkPacket;
import com.github.usdn.util.NodeAddress;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.sensorflow.SensorFlowInstruction;
import org.onosproject.net.sensorflow.SensorTrafficTreatment;
import org.onosproject.usdn.protocol.util.ActionInstructionUtil;
import org.onosproject.usdn.protocol.util.WindowCriterionUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class USDNFlowTableSetMessage extends  USDNMessage {
    private final Logger log = getLogger(getClass());
    private List<FlowtableStructure> flowTableWindows;
    private List<AbstractAction> flowTableActions;

    public USDNFlowTableSetMessage(USDNnodeId srcAddr, USDNnodeId dstAddr) {
        super.setDestination(dstAddr);
        super.setSource(srcAddr);
        super.setId(dstAddr.netId());
        flowTableWindows = new ArrayList<>();
        flowTableActions = new ArrayList<>();
    }
    public void setTrafficSelector(TrafficSelector trafficSelector) {
        Set<Criterion> criteria = trafficSelector.criteria();
        WindowCriterionUtil windowCriterionUtil = new WindowCriterionUtil();
        if ((criteria != null) && (criteria.size() > 0)) {
            for (Criterion criterion : criteria) {
                List<FlowtableStructure> windows = windowCriterionUtil.getWindow(criterion);
                if ((windows != null) && (windows.size() > 0)) {
                    flowTableWindows.addAll(windows);
                }
            }
        }
    }

    public void setTrafficTreatment(SensorTrafficTreatment trafficTreatment) {
        List<SensorFlowInstruction> instructions = trafficTreatment.sensorFlowInstructions();
        ActionInstructionUtil actionInstructionUtil = new ActionInstructionUtil();
        if ((instructions != null) && (instructions.size() > 0)) {
            for (SensorFlowInstruction instruction : instructions) {
                List<AbstractAction> actions = actionInstructionUtil.getAction(instruction);
                if ((actions != null) && (actions.size() > 0)) {
                    flowTableActions.addAll(actions);
                }
            }
        }
    }

    @Override
    public USDNnetworkPacket getNetworkPacket() {
        // A dummy network packet for init purposes alone
        USDNnetworkPacket networkPacket = new USDNnetworkPacket(0, new NodeAddress(0), new NodeAddress(0));

        FlowTableSetPacket responsePacket = new FlowTableSetPacket(networkPacket);
        responsePacket.setTyp(USDNnetworkPacket.FLOWTABLE_SET);
        responsePacket.setNet((byte) super.getId());
        responsePacket.setDst(new NodeAddress(super.getDestination().address()));
        responsePacket.setSrc(new NodeAddress(super.getSource().address()));
        responsePacket.setTtl((byte) 100);
        responsePacket.setNxh(new NodeAddress(getNxHop().address()));

        FlowtableEntry flowTableEntry = new FlowtableEntry();

        flowTableWindows.forEach(flowTableEntry::addWindow);
        flowTableActions.forEach(flowTableEntry::addAction);

        responsePacket.setRule(flowTableEntry);
        super.setRawDataPayload(getPayload(responsePacket));

        return responsePacket;
    }
    @Override
    public String toString() {
        return Arrays.toString(this.getNetworkPacket().toByteArray());
    }



}
