package jthing.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MultiProtocol extends Protocol
{
	private final List<Protocol> protocols = new ArrayList<>();
	private final Map<Object, Protocol> peer_protocol = new HashMap<>();
	private boolean run = false;

	private final Consumer<Message> localConsumer = msg -> {
		if ( ! run)
			return;

		if ( ! peer_protocol.containsKey(msg.route.last()))
		{
			peer_protocol.put(msg.route.last(), msg.receptionProtocol);
		}

		processNewMessage(msg);
	};

	public void addProtocol(Protocol p)
	{
		protocols.add(p);
	}

	@Override
	public void unicastImpl(Peer from, Message msg, Peer to)
	{
		if ( ! run)
			return;

		if (peer_protocol.containsKey(to))
		{
			peer_protocol.get(to).unicastImpl(from, msg, to);
		}
		else
		{
			// send through all protocols
			for (Protocol p : protocols)
			{
				if (p.canSendTo(to))
				{
					p.unicastImpl(from, msg, to);
				}
			}
		}
	}

	@Override
	public void setNewMessageConsumer(Consumer<Message> newConsumer)
	{
		super.setNewMessageConsumer(newConsumer);
		protocols.forEach(p -> p.setNewMessageConsumer(localConsumer));
	}

	@Override
	public String getName()
	{
		StringBuilder b = new StringBuilder();
		protocols.forEach(p -> b.append(p.getName() + "/"));
		return b.substring(0, b.length() - 1);
	}

	@Override
	public boolean canSendTo(Peer c)
	{
		return ! Protocol.findProtocolsWhichCanDealWith(c, protocols).isEmpty();
	}

	@Override
	public void fill(Peer c)
	{
		protocols.forEach(p -> p.fill(c));
	}

	@Override
	protected void start()
	{
		run = true;
	}

	@Override
	protected void stop()
	{
		run = false;
	}

}
