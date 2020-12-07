package org.onosproject.provider.usdn.device.impl;

import org.onlab.packet.ChassisId;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.usdn.controller.AtomController;
import org.onosproject.usdn.controller.USDNnodeListener;
import org.onosproject.usdn.protocol.USDNNode;
import org.onosproject.usdn.protocol.USDNnodeId;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.net.DeviceId.deviceId;

@Component(immediate = true)
public class USDNDeviceProvider extends AbstractProvider implements DeviceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(USDNDeviceProvider.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected AtomController controller;

    private DeviceProviderService providerService;

    private final USDNnodeListener listener = new InternalDeviceProvider();

    public USDNDeviceProvider() {
        super(new ProviderId("usdn", "org.onosproject.provider.usdn"));
        LOG.info("Initializing USDN Device Provider");
    }
    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        controller.addListener(listener);
        for (USDNNode node : controller.getNodes()) {
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
        return true;
    }

    @Override
    public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {

    }
    private class InternalDeviceProvider implements USDNnodeListener {

        @Override
        public void sensorNodeAdded(USDNnodeId nodeId) {
            if (providerService == null) {
                return;
            }
            DeviceId did = deviceId(nodeId.uri());
            USDNNode node = controller.getNode(did);

            SensorDevice.Type deviceType = SensorDevice.Type.IEEE802_15_4;
            ChassisId cId = new ChassisId(nodeId.toString());
            SparseAnnotations annotations = DefaultAnnotations.builder()
                    .set("protocol", node.getVersion().toString()).build();
        }
        @Override
        public void sensorNodeRemoved(USDNnodeId nodeId) {
            if (providerService == null) {
                return;
            }
            providerService.deviceDisconnected(deviceId(nodeId.uri()));
        }
    }


}
