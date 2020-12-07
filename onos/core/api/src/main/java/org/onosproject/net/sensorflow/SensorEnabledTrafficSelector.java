package org.onosproject.net.sensorflow;

import com.google.common.collect.ImmutableSet;
import org.onlab.packet.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.ExtensionSelector;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.sensor.SensorNodeAddress;
import org.onosproject.net.sensorflow.SensorFlowCriterion.SensorNodeCriterionMatchType;
import org.onosproject.net.sensorflow.SensorFlowCriterion.SensorNodeFields;
import org.onosproject.net.sensorpacket.SensorPacketTypeRegistry.SensorPacketType;

import java.util.HashSet;
import java.util.Set;

import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeDstAddrCriterion;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeFieldConstCriterion;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeFieldsGenericCriterion;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeMutlicastCurHopCriterior;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeMutlicastPrevHopCriterior;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodePacketFieldsCriterion;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeSrcAddrCriterion;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeStateConstCriterion;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorNodeStateStateCriterion;
import static org.onosproject.net.sensorflow.SensorFlowCriteria.matchSensorPacketTypeCriterion;

/**
 * Created by aca on 3/2/15.
 */
public final class SensorEnabledTrafficSelector implements SensorTrafficSelector {
    private Set<Criterion> criteria;

    private SensorEnabledTrafficSelector(Set<Criterion> criteria) {
        this.criteria = ImmutableSet.copyOf(criteria);
    }

    @Override
    public Set<Criterion> criteria() {
        return this.criteria;
    }

    @Override
    public Criterion getCriterion(Criterion.Type type) {
        if (criteria != null) {
            for (Criterion criterion : criteria) {
                if ((criterion.type() != null) && (criterion.type() == type)) {
                    return criterion;
                }
            }
        }
        return null;
    }

    public static SensorTrafficSelector.Builder builder() {
        return new Builder(DefaultTrafficSelector.builder());
    }

    @Override
    public SensorFlowCriterion getSdnWiseCriterion(SensorFlowCriterion.SensorFlowCriterionType type) {
        if (criteria != null) {
            for (Criterion criterion : criteria) {
                if (criterion.type() == null) {
                    SensorFlowCriterion sensorFlowCriterion = (SensorFlowCriterion) criterion;
                    if ((sensorFlowCriterion.type() != null) && (sensorFlowCriterion.sdnWiseType() == type)) {
                        return sensorFlowCriterion;
                    }
                }
            }
        }
        return null;
    }

    public static final class Builder implements SensorTrafficSelector.Builder {
        private Set<Criterion> criteria = new HashSet<>();
        private TrafficSelector.Builder trafficSelectorBuilder;

        private Builder(TrafficSelector.Builder trafficSelectorBuilder) {
            this.trafficSelectorBuilder = trafficSelectorBuilder;
        }

        @Override
        public TrafficSelector.Builder add(Criterion criterion) {
            criteria.add(criterion);
            return this;
        }

        @Override
        public TrafficSelector.Builder matchInPort(PortNumber port) {
            return this.trafficSelectorBuilder.matchInPort(port);
        }

        @Override
        public TrafficSelector.Builder matchInPhyPort(PortNumber port) {
            return this.trafficSelectorBuilder.matchInPhyPort(port);
        }

        @Override
        public TrafficSelector.Builder matchMetadata(long metadata) {
            return this.trafficSelectorBuilder.matchMetadata(metadata);
        }

        @Override
        public TrafficSelector.Builder matchEthSrc(MacAddress macAddress) {
            return this.trafficSelectorBuilder.matchEthSrc(macAddress);
        }

        @Override
        public TrafficSelector.Builder matchEthSrcMasked(MacAddress addr, MacAddress mask) {
            return this.trafficSelectorBuilder.matchEthSrcMasked(addr,mask);
        }

        @Override
        public TrafficSelector.Builder matchEthDst(MacAddress macAddress) {
            return this.trafficSelectorBuilder.matchEthDst(macAddress);
        }

