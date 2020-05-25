package jthing.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import jthing.net.Peer.names;
import toools.io.Cout;
import toools.io.ser.Serializer;
import toools.thread.Threads;

public class UDP extends IP {
	public static final int DEFAULT_PORT = IP.DEFAULT_PORT;

	private DatagramSocket socket;

	@Override
	public String getName() {
		return "UDP";
	}

	@Override
	public void fill(Peer c) {
		super.fill(c);
		c.set(names.udp_port.name(), getPort());
	}

	@Override
	public void unicastImpl(Peer from, Message msg, Peer to) {
//		Cout.debugSuperVisible("sending to " + to);

		if (socket == null)
			return;

		byte[] buf = Serializer.getDefaultSerializer().toBytes(msg);
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		p.setAddress(to.ips().get(0));
		p.setPort(to.getUDPPort());

		try {
			socket.send(p);
//			Cout.debugSuperVisible("packet sentt " + p);
		}
		catch (IOException e1) {
		}
	}

	@Override
	public boolean canSendTo(Peer c) {
		return c.hasInfo(names.udp_port.name());
	}

	@Override
	protected void startServer() {
		byte[] buf = new byte[1000000];

		while (true) {
			try {
				socket = new DatagramSocket(getPort());
				Cout.info("UDP server listening on port " + socket.getLocalPort());

				while (true) {
					DatagramPacket p = new DatagramPacket(buf, buf.length);

					try {
						socket.receive(p);
						Message msg = (Message) Serializer.getDefaultSerializer()
								.fromBytes(p.getData());
						processNewMessage(msg);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				Threads.sleepMs(1000);
			}
		}
	}

	@Override
	protected void stopServer() {
		socket.close();
		socket = null;
	}

	@Override
	protected boolean isRunning() {
		return socket != null;
	}

}
