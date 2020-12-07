package org.onosproject.provider.usdn.sensornode.impl;

import org.onlab.packet.ChassisId;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.*;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.usdn.controller.AtomController;
import org.onosproject.usdn.controller.USDNSensorNodeListener;
import org.onosproject.usdn.protocol.USDNnodeId;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class USDNSensorNodeDeviceProvider extends AbstractProvider implements DeviceProvider {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry deviceProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected AtomController controller;

    private DeviceProviderService deviceProviderService;

    private USDNSensorNodeListener sensorNodeListener =
            new InternalSDNWiseSensorNodeLister();

    public USDNSensorNodeDeviceProvider() {
        super(new ProviderId("usdn", "org.onosproject.provider.usdn", true));
    }

    @Activate
    public void activate() {
        deviceProviderService = deviceProviderRegistry.register(this);
        controller.addSensorNodeListener(sensorNodeListener);
        log.info("SDN-WISE Sensor Node Provider is now active");
    }

    @Deactivate
    public void deactivate() {
        deviceProviderRegistry.unregister(this);
        controller.removeSensorNodeListener(sensorNodeListener);
        deviceProviderService = null;
        log.info("SDN-WISE Sensor Node Provider is no longer active");
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
        // TODO implement
    }

    private class InternalSDNWiseSensorNodeLister implements USDNSensorNodeListener {

        @Override
        public void sensorNodeAdded(USDNnodeId nodeId) {
            // At this point register as a device
            DeviceId deviceId = DeviceId.deviceId(nodeId.uri());
            DeviceDescription deviceDescription =
                    new DefaultDeviceDescription(deviceId.uri(), Device.Type.OTHER,
                            "CNIT", "1.0", "1.0", null, new ChassisId(nodeId.value()));
            deviceProviderService.deviceConnected(deviceId, deviceDescription);
        }

        @Override
        public void sensorNodeRemoved(USDNnodeId nodeId) {
            DeviceId deviceId = DeviceId.deviceId(nodeId.uri());
            deviceProviderService.deviceDisconnected(deviceId);
        }
    }


}
