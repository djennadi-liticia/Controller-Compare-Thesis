package org.onosproject.provider.usdn.flow;

import org.onosproject.net.Link;
import org.onosproject.net.topology.LinkWeight;
import org.onosproject.net.topology.TopologyEdge;

public class USDNLinkWeight implements LinkWeight {
    @Override
    public double weight(TopologyEdge edge) {
        Link link = edge.link();
        String rssi = link.annotations().value("rssi");
        return Double.valueOf(rssi);
    }
}
