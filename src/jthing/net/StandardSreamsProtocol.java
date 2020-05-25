package jthing.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import toools.thread.Threads;

public class StandardSreamsProtocol extends Protocol
{
	final ObjectOutputStream oos;
	final ObjectInputStream ios;

	public StandardSreamsProtocol() throws IOException
	{
		oos = new ObjectOutputStream(System.out);
		ios = new ObjectInputStream(System.in);
	}

	private boolean run = false;

	@Override
	public void unicastImpl(Peer from, Message msg, Peer to)
	{
		if ( ! run)
			return;

		try
		{
			oos.writeObject(msg);
		}
		catch (IOException e)
		{
		}
	}

	@Override
	public String getName()
	{
		return "stdin/stdout message communication";
	}

	@Override
	public boolean canSendTo(Peer c)
	{
		return true;
	}

	@Override
	public void fill(Peer c)
	{
	}

	@Override
	public void start()
	{
		run = true;

		Threads.loop(() -> {
			try
			{
				Message msg = (Message) ios.readObject();
				processNewMessage(msg);
			}
			catch (IOException | ClassNotFoundException e)
			{
			}
		});
	}

	@Override
	protected void stop()
	{
		// TODO Auto-generated method stub

	}
}