        @Override
        public TrafficSelector.Builder matchEthDstMasked(MacAddress addr, MacAddress mask) {
            return this.trafficSelectorBuilder.matchEthDstMasked(addr, mask);
        }

        @Override
        public TrafficSelector.Builder matchEthType(short i) {
            return this.trafficSelectorBuilder.matchEthType(i);
        }

        @Override
        public TrafficSelector.Builder matchVlanId(VlanId vlanId) {
            return this.trafficSelectorBuilder.matchVlanId(vlanId);
        }

        @Override
        public TrafficSelector.Builder matchVlanPcp(byte vlanPcp) {
            return this.trafficSelectorBuilder.matchVlanPcp(vlanPcp);
        }

        @Override
        public TrafficSelector.Builder matchInnerVlanId(VlanId vlanId) {
            return this.trafficSelectorBuilder.matchInnerVlanId(vlanId);
        }

        @Override
        public TrafficSelector.Builder matchInnerVlanPcp(byte vlanPcp) {
            return this.trafficSelectorBuilder.matchInnerVlanPcp(vlanPcp);
        }

        @Override
        public TrafficSelector.Builder matchIPDscp(byte ipDscp) {
            return this.trafficSelectorBuilder.matchIPDscp(ipDscp);
        }

        @Override
        public TrafficSelector.Builder matchIPEcn(byte ipEcn) {
            return this.trafficSelectorBuilder.matchIPEcn(ipEcn);
        }

        @Override
        public TrafficSelector.Builder matchIPProtocol(byte proto) {
            return this.trafficSelectorBuilder.matchIPProtocol(proto);
        }

        @Override
        public TrafficSelector.Builder matchIPSrc(IpPrefix ipPrefix) {
            return this.trafficSelectorBuilder.matchIPSrc(ipPrefix);
        }

        @Override
        public TrafficSelector.Builder matchIPDst(IpPrefix ipPrefix) {
            return this.trafficSelectorBuilder.matchIPDst(ipPrefix);
        }

        @Override
        public TrafficSelector.Builder matchTcpSrc(TpPort tcpPort) {
            return this.trafficSelectorBuilder.matchTcpSrc(tcpPort);
        }

        @Override
        public TrafficSelector.Builder matchTcpSrcMasked(TpPort tcpPort, TpPort mask) {
            return this.trafficSelectorBuilder.matchTcpSrcMasked(tcpPort,mask);
        }

        @Override
        public TrafficSelector.Builder matchTcpDst(TpPort tcpPort) {
            return this.trafficSelectorBuilder.matchTcpDst(tcpPort);
        }

        @Override
        public TrafficSelector.Builder matchTcpDstMasked(TpPort tcpPort, TpPort mask) {
            return this.trafficSelectorBuilder.matchTcpDstMasked(tcpPort,mask);
        }

        @Override
        public TrafficSelector.Builder matchUdpSrc(TpPort udpPort) {
            return this.trafficSelectorBuilder.matchUdpSrc(udpPort);
        }

        @Override
        public TrafficSelector.Builder matchUdpSrcMasked(TpPort udpPort, TpPort mask) {
            return this.trafficSelectorBuilder.matchUdpSrcMasked(udpPort,mask);
        }

        @Override
        public TrafficSelector.Builder matchUdpDst(TpPort udpPort) {
            return this.trafficSelectorBuilder.matchUdpDst(udpPort);
        }

        @Override
        public TrafficSelector.Builder matchUdpDstMasked(TpPort udpPort, TpPort mask) {
            return this.trafficSelectorBuilder.matchUdpDstMasked(udpPort,mask);
        }

        @Override
        public TrafficSelector.Builder matchSctpSrc(TpPort sctpPort) {
            return this.trafficSelectorBuilder.matchSctpSrc(sctpPort);
        }

