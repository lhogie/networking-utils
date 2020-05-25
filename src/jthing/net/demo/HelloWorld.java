package jthing.net.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jthing.net.IP;
import jthing.net.Message;
import jthing.net.Peer;
import jthing.net.Peer.names;
import jthing.net.PeerID;
import jthing.net.UDP;
import toools.thread.Threads;

public class HelloWorld {
	public static void main(String[] args) throws UnknownHostException {
		int port = 4000;

		for (int i = 0; i < 20; ++i) {
			new HelloWorld(port++);
		}
	}

	public HelloWorld(int port) throws UnknownHostException {
		Peer me = new Peer();
		me.set(names.id.name(), new PeerID(port));
		me.set(names.tcp_port.name(), new PeerID(port));
		me.ips().add(InetAddress.getLoopbackAddress());

		IP messaging = new UDP();
		messaging.setPort(port);

		// what to do when a new message arrives
		messaging.setNewMessageConsumer(msg -> System.out.println(
				me + "> just received from " + msg.route.source() + " via protocol "
						+ msg.receptionProtocol.getName() + ": " + msg.content));

		Peer neighbor = new Peer();
		neighbor.set(names.id.name(), new PeerID(port + 1));
		neighbor.ips().add(me.ips().get(0));
		neighbor.set(names.udp_port.name(), port + 1);

		// every second, says hello to everyone in the address book
		Threads.loop(1000, () -> true,
				() -> messaging.unicast(me, new Message("Hello World!"), neighbor));
	}
}
