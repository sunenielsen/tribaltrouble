package com.oddlabs.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.oddlabs.event.Deterministic;

public final strictfp class NetworkSelector {
	private final static long PING_TIMEOUT = 4*60*1000;
	private final static long PING_DELAY = PING_TIMEOUT/2;
	
	private final MonotoneTimeManager time_manager;
	private int current_handler_id;
	private final Map handler_map = new HashMap();
	private TaskThread task_thread;
	private Selector selector;
	private final List ping_connections = new LinkedList();
	private final List ping_timeouts = new LinkedList();

	private final Deterministic deterministic;

	public NetworkSelector(final Deterministic deterministic) {
		this(deterministic, new TimeManager() {
			public final long getMillis() {
				return deterministic.log(System.currentTimeMillis());
			}
		});
	}

	public NetworkSelector(Deterministic deterministic, TimeManager time_manager) {
		this.deterministic = deterministic;
		this.time_manager = new MonotoneTimeManager(time_manager);
	}

	public final Deterministic getDeterministic() {
		return deterministic;
	}

	final void asyncConnect(String dns_name, int port, Connection conn) {
		try {
			initSelector();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		DNSTask task = new DNSTask(dns_name, port, conn);
		getTaskThread().addTask(task);
	}

	public final TaskThread getTaskThread() {
		if (task_thread == null) {
			task_thread = new TaskThread(deterministic, new Runnable() {
				public final void run() {
					selector.wakeup();
				}
			});
		}
		return task_thread;
	}

	public final void initSelector() throws IOException {
		if (selector == null)
			selector = Selector.open();
	}

	final Selector getSelector() {
		try {
			initSelector();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return selector;
	}

	final void unregisterForPinging(Connection conn) {
		TimedConnection unregister_key = new TimedConnection(-1, conn);
		ping_timeouts.remove(unregister_key);
		ping_connections.remove(unregister_key);
	}
	
	final void registerForPingTimeout(Connection conn) {
		long ping_timeout = time_manager.getMillis() + PING_TIMEOUT;
		ping_timeouts.add(new TimedConnection(ping_timeout, conn));
	}
	
	final void registerForPing(Connection conn) {
		long ping_time = time_manager.getMillis() + PING_DELAY;
		ping_connections.add(new TimedConnection(ping_time, conn));
	}
	
	private final void processTasks() {
		if (task_thread != null)
			task_thread.poll();
	}
	
	private final long processPings(long millis) {
		long next_select_timeout = PING_DELAY;
		while (ping_timeouts.size() > 0) {
			TimedConnection first_conn = (TimedConnection)ping_timeouts.get(0);
			long first = first_conn.getTimeout();
			if (first <= millis) {
				ping_timeouts.remove(0);
				first_conn.getConnection().timeout();
			} else {
				next_select_timeout = first - millis;
				break;
			}
		}
		while (ping_connections.size() > 0) {
			TimedConnection first_conn = (TimedConnection)ping_connections.get(0);
			long first = first_conn.getTimeout();
			if (first <= millis) {
				ping_connections.remove(0);
				Connection conn = first_conn.getConnection();
				if (conn.isConnected()) {
					conn.doPing();
					registerForPing(conn);
				}
			} else {
				next_select_timeout = Math.min(first - millis, next_select_timeout);
				break;
			}
		}
		return next_select_timeout;
	}
	
	public final void tickBlocking(long timeout) throws IOException {
		processTasks();
		long millis = time_manager.getMillis();
		long next_timeout;
		long ping_timeout = processPings(millis);
		if (ping_timeout == 0)
			next_timeout = timeout;
		else if (timeout == 0)
			next_timeout = ping_timeout;
		else
			next_timeout = StrictMath.min(ping_timeout, timeout);
		if (deterministic.log(selector != null && selector.select(next_timeout) > 0))
			doTick();
	}

	public final void tickBlocking() throws IOException {
		tickBlocking(0);
	}

	public final void tick() {
		try {
			processTasks();
			processPings(time_manager.getMillis());
			if (deterministic.log(selector != null && selector.selectNow() > 0))
				doTick();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final MonotoneTimeManager getTimeManager() {
		return time_manager;
	}

	final void cancelKey(SelectionKey key, Handler handler) {
		Object handler_key = null;
		if (!deterministic.isPlayback()) {
			handler_key = key.attachment();
			key.cancel();
		}
		handler_key = deterministic.log(handler_key);
		handler_map.remove(handler_key);
	}

	final void attachToKey(SelectionKey key, Handler handler) {
		Object handler_key = null;
		if (!deterministic.isPlayback()) {
			handler_key = new Integer(current_handler_id++);
			key.attach(handler_key);
		}
		handler_key = deterministic.log(handler_key);
		handler_map.put(handler_key, handler);
	}

	private final void doTick() throws IOException {
		Iterator selected_keys = null;
		if (!deterministic.isPlayback())
			selected_keys = selector.selectedKeys().iterator();
		while (deterministic.log(deterministic.isPlayback() || selected_keys.hasNext())) {
			SelectionKey key;
			if (!deterministic.isPlayback()) {
				key = (SelectionKey)selected_keys.next();
				selected_keys.remove();
			} else
				key = null;
			if (deterministic.log(deterministic.isPlayback() || !key.isValid()))
				continue;
			Object handler_key = null;
			if (!deterministic.isPlayback())
				handler_key = key.attachment();
			handler_key = deterministic.log(handler_key);
			Handler handler = (Handler)handler_map.get(handler_key);
			try {
				handler.handle();
			} catch (IOException e) {
				handler.handleError(e);
				cancelKey(key, handler);
			}
		}
	}
}
