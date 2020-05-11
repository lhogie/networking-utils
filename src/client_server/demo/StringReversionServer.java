package client_server.demo;

import java.io.IOException;

import toools.net.TCPConnection;
import client_server.net.TCPServer;







public class StringReversionServer extends TCPServer
{
	public StringReversionServer()
	{
		super(PORT);
	}



	public static final int PORT = 62592;


	@Override
	protected void newIncomingConnection(TCPConnection connection)
	{
		try
		{
			// read the incoming string
			String s = (String) connection.in.readObject2();
			
			// reverse it in memory
			String reverse = new StringBuilder(s).reverse().toString();
			
			// send the reversed string to the client
			connection.out.writeObject(reverse);
		}
		catch( IOException e)
		{
			
		}
	}



	public static void main(String[] args) throws IOException
	{
		new StringReversionServer().startInBackground();
	}

}
