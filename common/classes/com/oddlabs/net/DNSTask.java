package com.oddlabs.net;

import java.net.*;
import java.io.*;

public final strictfp class DNSTask implements Callable {
	private final String dns_name;
	private final int port;
	private final Connection connection;

	public DNSTask(String dns_name, int port, Connection conn) {
		this.dns_name = dns_name;
		this.port = port;
		this.connection = conn;
	}

	public void taskCompleted(Object result) {
		connection.connect((InetSocketAddress)result);
	}
	
	public void taskFailed(Exception e) {
		connection.dnsError((IOException)e);
	}

	/* WARNING: Potentially threaded and not deterministic. See Callable.java for details */
	public final Object call() throws Exception {
		InetAddress inet_address = InetAddress.getByName(dns_name);
		InetSocketAddress address = new InetSocketAddress(inet_address, port);
		return address;
	}
}

