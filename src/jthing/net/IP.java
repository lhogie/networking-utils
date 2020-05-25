package jthing.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class IP extends Protocol {
	public static final int DEFAULT_PORT = 4553;

	private int port = DEFAULT_PORT;
	private Thread thread;

	@Override
	protected void start() {
		if (isRunning())
			throw new IllegalStateException();

		thread = new Thread(() -> startServer());
		thread.start();
	}

	@Override
	protected void stop() {
		stopServer();

		try {
			thread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;

		if (isRunning()) {
			stop();
			start();
		}
	}

	protected abstract boolean isRunning();

	protected abstract void stopServer();

	protected abstract void startServer();

	@Override
	public void fill(Peer c) {
		try {
			c.ips().add(InetAddress.getLocalHost());
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static boolean isThisMyIpAddress(InetAddress addr) {
		// Check if the address is a valid special local or loop back
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
			return true;

		// Check if the address is defined on any interface
		try {
			return NetworkInterface.getByInetAddress(addr) != null;
		}
		catch (SocketException e) {
			return false;
		}
	}

}