        @Override
        public TrafficSelector.Builder matchSctpSrcMasked(TpPort sctpPort, TpPort mask) {
            return this.trafficSelectorBuilder.matchSctpSrcMasked(sctpPort,mask);
        }

        @Override
        public TrafficSelector.Builder matchSctpDst(TpPort sctpPort) {
            return this.trafficSelectorBuilder.matchSctpDst(sctpPort);
        }

        @Override
        public TrafficSelector.Builder matchSctpDstMasked(TpPort sctpPort, TpPort mask) {
            return this.trafficSelectorBuilder.matchSctpDstMasked(sctpPort,mask);
        }

        @Override
        public TrafficSelector.Builder matchIcmpType(byte icmpType) {
            return this.trafficSelectorBuilder.matchIcmpType(icmpType);
        }

        @Override
        public TrafficSelector.Builder matchIcmpCode(byte icmpCode) {
            return this.trafficSelectorBuilder.matchIcmpCode(icmpCode);
        }

        @Override
        public TrafficSelector.Builder matchIPv6Src(IpPrefix ip) {
            return this.trafficSelectorBuilder.matchIPv6Src(ip);
        }

        @Override
        public TrafficSelector.Builder matchIPv6Dst(IpPrefix ip) {
            return this.trafficSelectorBuilder.matchIPv6Dst(ip);
        }

        @Override
        public TrafficSelector.Builder matchIPv6FlowLabel(int flowLabel) {
            return this.trafficSelectorBuilder.matchIPv6FlowLabel(flowLabel);
        }

        @Override
        public TrafficSelector.Builder matchIcmpv6Type(byte icmpv6Type) {
            return this.trafficSelectorBuilder.matchIcmpv6Type(icmpv6Type);
        }

        @Override
        public TrafficSelector.Builder matchIcmpv6Code(byte icmpv6Code) {
            return this.trafficSelectorBuilder.matchIcmpv6Code(icmpv6Code);
        }

        @Override
        public TrafficSelector.Builder matchIPv6NDTargetAddress(Ip6Address targetAddress) {
            return this.trafficSelectorBuilder.matchIPv6NDTargetAddress(targetAddress);
        }

        @Override
        public TrafficSelector.Builder matchIPv6NDSourceLinkLayerAddress(MacAddress mac) {
            return this.trafficSelectorBuilder.matchIPv6NDSourceLinkLayerAddress(mac);
        }

        @Override
        public TrafficSelector.Builder matchIPv6NDTargetLinkLayerAddress(MacAddress mac) {
            return this.trafficSelectorBuilder.matchIPv6NDTargetLinkLayerAddress(mac);
        }

        @Override
        public TrafficSelector.Builder matchMplsLabel(MplsLabel mplsLabel) {
            return this.trafficSelectorBuilder.matchMplsLabel(mplsLabel);
        }

        @Override
        public TrafficSelector.Builder matchMplsBos(boolean mplsBos) {
            return this.trafficSelectorBuilder.matchMplsBos(mplsBos);
        }

        @Override
        public TrafficSelector.Builder matchTunnelId(long tunnelId) {
            return this.trafficSelectorBuilder.matchTunnelId(tunnelId);
        }

        @Override
        public TrafficSelector.Builder matchIPv6ExthdrFlags(short exthdrFlags) {
            return this.trafficSelectorBuilder.matchIPv6ExthdrFlags(exthdrFlags);
        }

        @Override
        public TrafficSelector.Builder matchArpTpa(Ip4Address addr) {
            return this.trafficSelectorBuilder.matchArpTpa(addr);
        }

        @Override
        public TrafficSelector.Builder matchArpSpa(Ip4Address addr) {
            return this.trafficSelectorBuilder.matchArpSpa(addr);
        }

        @Override
        public TrafficSelector.Builder matchArpTha(MacAddress addr) {
            return this.trafficSelectorBuilder.matchArpTha(addr);
        }

