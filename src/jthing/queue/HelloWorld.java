package jthing.queue;

import java.net.UnknownHostException;

import jthing.net.AddressBook;
import jthing.net.Peer;
import jthing.net.UDP;
import toools.thread.Threads;

public class HelloWorld
{
	public static void main(String[] args) throws UnknownHostException
	{
		for (int i = 0; i < 10; ++i)
		{
			new HelloWorld(i);
		}
	}

	public HelloWorld(int port) throws UnknownHostException
	{
		UDP protocol = new UDP();
		protocol.setPort(port);
		Peer me = new Peer();
		protocol.fill(me);
		System.out.println("I'm alive: " + me);

		// adds only the peer with port number+1 to address book
		AddressBook addressBook = new AddressBook();
		addressBook.addLocalPeerByPort(port + 1);

		MultiQueue<QMessage> queues = new MultiQueue<>();

		// new messages are placed to the null queue
		protocol.setNewMessageConsumer(
				msg -> queues.getQueue(null).add_blocking((QMessage) msg));

		// and each new message addition in the null queue triggers a display
		queues.getQueue(null).msg2event(msg -> System.out.println(
				me + " just received : " + msg.content + " from " + msg.route.source()),
				() -> true);

		// every second, says hello to everyone in the address book
		Threads.loop(1000, () -> true, () -> addressBook.getCards().forEach(
				c -> protocol.unicast(me, new QMessage("Hello World!", null, null), c)));
	}
}
