package jthing.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import jthing.net.Peer.names;
import toools.io.Cout;
import toools.net.NetUtilities;
import toools.net.SSHUtils;
import toools.thread.Threads;

public class TCP extends IP {
	public static final int DEFAULT_PORT = IP.DEFAULT_PORT;

	private static class Entry {
		// final String name;
		final Socket socket;
		final ObjectOutputStream oos;

		Entry(Socket s) throws IOException {
			this.socket = s;
			this.oos = new ObjectOutputStream(s.getOutputStream());
		}
	}

	private final Map<Peer, Entry> peer_oos = new HashMap<>();
	private ServerSocket ss;

	@Override
	public String getName() {
		return "TCP";
	}

	@Override
	public void fill(Peer c) {
		super.fill(c);
		c.map.put(names.tcp_port.name(), getPort());
	}

	@Override
	public boolean canSendTo(Peer c) {
		return c.hasInfo(names.tcp_port);
	}

	@Override
	public void startServer() {
		while (true) {
			try {
				this.ss = new ServerSocket(getPort());
				Cout.info("TCP server listening on port " + ss.getLocalPort());

				while (true) {
					Socket socket = ss.accept();
					InputStream is = socket.getInputStream();
					ObjectInputStream in = new ObjectInputStream(is);

					new Thread(() -> {
						try {
							while (true) {
								Message msg = (Message) in.readObject();
								Peer sender = msg.route.source();
								Entry e = peer_oos.get(sender);

								if (e == null || e.socket != socket) {
									peer_oos.put(sender, new Entry(socket));
								}

								processNewMessage(msg);
							}
						}
						catch (IOException e) {
							e.printStackTrace();
							errorOn(socket);
						}
						catch (ClassNotFoundException e) {
							Cout.error("can't find class " + e.getMessage());
						}
					}).start();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				Threads.sleepMs(1000);
			}
		}
	}

	private void errorOn(Socket s) {
		try {
			s.close();
		}
		catch (IOException e1) {
		}

		peer_oos.entrySet().removeIf(e -> e.getValue().socket == s);
	}

	@Override
	public void unicastImpl(Peer from, Message msg, Peer to) {
		Entry entry = peer_oos.get(to);

		// there is no connection to this peer yet
		if (entry == null) {
			try {
				Socket socket = getSocket(to);

				if (socket != null) {
					peer_oos.put(to, entry = new Entry(socket));
				}
			}
			catch (IOException e) {
				return;
			}
		}

		if (entry != null)
			try {
				entry.oos.writeObject(msg);
				knownEmitters.messageJustReceivedFrom(msg.route.last());
			}
			catch (IOException e) {
				errorOn(entry.socket);
			}
	}

	private Socket getSocket(Peer to) {
		InetAddress ip = to.ips().get(0);
		int port = to.hasInfo(names.tcp_port.name()) ? to.getTCPPort() : TCP.DEFAULT_PORT;

		try {
			return new Socket(ip, port);
		}
		catch (IOException e) {
			int localPort = NetUtilities.findAvailablePort(1000);
			Cout.debug("creating SSH tunnel to " + ip + ":" + port + " using local port "
					+ localPort);

			String username = to.hasInfo(names.ssh_username.name()) ? to.getSSH_USERNAME()
					: System.getProperty("user.name");

			SSHUtils.createSSHTunnelTo(ip, port, username, localPort, 2000);

			try {
				return new Socket("localhost", localPort);
			}
			catch (IOException e1) {
				return null;
			}
		}
	}

	@Override
	public void stopServer() {
		try {
			ss.close();
			ss = null;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean isRunning() {
		return ss != null;
	}
}
