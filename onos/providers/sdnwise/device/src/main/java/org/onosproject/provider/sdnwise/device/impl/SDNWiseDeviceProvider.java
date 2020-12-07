package org.onosproject.provider.sdnwise.device.impl;


import org.onlab.packet.ChassisId;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.sdnwise.controller.SDNWiseController;
import org.onosproject.sdnwise.controller.SDNWiseNodeListener;
import org.onosproject.sdnwise.protocol.SDNWiseNode;
import org.onosproject.sdnwise.protocol.SDNWiseNodeId;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.net.DeviceId.deviceId;

/**
 * Created by aca on 2/13/15.
 */
@Deprecated
@Component(immediate = true)
public class SDNWiseDeviceProvider extends AbstractProvider implements DeviceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SDNWiseDeviceProvider.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SDNWiseController controller;

    private DeviceProviderService providerService;

    private final SDNWiseNodeListener listener = new InternalDeviceProvider();

    public SDNWiseDeviceProvider() {
        super(new ProviderId("sdnwise", "org.onosproject.provider.sdnwise"));
        LOG.info("Initializing SDN WISE Device Provider");
    }

    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        controller.addListener(listener);
        for (SDNWiseNode node : controller.getNodes()) {
            listener.sensorNodeAdded(node.getId());
        }
        LOG.info("Started");
    }

    @Deactivate
    public void deactivate() {
        providerRegistry.unregister(this);
        controller.removeListener(listener);
        providerService = null;

        LOG.info("Stopped");
    }

    @Override
    public void triggerProbe(DeviceId deviceId) {

    }

    @Override
    public void roleChanged(DeviceId deviceId, MastershipRole newRole) {

    }

    @Override
    public boolean isReachable(DeviceId deviceId) {
        // TODO: Check whether the node is really reachable
        return true;
    }

    @Override
    public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {

    }

    private class InternalDeviceProvider implements SDNWiseNodeListener {

        @Override
        public void sensorNodeAdded(SDNWiseNodeId nodeId) {
            if (providerService == null) {
                return;
            }
            DeviceId did = deviceId(nodeId.uri());
            SDNWiseNode node = controller.getNode(did);

            SensorDevice.Type deviceType = SensorDevice.Type.IEEE802_15_4;
            ChassisId cId = new ChassisId(nodeId.toString());
            SparseAnnotations annotations = DefaultAnnotations.builder()
                    .set("protocol", node.getVersion().toString()).build();
//            DeviceDescription description =
//                    new DefaultSensorNodeDeviceDescription(nodeId.uri(), deviceType,
//                            node.manufacturerDescription(),
//                            node.hardwareDescription(),
//                            node.softwareDescription(),
//                            node.serialNumber(),
//                            cId, annotations);
//            providerService.deviceConnected(deviceId(nodeId.uri()), description);
//            providerService.updatePorts(did, buildPortDescriptions(sw.getPorts()));
        }

        @Override
        public void sensorNodeRemoved(SDNWiseNodeId nodeId) {
            if (providerService == null) {
                return;
            }
            providerService.deviceDisconnected(deviceId(nodeId.uri()));
        }
    }
}