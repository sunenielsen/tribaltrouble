package com.oddlabs.router;

import com.oddlabs.net.AbstractConnection;
import com.oddlabs.net.ConnectionInterface;
import com.oddlabs.net.ARMIEvent;
import com.oddlabs.net.ARMIEventBroker;
import com.oddlabs.net.ARMIInterfaceMethods;
import com.oddlabs.net.IllegalARMIEventException;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

final strictfp class RouterClient implements ConnectionInterface {
	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(RouterInterface.class);
	private final RouterClientInterface client_interface;
	private final Logger logger;
	private final AbstractConnection connection;
	private final Router router;
	private final List checksums = new LinkedList();
	private int client_id;
	private SessionManager.Timeout timeout;
	private Session session;
	private Interface current_interface;

	RouterClient(final SessionManager session_manager, AbstractConnection conn, Logger logger, Router router) {
		this.router = router;
		this.connection = conn;
		this.logger = logger;
		this.client_interface = (RouterClientInterface)ARMIEvent.createProxy(conn, RouterClientInterface.class);
		this.current_interface = new Interface(RouterInterface.class, new RouterInterface() {
			public final void login(SessionID session_id, SessionInfo session_info, int client_id) {
				Session session = session_manager.get(session_id, session_info, client_id);
				doLogin(session, session_info, client_id);
			}
		});
	}

	final List getChecksums() {
		return checksums;
	}

	final Session getSession() {
		return session;
	}

	final void setTimeout(SessionManager.Timeout timeout) {
		this.timeout = timeout;
	}

	final SessionManager.Timeout getTimeout() {
		return timeout;
	}

	final void heartbeat(int millis) {
		client_interface.heartbeat(millis);
	}

	final RouterClientInterface getInterface() {
		return client_interface;
	}

	public final void writeBufferDrained(AbstractConnection conn) {
		if (session != null && session.isComplete())
			session.startTimeout(this);
	}

	final int getClientID() {
		return client_id;
	}

	private void doLogin(Session session, SessionInfo session_info, int client_id) {
		this.session = session;
		this.client_id = client_id;
		this.current_interface = new Interface(GameInterface.class, new GameInterface() {
			public final void checksum(int checksum) {
				doChecksum(checksum);
			}
			public final void relayEventTo(int client_id, ARMIEvent event) {
				doRelayEventTo(client_id, event);
			}
			public final void relayGameStateEvent(ARMIEvent event) {
				doRelayGameStateEvent(event);
			}
			public final void relayEvent(ARMIEvent event) {
				doRelayEvent(event);
			}
		});
		session.addPlayer(this);
		logger.info("Player logged in: session = " + session + " client_id = " + client_id);
	}

	private void doChecksum(int checksum) {
		checksums.add(new Integer(checksum));
		session.checksum();
/*		if (!session.checksum()) {
			doError(true, new IOException("Checksum mismatch"));
		}*/
	}

	private void doRelayGameStateEvent(final ARMIEvent event) {
		final int next_tick = session.getNextTick();
		session.visit(new SessionVisitor() {
			public final void visit(RouterClient client) {
				client.client_interface.receiveGameStateEvent(client_id, next_tick, event);
			}
		});
	}

	private void doRelayEvent(final ARMIEvent event) {
		session.visit(new SessionVisitor() {
			public final void visit(RouterClient client) {
				client.client_interface.receiveEvent(client_id, event);
			}
		});
	}

	private void doRelayEventTo(final int receiver_client_id, final ARMIEvent event) {
		session.visit(new SessionVisitor() {
			public final void visit(RouterClient client) {
				if (client.client_id == receiver_client_id)
					client.client_interface.receiveEvent(client_id, event);
			}
		});
	}

	public final void handle(Object sender, ARMIEvent event) {
		try {
			event.execute(current_interface.methods, current_interface.instance);
		} catch (IllegalARMIEventException e) {
			doError(false, e);
		}
	}

	public final void connected(AbstractConnection conn) {
	}

	public final void error(AbstractConnection conn, IOException e) {
		doError(false, e);
	}

	final void close(final boolean checksum_error) {
		connection.close();
		if (session != null) {
			logger.info("Removing client: " + this);
			session.removePlayer(this);
			session.visit(new SessionVisitor() {
				public final void visit(RouterClient client) {
					client.client_interface.playerDisconnected(client_id, checksum_error);
				}
			});
		}
	}

	final void doError(final boolean checksum_error, Exception e) {
		close(checksum_error);
		router.removeClient(this);
		logger.info("Client disconnected, reason: " + e);
	}

	private static strictfp class Interface {
		private final Object instance;
		private final ARMIInterfaceMethods methods;

		Interface(Class interface_class, Object instance) {
			this.instance = instance;
			this.methods = new ARMIInterfaceMethods(interface_class);
		}
	}
}
