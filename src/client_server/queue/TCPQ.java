package client_server.queue;

import java.io.IOException;
import java.net.ServerSocket;

import client_server.net.TCPServer;
import toools.net.TCPConnection;

public class TCPQ extends Q
{
	public TCPQ(int port)
	{
		super(port);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				TCPServer s = new TCPServer(port)
				{
					
					@Override
					protected void newIncomingConnection(TCPConnection connection)
							throws IOException, ClassNotFoundException
					{
						connection.getInputStream().readObject2();
					}
				};
			}

		}).start();
	}

	@Override
	public Object getOrWait()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
