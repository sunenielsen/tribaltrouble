package com.oddlabs.util;

public final class Compatibility {
    /**
     * Server-client wire protocol version. Checked at login with strict equality;
     * a mismatch rejects the client with a "please update" error. Bump when the
     * matchmaking wire (ARMI interfaces or serialized classes) changes
     * incompatibly. A bump requires the server to be rebuilt and deployed from
     * the bumped ref, and locks out all older clients.
     */
    public static final int API_VERSION = 103;

    /**
     * Client-client gameplay determinism version. Reported to the server after
     * connecting (setSimVersion); the server only lists and joins games between
     * clients with equal values. Clients too old to report one are assigned
     * {@link #SIM_LEGACY}. Bump when a change alters lockstep simulation
     * behavior (model, pathfinding, landscape generation, behaviours), or the
     * methods of PlayerInterface or PeerHubInterface, whose sorted order numbers
     * the game events. Needs no server deploy and locks nobody out.
     */
    public static final int SIM_VERSION = 1009;

    /** Sim version assumed for clients that predate sim version reporting. */
    public static final int SIM_LEGACY = 0;
}
