package com.oddlabs.matchmaking;

import java.net.InetAddress;
import java.io.Serializable;

public final strictfp class TunnelAddress implements Serializable {
	private final static long serialVersionUID = -2854382209354714233l;
	private final int host_id;
	private final InetAddress address;
	private final InetAddress local_address;

	public TunnelAddress(int host_id, InetAddress address, InetAddress local_address) {
		this.host_id = host_id;
		this.address = address;
		this.local_address = local_address;
	}

	public final int getHostID() {
		return host_id;
	}

	public final InetAddress getAddress() {
		return address;
	}

	public final InetAddress getLocalAddress() {
		return local_address;
	}

	public final String toString() {
		return "host id = " + host_id + " address = " + address + " local_address = " + local_address;
	}
}	