        @Override
        public TrafficSelector.Builder matchArpSha(MacAddress addr) {
            return this.trafficSelectorBuilder.matchArpSha(addr);
        }

        @Override
        public TrafficSelector.Builder matchArpOp(int arpOp) {
            return this.trafficSelectorBuilder.matchArpOp(arpOp);
        }

        @Override
        public TrafficSelector.Builder matchPi(PiCriterion piCriterion) {
            return null;
        }

        @Override
        public TrafficSelector.Builder extension(ExtensionSelector extensionSelector, DeviceId deviceId) {
            return this.trafficSelectorBuilder.extension(extensionSelector,deviceId);
        }

        @Override
        public SensorTrafficSelector build() {
            return new SensorEnabledTrafficSelector(criteria);
        }

        @Override
        public SensorTrafficSelector.Builder add(SensorFlowCriterion sensorFlowCriterion) {
            criteria.add(sensorFlowCriterion);
            return this;
        }

        @Override
        public SensorTrafficSelector.Builder matchPacketFields(SensorNodeFields field1,
                                                                SensorNodeFields field2) {
            return add(matchSensorNodePacketFieldsCriterion(field1, field2));
        }

        @Override
        public SensorTrafficSelector.Builder matchPacketFields(SensorNodeFields field1, SensorNodeFields field2,
                                                                SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodePacketFieldsCriterion(field1, field2, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchPacketFields(int pos1, int pos2, SensorNodeCriterionMatchType
                matchType) {
            return add(matchSensorNodeFieldsGenericCriterion(pos1, pos2, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchPacketFieldWithConst(int packetPos, int value, int offset,
                                                                      SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodeFieldConstCriterion(packetPos, value, offset, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchStateConst(int beginPos, int endPos, int value,
                                                             SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodeStateConstCriterion(beginPos, endPos, value, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchStateState(int operand1Pos, int operand2Pos, int offset,
                                                             SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodeStateStateCriterion(operand1Pos, operand2Pos, offset, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchNodeSrcAddr(SensorNodeAddress addr) {
            return add(matchSensorNodeSrcAddrCriterion(addr));
        }

        @Override
        public SensorTrafficSelector.Builder matchNodeSrcAddr(SensorNodeAddress addr,
                                                               SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodeSrcAddrCriterion(addr, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchNodeDstAddr(SensorNodeAddress addr) {
            return add(matchSensorNodeDstAddrCriterion(addr));
        }

        @Override
        public SensorTrafficSelector.Builder matchNodeDstAddr(SensorNodeAddress addr,
                                                               SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodeDstAddrCriterion(addr, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchSensorPacketType(SensorPacketType packetType) {
            return add(matchSensorPacketTypeCriterion(packetType));
        }

        @Override
        public SensorTrafficSelector.Builder matchSensorPacketType(SensorNodeCriterionMatchType matchType,
                                                                   SensorPacketType packetType) {
            return add(matchSensorPacketTypeCriterion(matchType, packetType));
        }

        @Override
        public SensorTrafficSelector.Builder matchSensorNodeMutlicastPrevHop(SensorNodeAddress prevNode) {
            return add(matchSensorNodeMutlicastPrevHopCriterior(prevNode));
        }

        @Override
        public SensorTrafficSelector.Builder matchSensorNodeMutlicastPrevHop(SensorNodeAddress prevNode,
                                                                              SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodeMutlicastPrevHopCriterior(prevNode, matchType));
        }

        @Override
        public SensorTrafficSelector.Builder matchSensorNodeMutlicastCurHop(SensorNodeAddress curNode) {
            return add(matchSensorNodeMutlicastCurHopCriterior(curNode));
        }

        @Override
        public SensorTrafficSelector.Builder matchSensorNodeMutlicastCurHop(SensorNodeAddress curNode,
                                                                             SensorNodeCriterionMatchType matchType) {
            return add(matchSensorNodeMutlicastCurHopCriterior(curNode, matchType));
        }


    }
}
