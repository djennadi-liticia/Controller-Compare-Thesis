package org.onosproject.usdn.controller.impl;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.onosproject.usdn.controller.driver.USDNAgent;
import org.onosproject.usdn.controller.driver.USDNNodeDriver;
import org.onosproject.usdn.drivers.USDNSensorNodeImpl;
import org.onosproject.usdn.protocol.USDNnodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.namedThreads;

public class Controller {
    protected static final Logger log = LoggerFactory.getLogger(Controller.class);

    private ChannelGroup cg;

    protected int usdnPort = 9999;
    protected int workerThreads = 0;

    protected long systemStartTime;

    private USDNAgent agent;

    private NioServerSocketChannelFactory execFactory;

    protected static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;
    protected static final int RCV_BUFFER_SIZE = 10240;

    public long getSystemStartTime() {
        return this.systemStartTime;
    }
    public void run() {

        try {
            final ServerBootstrap bootstrap = createServerBootStrap();

            bootstrap.setOption("reuseAddress", true);
            bootstrap.setOption("child.keepAlive", true);
//            bootstrap.setOption("child.tcpNoDelay", false);
            bootstrap.setOption("child.sendBufferSize", Controller.SEND_BUFFER_SIZE);
            bootstrap.setOption("child.receiveBufferSize", Controller.RCV_BUFFER_SIZE);

            ChannelPipelineFactory pfact =
                    new USDNPipelineFactory(this, null);
            bootstrap.setPipelineFactory(pfact);
            InetSocketAddress sa = new InetSocketAddress(usdnPort);
            cg = new DefaultChannelGroup();
            cg.add(bootstrap.bind(sa));

            log.info("Listening for sensor node connections on {}", sa);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private ServerBootstrap createServerBootStrap() {

        if (workerThreads == 0) {
            execFactory =  new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(namedThreads("Controller-boss-%d")),
                    Executors.newCachedThreadPool(namedThreads("Controller-worker-%d")));
//            execFactory = new OioServerSocketChannelFactory(
//                    Executors.newCachedThreadPool(namedThreads("Controller-boss-%d")),
//                    Executors.newCachedThreadPool(namedThreads("Controller-worker-%d")));
            return new ServerBootstrap(execFactory);
        } else {
            execFactory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(namedThreads("Controller-boss-%d")),
                    Executors.newCachedThreadPool(namedThreads("Controller-worker-%d")), workerThreads);
//            execFactory = new OioServerSocketChannelFactory(
//                    Executors.newCachedThreadPool(namedThreads("Controller-boss-%d")),
//                    Executors.newCachedThreadPool(namedThreads("Controller-worker-%d")), workerThreads);
            return new ServerBootstrap(execFactory);
        }
    }
    public void start(USDNAgent ag) {
        log.info("Starting USDN IO");
        this.agent = ag;
//        this.init(new HashMap<String, String>());
        this.run();
    }


    public void stop() {
        log.info("Stopping USDN IO");
        execFactory.releaseExternalResources();
        cg.close();
    }

    public USDNNodeDriver getDriver(USDNnodeId nodeId, Channel channel) {
        USDNNodeDriver sdnWiseNodeDriver = new USDNSensorNodeImpl(nodeId, channel);
        sdnWiseNodeDriver.setAgent(agent);

        return sdnWiseNodeDriver;
    }



}
