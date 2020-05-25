package jthing.net;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Protocol {
	public final Neighborhood knownEmitters = new Neighborhood(this, 2000);
	private Consumer<Message> messageConsumer;

	protected void processNewMessage(Message msg) {
		knownEmitters.messageJustReceivedFrom(msg.route.last());
		msg.receptionDate = System.currentTimeMillis();
		msg.receptionProtocol = this;
		messageConsumer.accept(msg);
	}

	public abstract String getName();

	public abstract boolean canSendTo(Peer c);

	public abstract void fill(Peer c);

	protected abstract void unicastImpl(Peer from, Message msg, Peer to);

	public final void unicast(Peer from, Message msg, Peer to) {
		if ( ! canSendTo(to))
			throw new IllegalArgumentException(this + " can't send to peer " + to);

		msg.route.add(from);
		unicastImpl(from, msg, to);
	}

	public void multicast(Peer from, Message msg, Set<Peer> to) {
		to = new HashSet<>(to);
		Iterator<Peer> i = to.iterator();

		while (i.hasNext()) {
			if ( ! canSendTo(i.next())) {
				i.remove();
			}
		}

		msg.route.add(from);
		multicastImpl(from, msg, to);
	}

	protected void multicastImpl(Peer from, Message msg, Set<Peer> to) {
		new ArrayList<>(to).stream().forEach(r -> unicastImpl(from, msg, r));
	}

	public void setNewMessageConsumer(Consumer<Message> newConsumer) {
		Consumer<Message> formerConsumer = messageConsumer;

		if (newConsumer == null) {
			if (formerConsumer != null) {
				stop();
			}

			this.messageConsumer = newConsumer;
		}
		else {
			this.messageConsumer = newConsumer;

			if (formerConsumer == null) {
				start();
			}
		}
	}

	protected abstract void start();

	protected abstract void stop();

	public static List<Protocol> findProtocolsWhichCanDealWith(Peer c,
			List<Protocol> protocols) {
		return protocols.stream().filter(p -> p.canSendTo(c))
				.collect(Collectors.toList());
	}

}
