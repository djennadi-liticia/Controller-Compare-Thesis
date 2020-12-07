package com.github.usdn.packet;

import com.github.usdn.flowtable.FlowtableStructure;
import com.github.usdn.util.NodeAddress;

import java.util.LinkedList;
import java.util.List;

public class OpenPathPacket extends USDNnetworkPacket {
    private static final int WINDOWS_SIZE_INDEX = 0;

    public OpenPathPacket(final byte[] data) {
        super(data);
    }
    public OpenPathPacket(final int net, final NodeAddress src,
                          final NodeAddress dst,
                          final List<NodeAddress> path) {
        super(net, src, dst);
        setTyp(OPEN_PATH);
        setPayloadAt((byte) 0, WINDOWS_SIZE_INDEX);
        setPath(path);
    }
    public OpenPathPacket(final int[] data) {
        super(data);
    }
    public OpenPathPacket(final USDNnetworkPacket data) {
        super(data.toByteArray());
    }
    public final OpenPathPacket setPath(final List<NodeAddress> path) {
        int i = (getPayloadAt(WINDOWS_SIZE_INDEX) * FlowtableStructure.SIZE) + 1;
        for (NodeAddress addr : path) {
            setPayloadAt(addr.getHigh(), i);
            i++;
            setPayloadAt(addr.getLow(), i);
            i++;
        }
        return this;
    }
    public final List<NodeAddress> getPath() {
        LinkedList<NodeAddress> list = new LinkedList<>();
        byte[] payload = getPayload();
        int p = (getPayloadAt(WINDOWS_SIZE_INDEX) * FlowtableStructure.SIZE) + 1;
        for (int i = p; i < payload.length - 1; i += 2) {
            list.add(new NodeAddress(payload[i], payload[i + 1]));
        }
        return list;
    }
    public final OpenPathPacket setWindows(final List<FlowtableStructure> conditions) {
        List<NodeAddress> tmp = getPath();

        setPayloadAt((byte) conditions.size(), WINDOWS_SIZE_INDEX);
        int i = WINDOWS_SIZE_INDEX + 1;

        for (FlowtableStructure w : conditions) {
            byte[] win = w.toByteArray();
            setPayload(win, 0, i, win.length);
            i = i + win.length;
        }
        setPath(tmp);
        return this;
    }
    public final List<FlowtableStructure> getWindows() {
        LinkedList<FlowtableStructure> w = new LinkedList<>();

        int nWindows = getPayloadAt(WINDOWS_SIZE_INDEX);
        int j = 0;
        for (int i = 0; i < nWindows; i++) {
            FlowtableStructure win = new FlowtableStructure(getPayloadFromTo(
                    WINDOWS_SIZE_INDEX + 1 + j,
                    WINDOWS_SIZE_INDEX + 1 + FlowtableStructure.SIZE + j));
            w.add(win);
            j = j + FlowtableStructure.SIZE;
        }
        return w;
    }


}
