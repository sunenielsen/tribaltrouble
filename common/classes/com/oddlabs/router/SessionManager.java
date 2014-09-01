package com.oddlabs.router;

import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.oddlabs.net.MonotoneTimeManager;

final strictfp class SessionManager {
	private final Map id_to_session = new HashMap();
	private final SortedMap timeouts = new TreeMap();
	private final MonotoneTimeManager time_manager;
	private final Logger logger;

	private int internal_session_id;

	SessionManager(MonotoneTimeManager time_manager, Logger logger) {
		this.logger = logger;
		this.time_manager = time_manager;
	}

	final Session get(SessionID session_id, SessionInfo session_info, int client_id) {
		Session session = (Session)id_to_session.get(session_id);
		if (session == null) {
			session = new Session(logger, session_id, session_info, this);
			id_to_session.put(session_id, session);
			logger.info("Creating session: " + session);
		} else {
			if (!session.info.equals(session_info) || session.hasClient(client_id) || client_id >= session_info.num_participants)
				throw new RuntimeException("SessionInfo mismatch " + session.info + " != " + session_info + " client_id = " + client_id);
		}
		return session;
	}

	final long getNextTimeout() {
		if (timeouts.size() == 0)
			return 0;
		Timeout timeout = (Timeout)timeouts.firstKey();
		long timeout_value = timeout.next_timeout - time_manager.getMillis();
		return StrictMath.max(1, timeout_value);
	}

	final void process() {
		long millis = time_manager.getMillis();
		while (timeouts.size() > 0) {
			Timeout timeout = (Timeout)timeouts.firstKey();
			RouterClient client = (RouterClient)timeouts.get(timeout);
			if (client.getSession().getNumPlayers() > 0) {
				if (!heartbeat(timeout.next_timeout, client, millis))
					return;
			}
			boolean removed = unregister(timeout);
			assert removed;
		}
//		verify();
	}

	private boolean heartbeat(long next_timeout, RouterClient client, long millis) {
		if (millis < next_timeout)
			return false;
		//logger.finer(id + ": next_tick " + next_tick);
		final int next_tick = computeNextTick(client, millis);
		client.heartbeat(next_tick);
		return true;
	}

	private int computeNextTick(RouterClient client, long millis) {
		return doComputeNextTick(client.getSession(), millis);
	}

/*	private void verify() {
		Iterator it = timeouts.iterator();
		if (!it.hasNext())
			return;
		Timeout t = (Timeout)it.next();
		while (it.hasNext()) {
			Timeout t2 = (Timeout)it.next();
			assert t2.compareTo(t) > 0;
			t = t2;
		}
	}
*/
	private Timeout createTimeout(RouterClient client, long millis) {
		return new Timeout(internal_session_id++, client, millis);
	}

	final long start(Session session) {
		remove(session);
		final long initial_time = time_manager.getMillis();
		session.visit(new SessionVisitor() {
			public final void visit(RouterClient client) {
				Timeout timeout = createTimeout(client, initial_time);
				client.setTimeout(timeout);
				timeouts.put(timeout, client);
			}
		});
		logger.info("Started session: " + session);
		return initial_time;
	}

	private boolean unregister(Timeout timeout) {
		return timeouts.remove(timeout) != null;
	}

	final void remove(Session session) {
		id_to_session.remove(session.session_id);
		logger.info("Removing session: " + session);
	}

	final int getNextTick(Session session) {
		final long millis = time_manager.getMillis();
		session.visit(new SessionVisitor() {
			public final void visit(RouterClient client) {
				unregister(client.getTimeout());
			}
		});
		return doComputeNextTick(session, millis);
	}

	public final void startTimeout(RouterClient client) {
		unregister(client.getTimeout());
		long millis = time_manager.getMillis();
		Timeout timeout = createTimeout(client, millis);
		client.setTimeout(timeout);
		timeouts.put(timeout, client);
	}

	private static int doComputeNextTick(Session session, long millis) {
		return (int)(millis - session.getInitialTime());
	}

	final strictfp class Timeout implements Comparable {
		private final int id;

		private final long next_timeout;

		Timeout(int id, RouterClient client, long millis) {
			this.id = id;
			this.next_timeout = millis + client.getSession().info.milliseconds_per_heartbeat;
		}

		public final int compareTo(Object other) {
			Timeout other_session = (Timeout)other;
			int diff = (int)(next_timeout - other_session.next_timeout);
			if (diff != 0)
				return diff;
			else
				return id - other_session.id;
		}

		public final boolean equals(Object other) {
			Timeout other_session = (Timeout)other;
			return compareTo(other_session) == 0;
		}

		public final int hashCode() {
			return (int)(id + next_timeout);
		}

		public final String toString() {
			return "Timout: id = " + id + " timeout " + next_timeout;
		}
	}
}
