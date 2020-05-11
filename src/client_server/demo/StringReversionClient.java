package client_server.demo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import toools.io.FullDuplexDataConnection2;
import toools.net.TCPConnection;







public class StringReversionClient
{
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
		FullDuplexDataConnection2 c = new TCPConnection(InetAddress.getByName("localhost"), StringReversionServer.PORT, 1000);
		c.out.writeObject("This is an example text.");
		System.out.println("response: " + c.in.readObject2());
	}
}
