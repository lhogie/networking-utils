package jthing.net;

import java.net.InetAddress;

public class IPAddress {
	public InetAddress ip;
	public int port;

	public IPAddress(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
	}
}
