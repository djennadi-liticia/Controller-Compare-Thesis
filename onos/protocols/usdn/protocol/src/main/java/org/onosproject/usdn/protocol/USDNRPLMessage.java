package org.onosproject.usdn.protocol;

public class USDNRPLMessage extends USDNMessage {
    private int distance;
    private int batteryLevel;

    public USDNRPLMessage() {
        super();
    }
    public USDNRPLMessage(int distance, int batteryLevel) {
        super();
        this.distance = distance;
        this.batteryLevel = batteryLevel;
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

}
