package jthing.net;

import java.util.HashMap;
import java.util.Map;

import toools.io.ser.JavaSerializer;

public class LMI extends Protocol
{
	public static final Map<Object, LMI> name_peer = new HashMap<>();
	private boolean run;

	@Override
	synchronized public void unicastImpl(Peer from, Message msg, Peer to)
	{
		Message clone = (Message) JavaSerializer.getDefaultSerializer().clone(msg);
		LMI p = name_peer.get(to);

		if (p != null && run && p.run)
		{
			p.processNewMessage(clone);
		}
	}

	@Override
	public String getName()
	{
		return "LMI";
	}

	@Override
	public boolean canSendTo(Peer c)
	{
		return name_peer.containsKey(c.getID());
	}

	@Override
	public void fill(Peer c)
	{
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
