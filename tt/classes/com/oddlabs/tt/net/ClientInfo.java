package com.oddlabs.tt.net;

import java.io.IOException;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.IllegalARMIEventException;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.net.ConnectionListener;
import com.oddlabs.net.AbstractConnection;
import com.oddlabs.tt.player.PlayerInfo;

public final strictfp class ClientInfo implements GameServerInterface, ConnectionInterface {
	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(GameServerInterface.class);
	private final Server server;
	private final PlayerSlot player_slot;

	public ClientInfo(Server server, PlayerSlot player_slot) {
		this.player_slot = player_slot;
		this.server = server;
	}

	public final void handle(Object sender, ARMIEvent armi_event) {
		try {
			armi_event.execute(interface_methods, this);
		} catch (IllegalARMIEventException e) {
			server.handleError((AbstractConnection)sender, e);
		}
	}

	public final void writeBufferDrained(AbstractConnection conn) {
	}

	public final void error(AbstractConnection conn, IOException e) {
		server.handleError(conn, e);
	}

	public final void connected(AbstractConnection conn) { 
	}

	public final PlayerSlot getPlayerSlot() {
		return player_slot;
	}

	public final void resetSlotState(int slot, boolean open) {
		server.resetSlotState(player_slot, slot, open);
	}

	public final void setPlayerSlot(int slot, int type, int race, int team, boolean ready, int ai_difficulty) {
		server.setPlayerSlot(player_slot, slot, type, race, team, ready, ai_difficulty);
	}

	public final void startServer() {
		server.startServer(player_slot);
	}

	public final void chat(String chat) {
		server.chat(player_slot, chat);
	}
}
