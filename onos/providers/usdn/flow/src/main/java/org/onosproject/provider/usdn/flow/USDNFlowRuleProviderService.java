package org.onosproject.provider.usdn.flow;

import org.onosproject.net.*;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.oldbatch.FlowRuleBatchEntry;
import org.onosproject.net.flow.oldbatch.FlowRuleBatchOperation;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.sensor.SensorNodeService;
import org.onosproject.net.sensorflow.SensorFlowInstruction;
import org.onosproject.net.sensorflow.SensorFlowOpenPathInstruction;
import org.onosproject.net.sensorflow.SensorTrafficSelector;
import org.onosproject.net.sensorflow.SensorTrafficTreatment;
import org.onosproject.usdn.controller.AtomController;
import org.onosproject.usdn.controller.USDNEventListener;
import org.onosproject.usdn.controller.USDNnodeListener;
import org.onosproject.usdn.protocol.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.onosproject.net.sensorflow.SensorFlowInstruction.Type.OPEN_PATH;
import static org.slf4j.LoggerFactory.getLogger;

public class USDNFlowRuleProviderService extends AbstractProvider implements FlowRuleProvider {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected AtomController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected SensorNodeService sensorNodeService;

    private FlowRuleProviderService providerService;

    private InternalFlowProvider listener = new InternalFlowProvider();

    public USDNFlowRuleProviderService() {
        super(new ProviderId("usdn", "org.onosproject.provider.usdn"));
        log.info("Initializing USDN  Rule Provider");
    }
    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        controller.addListener(listener);
        controller.addEventListener(listener);

