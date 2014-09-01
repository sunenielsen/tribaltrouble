package com.oddlabs.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import com.oddlabs.event.Deterministic;

public final strictfp class ConnectionListener extends AbstractConnectionListener implements Handler {
	private final NetworkSelector network;
	private SelectionKey key;

	private List incoming_connections = new LinkedList();
	
	private final static SelectionKey createServerSocket(NetworkSelector network, InetAddress ip, int port) throws IOException {
		ServerSocketChannel server_channel = ServerSocketChannel.open();
		server_channel.configureBlocking(false);
		SocketAddress address = new InetSocketAddress(ip, port);
		server_channel.socket().setReuseAddress(true);
		server_channel.socket().bind(address);
		SelectionKey key = server_channel.register(network.getSelector(), SelectionKey.OP_ACCEPT);
		return key;
	}
	
	public ConnectionListener(NetworkSelector network, InetAddress ip, int port, ConnectionListenerInterface connection_listener_interface) {
		super(connection_listener_interface);
		this.network = network;
		IOException exception;
		try {
			if (!network.getDeterministic().isPlayback()) {
				key = createServerSocket(network, ip, port);
			}
			exception = null;
		} catch (IOException e) {
			exception = e;
		}
		if (network.getDeterministic().log(exception != null))
			error((IOException)network.getDeterministic().log(exception));
		else
			network.attachToKey(key, this);
	}
	
	public final int getPort() {
		return network.getDeterministic().log(key != null ? ((ServerSocketChannel)key.channel()).socket().getLocalPort() : -1);
	}

	public final void handle() throws IOException {
		IOException exception = null;
		SocketChannel channel = null;
		if (!network.getDeterministic().isPlayback()) {
			ServerSocketChannel server_channel = (ServerSocketChannel)key.channel();
			channel = server_channel.accept();
			try {
				Connection.configureChannel(channel);
			} catch (IOException e) {
				try {
					channel.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				exception = e;
			}
		}
		if (network.getDeterministic().log(exception != null))
			throw (IOException)network.getDeterministic().log(exception);
		incoming_connections.add(channel);
		notifyIncomingConnection();
	}

	public final void error(IOException e) {
		notifyError(e);
	}

	public final void incoming(InetAddress remote_address) {
		notifyIncomingConnection(remote_address);
	}
	
	private final void notifyIncomingConnection() {
		InetAddress remote_inet_address = null;
		if (!network.getDeterministic().isPlayback()) {
			SocketChannel channel = (SocketChannel)incoming_connections.get(0);
			SocketAddress remote_address = channel.socket().getRemoteSocketAddress();
			remote_inet_address = ((InetSocketAddress)remote_address).getAddress();
		}
		incoming((InetAddress)network.getDeterministic().log(remote_inet_address));
	}

	private final SocketChannel removeNextChannel() {
		return (SocketChannel)incoming_connections.remove(0);
	}
	
	private final SocketChannel getNextConnection() {
		SocketChannel channel = removeNextChannel();
		if (incoming_connections.size() > 0)
			notifyIncomingConnection();
		return channel;
	}
	
	private final SelectionKey getNextConnectionKey() {
		try {
			SocketChannel channel = getNextConnection();
			SelectionKey socket_key = channel.register(network.getSelector(), SelectionKey.OP_READ);
			return socket_key;
		} catch (ClosedChannelException e) {
			throw new RuntimeException(e);
		}
	}

	protected final AbstractConnection doAcceptConnection(ConnectionInterface conn_interface) {
		SelectionKey socket_key;
		if (!network.getDeterministic().isPlayback())
			socket_key = getNextConnectionKey();
		else
			socket_key = null;
		return new Connection(network, socket_key, conn_interface);
	}

	public final void rejectConnection() {
		try {
			SocketChannel channel = getNextConnection();
			if (!network.getDeterministic().isPlayback())
				channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final void close() {
		if (key != null && key.isValid()) {
			try {
				key.channel().close();
				while (incoming_connections.size() > 0)
					removeNextChannel().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (network.getDeterministic().log(key != null))
			network.cancelKey(key, this);
	}
	
	public final void handleError(IOException e) throws IOException {
		error(e);
	}
}
