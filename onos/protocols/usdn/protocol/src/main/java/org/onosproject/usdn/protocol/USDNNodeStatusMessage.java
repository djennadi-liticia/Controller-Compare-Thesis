package org.onosproject.usdn.protocol;

import org.onlab.packet.IpAddress;
import org.onosproject.net.PortNumber;

import java.util.HashMap;
import java.util.Map;

public class USDNNodeStatusMessage extends USDNMessage {

    private int distance;
    private int batteryLevel;
    private int nofNeighbors;
    private Map<USDNnodeId, Integer> neighborRSSI;
    private IpAddress sinkIpAddress;
    private PortNumber sinkPortNumber;

    public USDNNodeStatusMessage() {
        super();
        this.neighborRSSI = new HashMap<>();
    }
    public USDNNodeStatusMessage(int distance, int batteryLevel, int nofNeighbors,
                               IpAddress sinkIpAddress, PortNumber sinkPortNumber) {
        super();
        this.distance = distance;
        this.batteryLevel = batteryLevel;
        this.nofNeighbors = nofNeighbors;
        this.neighborRSSI = new HashMap<>();
        this.sinkIpAddress = sinkIpAddress;
        this.sinkPortNumber = sinkPortNumber;
    }

    public USDNNodeStatusMessage(int distance, int batteryLevel, int nofNeighbors,
                                Map<USDNnodeId, Integer> neighborRSSI) {
        super();
        this.distance = distance;
        this.batteryLevel = batteryLevel;
        this.nofNeighbors = nofNeighbors;
        this.neighborRSSI = neighborRSSI;
    }
    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public int getNofNeighbors() {
        return nofNeighbors;
    }

    public void setNofNeighbors(int nofNeighbors) {
        this.nofNeighbors = nofNeighbors;
    }

    public Map<USDNnodeId, Integer> getNeighborRSSI() {
        return neighborRSSI;
    }
    public void setNeighborRSSI(Map<USDNnodeId, Integer> neighborRSSI) {
        this.neighborRSSI = neighborRSSI;
    }

    public void addNeighborRSSI(USDNnodeId neighbor, int rssi) {
        this.neighborRSSI.put(neighbor, rssi);
    }

    public Integer getNeighborRSSI(USDNnodeId neighbor) {
        return this.neighborRSSI.get(neighbor);
    }

    public IpAddress sinkIp() {
        return this.sinkIpAddress;
    }

    public PortNumber sinkPort() {
        return this.sinkPortNumber;
    }


}
