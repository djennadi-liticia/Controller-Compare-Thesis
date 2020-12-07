package org.onosproject.usdn.protocol;

import com.github.usdn.packet.RegProxyPacket;
import com.github.usdn.packet.USDNnetworkPacket;

import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class USDNDPConnectionMessage extends USDNMessage {
    private DeviceId dpid;
    private PortNumber portNumber;
    private MacAddress sinkMac;
    private IpAddress sinkIpAddress;
    private PortNumber sinkPort;
    private IpAddress sinkConnectionIpAddress;
    private PortNumber sinkConnectionPort;

    public USDNDPConnectionMessage(USDNnetworkPacket networkPacket, IpAddress sinkIpAddress, PortNumber sinkPort) {
        RegProxyPacket regProxyPacket = new RegProxyPacket(networkPacket);

        String dpidStr = "of:00000000" + regProxyPacket.getDpid();
        this.dpid = DeviceId.deviceId(dpidStr);
        this.portNumber = PortNumber.portNumber(regProxyPacket.getPort());
        this.sinkMac = MacAddress.valueOf(regProxyPacket.getMac());
        this.sinkIpAddress = IpAddress.valueOf(regProxyPacket.getInetSocketAddress().getAddress());
        this.sinkPort = PortNumber.portNumber(regProxyPacket.getInetSocketAddress().getPort());
        this.sinkConnectionIpAddress = sinkIpAddress;
        this.sinkConnectionPort = sinkPort;
    }
    private void init(byte[] dpid, byte[] sinkMacAddr, long portNumber) {
        String dpidStr = "of:00000000" + (new String(dpid));
        this.dpid = DeviceId.deviceId(dpidStr);
        this.portNumber = PortNumber.portNumber(portNumber);
        this.sinkMac = MacAddress.valueOf(sinkMacAddr);
    }
    public DeviceId dpid() {
        return this.dpid;
    }

    public PortNumber portNumber() {
        return this.portNumber;
    }

    public MacAddress sinkMac() {
        return this.sinkMac;
    }

    public IpAddress sinkIp() {
        return this.sinkIpAddress;
    }

    public PortNumber sinkPort() {
        return this.sinkPort;
    }

    public IpAddress sinkConnectionIp() {
        return this.sinkConnectionIpAddress;
    }
    public PortNumber sinkConnectionPort() {
        return this.sinkConnectionPort;
    }
}
