package com.oddlabs.tt.net;

import com.oddlabs.net.ARMIEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Manages spectator-specific state for PeerHub: catch-up, fast-forward,
 * and command event recording. Extracted to minimize spectator footprint in PeerHub.
 */
final class PeerHubSpectatorController {
    private static final int CATCH_UP_TICKS_PER_FRAME = 15;

    private static PeerHubSpectatorController instance;

    private final PeerHub hub;
    private boolean catchingUp;
    private int catchUpTargetTick;
    private final List<HeldEvent> heldEvents = new ArrayList<>();
    private boolean logApplied;

    static PeerHubSpectatorController getInstance() {
        return instance;
    }

    static void clearInstance(PeerHubSpectatorController controller) {
        if (instance == controller) instance = null;
    }

    PeerHubSpectatorController(PeerHub hub) {
        this.hub = hub;
        instance = this;
    }

    void onStart() {
        if (Network.getMatchmakingClient().isConnected()) {
            Network.getMatchmakingClient().getInterface().requestSpectatorEventLog();
        }
    }

    /**
     * Run catch-up ticks if active. Returns true if catch-up consumed this frame
     * (caller should skip normal tick processing).
     */
    boolean animateCatchUp(float t) {
        if (!catchingUp) return false;
        int ticks = 0;
        while (hub.getTick() < catchUpTargetTick && ticks < CATCH_UP_TICKS_PER_FRAME) {
            hub.doTickInternal(t);
            ticks++;
        }
        if (hub.getTick() >= catchUpTargetTick) {
            catchingUp = false;
            hub.setSynchronized(true);
            IO.println("Spectator catch-up complete at tick " + hub.getTick());
        }
        return true;
    }

    // A live event queued ahead of the log's older events would block them, since a peer only runs its head event.
    boolean holdLiveEvent(Peer peer, int tick, ARMIEvent event) {
        if (logApplied) return false;
        heldEvents.add(new HeldEvent(peer, tick, event));
        return true;
    }

    void fastForward(byte[] eventLogData, int targetTick) {
        int eventCount = 0;
        int firstHeldTick = heldEvents.isEmpty() ? Integer.MAX_VALUE : heldEvents.getFirst().tick();
        List<String> loggedSinceHeld = new ArrayList<>();
        if (eventLogData != null && eventLogData.length > 0) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(eventLogData));
            try {
                while (dis.available() > 0) {
                    int tick = dis.readInt();
                    int clientId = dis.readInt();
                    short eventSize = dis.readShort();
                    if (eventSize <= 0 || eventSize > dis.available()) break;
                    byte[] data = new byte[eventSize];
                    dis.readFully(data);
                    ByteBuffer buf = ByteBuffer.wrap(data);
                    ARMIEvent event = ARMIEvent.read(buf, eventSize);
                    Peer peer = hub.getPeerFromClientID(clientId);
                    if (peer != null) {
                        peer.addEvent(tick, event);
                        eventCount++;
                        if (tick >= firstHeldTick)
                            loggedSinceHeld.add(key(tick, clientId, data));
                    }
                }
            } catch (IOException e) {
                IO.println("Error reading event log: " + e);
            }
        }
        for (HeldEvent held : heldEvents) {
            if (!loggedSinceHeld.remove(key(held.tick(), held.peer().getPeerIndex(), bytes(held.event()))))
                held.peer().addEvent(held.tick(), held.event());
        }
        heldEvents.clear();
        logApplied = true;
        this.catchUpTargetTick = targetTick;
        this.catchingUp = true;
        IO.println("Spectator catching up to tick " + targetTick + " (" + eventCount + " events queued)");
    }

    /**
     * Records a command event to the server for spectator replay.
     * Called by the first active peer when receiving game state events.
     */
    static void sendCommandEvent(int tick, int clientId, ARMIEvent event) {
        Network.getMatchmakingClient().getInterface().updateCommandEvent(tick, clientId, event.getEventSize(),
                bytes(event));
    }

    private static byte[] bytes(ARMIEvent event) {
        ByteBuffer buf = ByteBuffer.allocate(event.getEventSize());
        event.write(buf);
        return buf.array();
    }

    private static String key(int tick, int clientId, byte[] data) {
        return tick + ":" + clientId + ":" + HexFormat.of().formatHex(data);
    }

    private record HeldEvent(Peer peer, int tick, ARMIEvent event) {
    }
}
