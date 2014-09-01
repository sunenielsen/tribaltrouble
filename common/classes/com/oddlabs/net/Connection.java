package com.oddlabs.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import com.oddlabs.event.Deterministic;
import com.oddlabs.event.NotDeterministic;

public final strictfp class Connection extends AbstractConnection implements Handler, ConnectionPeerInterface {
	public final static int BUFFER_SIZE = 16382;
	private final static short HEADER_SIZE = 2;

	private final ByteBuffer read_buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final List back_log_list = new LinkedList();
	private final ByteBuffer write_buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	private final ConnectionPeerInterface peer_interface;
	private final boolean ping_reply;
	private final NetworkSelector network;
	private final ARMIInterfaceMethods interface_methods = new ARMIInterfaceMethods(ConnectionPeerInterface.class);
	private boolean writing = false;
	private boolean pinged = false;
	private SelectionKey key;
	private InetAddress local_address;

	public final static void configureChannel(SocketChannel channel) throws IOException {
		channel.configureBlocking(false);
		channel.socket().setTcpNoDelay(true);
	}

	private Connection(NetworkSelector network, boolean ping_reply) {
		this.network = network;
		network.registerForPingTimeout(this);
		this.ping_reply = ping_reply;
		ARMIEventWriter event_writer = new ARMIEventWriter() {
			public final void handle(ARMIEvent event) {
				if (back_log_list.size() > 0 || !writeEvent(event))
					back_log_list.add(event);
			}
		};
		this.peer_interface = (ConnectionPeerInterface)ARMIEvent.createProxy(event_writer, ConnectionPeerInterface.class);
		peer_interface.ping();
	}
	
	public Connection(NetworkSelector network, String dns_name, int port, ConnectionInterface connection_interface) {
		this(network, true);
		setConnectionInterface(connection_interface);
		network.asyncConnect(dns_name, port, this);
	}

	public Connection(NetworkSelector network, SocketAddress address, ConnectionInterface connection_interface) {
		this(network, true);
		setConnectionInterface(connection_interface);
		connect(address);
	}

	Connection(NetworkSelector network, SelectionKey key, ConnectionInterface connection_interface) {
		this(network, false);
		network.registerForPing(this);
		setConnectionInterface(connection_interface);
		notifyConnected();
		setKey(key);
	}

	public final void doPing() {
		if (isConnected())
			peer_interface.ping();
	}

	public final void ping() {
		if (ping_reply)
			peer_interface.ping();
		pinged = true;
	}

	public final void timeout() {
		if (!isConnected())
			return;
		if (!pinged) {
			handleError(new IOException("Connection timed out"));
		} else {
			pinged = false;
			network.registerForPingTimeout(this);
		}
	}

	public final void connected(InetAddress local_address) {
		this.local_address = local_address;
		notifyConnected();
	}

	public final InetAddress getLocalAddress() {
		return local_address;
	}

	public final void error(IOException e) {
		notifyError(e);
	}

	public final void connect(SocketAddress socket_address) {
		IOException exception = null;
		InetAddress local_address = null;
		SelectionKey key = null;
		if (!network.getDeterministic().isPlayback()) {
			Selector selector = network.getSelector();
			try {
				SocketChannel channel = SocketChannel.open();
				try {
					configureChannel(channel);
					boolean success = channel.connect(socket_address);
					if (success) {
						key = channel.register(selector, SelectionKey.OP_READ);
						local_address = channel.socket().getLocalAddress();
					} else {
						key = channel.register(selector, SelectionKey.OP_CONNECT);
					}
				} catch (IOException e) {
					channel.close();
					throw e;
				}
			} catch (IOException e) {
				exception = e;
			}
		}
		setKey(key);
		if (writing)
			doSetWriting();
		else
			doResetWriting();
		if (network.getDeterministic().log(exception != null))
			error((IOException)network.getDeterministic().log(exception));
		else if (network.getDeterministic().log(local_address != null))
			connected((InetAddress)network.getDeterministic().log(local_address));
	}

	public final void dnsError(IOException e) {
		error(e);
	}
	
	private final void setKey(SelectionKey key) {
		assert key != null || network.getDeterministic().isPlayback();
		this.key = key;
		network.attachToKey(key, this);
	}

	private final void doSetWriting() {
		if (!network.getDeterministic().isPlayback()) {
			int new_ops = key.interestOps() | SelectionKey.OP_WRITE;
			key.interestOps(new_ops);
		}
	}

	private final void doResetWriting() {
		if (!network.getDeterministic().isPlayback()) {
			int new_ops = key.interestOps() & ~SelectionKey.OP_WRITE;
			key.interestOps(new_ops);
		}
	}

	private final void resetWriting() {
		assert writing;
		if (isKeyValid())
			doResetWriting();
		writing = false;
	}

	private final void setWriting() {
		if (writing)
		   return;
		if (isKeyValid())
			doSetWriting();
		writing = true;
	}

	public final void handle(ARMIEvent event) {
		peer_interface.receiveEvent(event);
	}

	private final boolean writeNextEvent() {
		if (back_log_list.size() == 0)
			return false;
		ARMIEvent event = (ARMIEvent)back_log_list.get(0);
		boolean success = writeEvent(event);
		if (success)
			back_log_list.remove(0);
		return success;
	}

	private final void writeBackLog() {
		while (writeNextEvent())
			;
	}

	private void writeToChannel(SocketChannel channel) throws IOException {
		int bytes_written;
		do {
			bytes_written = 0;
			write_buffer.flip();
			if (write_buffer.remaining() > 0) {
				if (!network.getDeterministic().isPlayback())
					bytes_written = channel.write(write_buffer);
				bytes_written = network.getDeterministic().log(bytes_written);
				int new_position = network.getDeterministic().log(write_buffer.position());
				write_buffer.position(new_position);
			}
			write_buffer.compact();
			writeBackLog();
		} while (bytes_written > 0);
		if (write_buffer.position() == 0) {
			resetWriting();
			writeBufferDrained();
		}
	}

	private final boolean writeEvent(ARMIEvent event) {
		short event_size = (short)event.getEventSize();
		int total_event_size = event_size + HEADER_SIZE;
		assert total_event_size <= write_buffer.capacity();
		boolean fits = total_event_size <= write_buffer.remaining();
		if (fits) {
			write_buffer.putShort(event_size);
			event.write(write_buffer);
		}
		// Avoid differently sized serializing (we don't care about the write_buffer contents at playback anyway)
		int new_position = network.getDeterministic().log(write_buffer.position());
		write_buffer.position(new_position);
		setWriting();
		return fits;
	}

	private int readDeterministic(SocketChannel channel) throws IOException {
		int old_position = read_buffer.position();
		int num_bytes_read = -1;
		IOException exception;
		try {
			if (!network.getDeterministic().isPlayback())
				num_bytes_read = channel.read(read_buffer);
			exception = null;
		} catch (IOException e) {
			exception = e;
		}
		if (network.getDeterministic().log(exception != null))
			throw (IOException)network.getDeterministic().log(exception);
		else
			num_bytes_read = network.getDeterministic().log(num_bytes_read);
		int new_position = network.getDeterministic().log(read_buffer.position());
		int old_limit = read_buffer.limit();
		read_buffer.limit(new_position);
		read_buffer.position(old_position);
		network.getDeterministic().log(read_buffer);
		assert read_buffer.position() == new_position && !read_buffer.hasRemaining() : read_buffer.position() + " " + new_position + " " + !read_buffer.hasRemaining();
		read_buffer.limit(old_limit);
		return num_bytes_read;
	}

	private void readFromChannel(SocketChannel channel) throws IOException {
		boolean bytes_read = true;
		do {
			int num_bytes_read = readDeterministic(channel);
			if (num_bytes_read == -1)
				throw new IOException("Channel closed");
			bytes_read = num_bytes_read > 0;
			read_buffer.flip();
			if (read_buffer.remaining() >= HEADER_SIZE) {
				short event_size = read_buffer.getShort(read_buffer.position());
				if (event_size > read_buffer.capacity() - HEADER_SIZE)
					handleError(new IOException("Message too large: " + event_size));
				if (read_buffer.remaining() >= event_size + HEADER_SIZE) {
					read_buffer.position(read_buffer.position() + HEADER_SIZE);
					ARMIEvent event = ARMIEvent.read(read_buffer, event_size);
					network.getDeterministic().checkpoint();
					try {
						event.execute(interface_methods, this);
					} catch (IllegalARMIEventException e) {
						IOException ioe = new IOException();
						ioe.initCause(e);
						throw ioe;
					}
					bytes_read = true;
				}
			}
			read_buffer.compact();
		} while (bytes_read && network.getDeterministic().log(network.getDeterministic().isPlayback() || channel.isOpen()));
	}

	public final void handle() throws IOException {
		SocketChannel channel;
		if (!network.getDeterministic().isPlayback())
			channel = (SocketChannel)key.channel();
		else
			channel = null;
		network.getDeterministic().checkpoint();
		if (network.getDeterministic().log(network.getDeterministic().isPlayback() || !channel.isConnected())) {
			boolean success = false;
			IOException exception;
			try {
				if (!network.getDeterministic().isPlayback())
					success = channel.finishConnect();
				exception = null;
			} catch (IOException e) {
				exception = e;
			}
			if (network.getDeterministic().log(exception != null))
				throw (IOException)network.getDeterministic().log(exception);
			else
				success = network.getDeterministic().log(success);
			assert success; // finishConnect should always succeed (or throw), because we are called on OP_CONNECT
			int interest_ops;
			if (network.getDeterministic().isPlayback())
				interest_ops = network.getDeterministic().log(-1);
			else
				interest_ops = network.getDeterministic().log(key.interestOps());
			int new_ops = (interest_ops | SelectionKey.OP_READ) & ~SelectionKey.OP_CONNECT;
			if (!network.getDeterministic().isPlayback())
				key.interestOps(new_ops);
			connected((InetAddress)network.getDeterministic().log(network.getDeterministic().isPlayback() ? null : channel.socket().getLocalAddress()));
		} else {
			network.getDeterministic().checkpoint();
			if (writing)
				writeToChannel(channel);
			readFromChannel(channel);
		}
	}
	
	private final boolean isKeyValid() {
		// double negation because we want to the common result to be false, the default logger value
		return !network.getDeterministic().log(network.getDeterministic().isPlayback() || !(key != null && key.isValid()));
	}

	protected final void doClose() {
		if (isKeyValid()) {
			network.cancelKey(key, this);
			if (!network.getDeterministic().isPlayback()) {
				SocketChannel channel = (SocketChannel)key.channel();
				try {
					channel.socket().shutdownInput();
				} catch (IOException e) {
					// ignore
				}
				try {
					channel.socket().shutdownOutput();
				} catch (IOException e) {
					// Ignore
				}
				try {
					channel.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		network.unregisterForPinging(this);
	}
	
	public void handleError(IOException e) {
		error(e);
	}
}
