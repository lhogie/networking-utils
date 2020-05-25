package jthing.net;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final int ID = ThreadLocalRandom.current().nextInt();
	public final Route route = new Route();
	public final Route suggestedRoute = new Route();
	private Peer recipient;
	public final Object content;
	public int coverage = Integer.MAX_VALUE;
	public long expirationDate = Long.MAX_VALUE;
	public final long emissionDate = System.currentTimeMillis();
	public long receptionDate;

	public transient Protocol receptionProtocol;

	public Message(Object content)
	{
		this.content = content;
	}

	@Override
	public int hashCode()
	{
		return ID;
	}

	public boolean isExpired()
	{
		return System.currentTimeMillis() > expirationDate;
	}

	@Override
	public String toString()
	{
		return " ID: " + ID + ", route: " + route + ", " + ", content: " + content;
	}
}
