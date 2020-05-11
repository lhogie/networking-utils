package client_server.net;

import java.io.IOException;

import toools.log.Logger;
import toools.log.StdOutLogger;

public abstract class Server
{
	private Logger logger;
	private int port;

	public Server(int port)
	{
		this.port = port;
		logger = StdOutLogger.SYNCHRONIZED_INSTANCE;
	}

	public int getPort()
	{
		return port;
	}

	public Logger getLogger()
	{
		return logger;
	}

	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	private Thread listenThread = null;

	public void startInBackground()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					startInTheForeground();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		thread.setName("ServerListen@" + getPort());
		listenThread = thread;
		thread.start();
	}

	protected abstract void startInTheForeground() throws IOException, ClassNotFoundException;

	public void stop()
	{
		if (listenThread != null)
			listenThread.interrupt();
	}
}
