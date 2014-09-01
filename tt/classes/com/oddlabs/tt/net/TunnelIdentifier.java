package com.oddlabs.tt.net;

import com.oddlabs.matchmaking.TunnelAddress;
import com.oddlabs.matchmaking.Profile;

public final strictfp class TunnelIdentifier {
	private final Profile profile;
	private final TunnelAddress address;

	public TunnelIdentifier(Profile profile, TunnelAddress address) {
		this.profile = profile;
		this.address = address;
	}

	public final Profile getProfile() {
		return profile;
	}

	public final TunnelAddress getAddress() {
		return address;
	}

	public final String toString() {
		return "profile: " + profile + " tunnel address: " + address;
	}
}