        log.info("Sarted USDN Flow Rule Provider");
    }

    @Deactivate
    public void deactivate() {
        providerRegistry.unregister(this);
        providerService = null;

        log.info("Stopped USDN Flow Rule Provider");
    }

    @Override
    public void applyFlowRule(FlowRule... flowRules) {
        for (FlowRule flowRule : flowRules) {
            applyRule(flowRule);
        }
    }

    private void applyRule(FlowRule flowRule) {
        USDNNode node = controller.getNode(flowRule.deviceId());


        SensorNode dstNode = sensorNodeService.getSensorNode(
                SensorNodeId.sensorNodeId(node.getId().generateMacAddress(), node.getId().netId()));
        SensorNode dstAssociatedSink = dstNode.associatedSink();

        USDNFlowTableSetMessage sdnWiseResponseMessage = new USDNFlowTableSetMessage(
                USDNnodeId.fromMacAddress(dstAssociatedSink.mac()), node.getId());

        boolean isOpenPath = false;

        SensorTrafficSelector trafficSelector = (SensorTrafficSelector) flowRule.selector();
        SensorTrafficTreatment trafficTreatment = (SensorTrafficTreatment) flowRule.treatment();

        if (trafficTreatment != null) {
            SensorFlowInstruction sensorFlowInstruction = trafficTreatment.sensorFlowInstructions().get(0);
            if (sensorFlowInstruction.getSensorFlowInstructionType().equals(OPEN_PATH)) {
                isOpenPath = true;
//                if (node == null) {
//                    LOG.info("Node {} is not connected!!!", flowRule.deviceId().uri());
//                } else {
//                    LOG.info("Preparing OPEN_PATH for node {}", node.getId().uri());
//                }
//                        SensorNode dstNode = sensorNodeService.getSensorNode(flowRule.deviceId());

                USDNOpenPathMessage sdnWiseOpenPathMessage =
                        new USDNOpenPathMessage(node);
                SensorFlowOpenPathInstruction sensorFlowOpenPathInstruction =
                        (SensorFlowOpenPathInstruction) sensorFlowInstruction;



                Path path = sensorFlowOpenPathInstruction.getPath();
                List<Link> links = path.links();
                byte[] pathArray = new byte[2 * (links.size() + 1)];
                int k = 0;
                if ((links != null) && (links.size() > 0)) {
                    for (Link link : links) {
                        if (k == 0) {
                            DeviceId srcDeviceId = link.src().deviceId();
                            SensorNode sensorNode = sensorNodeService.getSensorNode(srcDeviceId);
                            pathArray[k++] = sensorNode.addr()[0];
                            pathArray[k++] = sensorNode.addr()[1];
                        }
                        DeviceId dstDeviceId = link.dst().deviceId();
//                        LOG.info("Adding device {} to path", dstDeviceId);
                        SensorNode sensorNode = sensorNodeService.getSensorNode(dstDeviceId);
                        if (sensorNode == null) {
                            log.warn("Got a non-SDNWISE device in OPEN PATH");
                            Iterator<Link> pathIterator = path.links().iterator();
                            while (pathIterator.hasNext()) {
                                Link myLink = pathIterator.next();
                                log.warn("Have link {}", myLink);
                            }
                        }
                        pathArray[k++] = sensorNode.addr()[0];
                        pathArray[k++] = sensorNode.addr()[1];
//                                SDNWiseNodeId dstNodeId = SDNWiseNodeId.fromUri(dstDeviceId.uri());
//                                pathArray[k++] = dstNodeId.address()[0];
//                                pathArray[k++] = dstNodeId.address()[1];
                    }
                    SensorTrafficSelector openPathTrafficSelector = (SensorTrafficSelector) flowRule.selector();
                    if (openPathTrafficSelector != null) {
                        sdnWiseOpenPathMessage.setTrafficSelection(openPathTrafficSelector);
                    }
                    sdnWiseOpenPathMessage.setPath(pathArray);
                    sdnWiseOpenPathMessage.setSource(
                            new USDNnodeId(dstAssociatedSink.netId(), dstAssociatedSink.nodeAddress().getAddr()));
                    sdnWiseOpenPathMessage.setNxHop(USDNnodeId.fromMacAddress(dstAssociatedSink.mac()));

//                            SDNWiseNode node = controller.getNode(flowRule.deviceId());

                    log.info("Sending message {} to node {}", sdnWiseOpenPathMessage.toString(), node.getID());
                    node.sendMsg(sdnWiseOpenPathMessage);
                }
            } else {
                sdnWiseResponseMessage.setTrafficSelector(trafficSelector);
                sdnWiseResponseMessage.setTrafficTreatment(trafficTreatment);
                sdnWiseResponseMessage.setNxHop(USDNnodeId.fromMacAddress(dstAssociatedSink.mac()));
            }
        }
        if (!isOpenPath) {
            node.sendMsg(sdnWiseResponseMessage);
        }
    }
    @Override
    public void removeFlowRule(FlowRule... flowRules) {

    }

    @Override
    public void executeBatch(FlowRuleBatchOperation batch) {

    }

    public Future<CompletedBatchOperation> executeBatch(BatchOperation<FlowRuleBatchEntry> batchOperation) {
//        LOG.info("Executing batch operation");
        if ((batchOperation != null) && (batchOperation.size() > 0)) {
            List<FlowRuleBatchEntry> flowRuleBatchEntries = batchOperation.getOperations();
            if ((flowRuleBatchEntries != null) && (flowRuleBatchEntries.size() > 0)) {
                for (FlowRuleBatchEntry flowRuleBatchEntry : flowRuleBatchEntries) {
                    FlowRule flowRule = flowRuleBatchEntry.target();
//                    final SDNWiseNodeId nodeId = SDNWiseNodeId.dpid(flowRule.deviceId().uri());
                    USDNNode node = controller.getNode(flowRule.deviceId());
                    if (node == null) {
                        InstallationFuture installationFuture = new InstallationFuture();
                        installationFuture.cancel(true);
                        return installationFuture;
                    }
                    applyRule(flowRule);
                }
                InstallationFuture installationFuture = new InstallationFuture();
                installationFuture.setCompleted();
                return installationFuture;
            }
        }
            return null;
        }
    private class InstallationFuture implements Future<CompletedBatchOperation> {
        private boolean cancelled = false;
        private boolean done = false;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            this.cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        public void setCompleted() {
            this.done = true;
        }

        @Override
        public boolean isDone() {
            return this.done;
        }

        @Override
        public CompletedBatchOperation get() throws InterruptedException, ExecutionException {
            return new CompletedBatchOperation(true, null, null);
        }

        @Override
        public CompletedBatchOperation get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return new CompletedBatchOperation(true, null, null);
        }
    }

    private class InternalFlowProvider
            implements USDNnodeListener, USDNEventListener {

        @Override
        public void handleMessage(USDNnodeId sdnWiseNodeId, USDNMessage sdnWiseMessage) {

        }

        @Override
        public void sensorNodeAdded(USDNnodeId nodeId) {

        }

        @Override
        public void sensorNodeRemoved(USDNnodeId nodeId) {

        }
    }
}

