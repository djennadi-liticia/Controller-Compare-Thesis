package org.onosproject.usdn.controller.impl;

import com.google.common.collect.Sets;
import org.onosproject.net.DeviceId;
import org.onosproject.usdn.controller.*;
import org.onosproject.usdn.controller.driver.USDNAgent;
import org.onosproject.usdn.protocol.USDNMessage;
import org.onosproject.usdn.protocol.USDNNode;
import org.onosproject.usdn.protocol.USDNnodeId;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.namedThreads;

public class USDNControllerimpl implements AtomController {
    private static final Logger log = LoggerFactory.getLogger(USDNControllerimpl.class);

    private final ExecutorService executorMsgs =
            Executors.newFixedThreadPool(32,
                    namedThreads("sdnwise-event-stats-%d"));

    private final ExecutorService executorBarrier =
            Executors.newFixedThreadPool(4,
                    namedThreads("sdnwise-event-barrier-%d"));

    protected ConcurrentHashMap<DeviceId, USDNNode> connectedNodes =
            new ConcurrentHashMap<>();

    protected USDNAgent agent = new USDNnodeAgent();
    protected Set<USDNnodeListener> sdnWiseNodeListeners =
            new HashSet<>();

    protected Set<USDNPacketListener> sdnWisePacketListeners = Sets.newHashSet();

    protected Set<USDNEventListener> sdnWiseEventListeners = Sets.newHashSet();

    protected Set<USDNSensorNodeListener> sdnWiseSensorNodeListeners = Sets.newHashSet();

    private final Controller ctrl = new Controller();

    @Override
    public Iterable<USDNNode> getNodes() {
        return connectedNodes.values();
    }

    @Override
    public USDNNode getNode(DeviceId id) {
        return connectedNodes.get(id);
    }

    @Override
    public void addListener(USDNnodeListener listener) {
        if (!this.sdnWiseNodeListeners.contains(listener)) {
            this.sdnWiseNodeListeners.add(listener);
        }
    }

    @Override
    public void removeListener(USDNnodeListener listener) {
        this.sdnWiseNodeListeners.remove(listener);
    }
    @Override
    public void addPacketListener(USDNPacketListener packetListener) {
        this.sdnWisePacketListeners.add(packetListener);
    }

    @Override
    public void addEventListener(USDNEventListener eventListener) {
        if (!this.sdnWiseEventListeners.contains(eventListener)) {
            this.sdnWiseEventListeners.add(eventListener);
        }
    }

    @Override
    public void removePacketListener(USDNPacketListener packetListener) {
        this.sdnWisePacketListeners.remove(packetListener);
    }

    @Override
    public void addSensorNodeListener(USDNSensorNodeListener sensorNodeListener) {
        if (!this.sdnWiseSensorNodeListeners.contains(sensorNodeListener)) {
            this.sdnWiseSensorNodeListeners.add(sensorNodeListener);
        }
    }
    @Override
    public void removeSensorNodeListener(USDNSensorNodeListener sensorNodeListener) {
        this.sdnWiseSensorNodeListeners.remove(sensorNodeListener);
    }

    @Override
    public void write(USDNnodeId nodeId, USDNMessage message) {
        this.getNode(DeviceId.deviceId(nodeId.uri())).sendMsg(message);
    }

    @Override
    public void processPacket(USDNnodeId nodeId, USDNMessage message) {
        for (USDNPacketListener sdnWisePacketListener : sdnWisePacketListeners) {
            sdnWisePacketListener.handlePacket(message);
        }
    }
    @Activate
    public void activate() {
        ctrl.start(agent);
        log.info("Started SDNWiseController");
    }

    @Deactivate
    public void deactivate() {
        ctrl.stop();
    }

    public class USDNnodeAgent implements USDNAgent {
        private final Logger log = LoggerFactory.getLogger(USDNnodeAgent.class);

        private USDNnodeId id;

        @Override
        public boolean addConnectedNode(USDNnodeId id, USDNNode node) {
            if (connectedNodes.get(DeviceId.deviceId(id.uri())) != null) {
//                log.error("Tried to connect node with id " + id.toString() + " but found already existing one.");
                return false;
            }
            this.id = id;
//            log.info("Added node " + id.toString());
            connectedNodes.put(DeviceId.deviceId(id.uri()), node);
//            for (SDNWiseNodeListener sdnWiseNodeListener : sdnWiseNodeListeners) {
//                sdnWiseNodeListener.sensorNodeAdded(id);
//            }

            for (USDNSensorNodeListener sdnWiseSensorNodeListener : sdnWiseSensorNodeListeners) {
                sdnWiseSensorNodeListener.sensorNodeAdded(id);
            }
            return true;
        }

        @Override
        public void removeConnectedNode(USDNnodeId id) {
            if (connectedNodes.get(DeviceId.deviceId(id.uri())) == null) {
                log.error("No node found with id " + id.toString());
            } else {
                USDNNode node = connectedNodes.remove(DeviceId.deviceId(id.uri()));
                if (node == null) {
                    log.warn("The node object for id " + id.toString() + " was null");
                }
//                for (SDNWiseNodeListener sdnWiseNodeListener : sdnWiseNodeListeners) {
//                    sdnWiseNodeListener.sensorNodeRemoved(id);
//                }
                for (USDNSensorNodeListener sdnWiseSensorNodeListener : sdnWiseSensorNodeListeners) {
                    sdnWiseSensorNodeListener.sensorNodeRemoved(id);
                }
            }
        }

        @Override
        public void processMessage(USDNMessage message) {
            processPacket(id, message);
        }
    }
    private final class USDNMessageHandler implements Runnable {

        private final USDNMessage message;
        private final USDNnodeId id;

        public USDNMessageHandler(USDNnodeId id, USDNMessage message) {
            this.message = message;
            this.id = id;
        }

        @Override
        public void run() {
            for (USDNEventListener sdnWiseEventListener : sdnWiseEventListeners) {
                sdnWiseEventListener.handleMessage(id, message);
            }
        }
    }



    }
