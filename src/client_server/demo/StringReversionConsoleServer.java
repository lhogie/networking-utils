package client_server.demo;

import client_server.net.Server;
import client_server.net.script.AbstractConsoleServer;
import j4u.License;
import toools.io.file.RegularFile;



public class StringReversionConsoleServer extends AbstractConsoleServer
{

	public StringReversionConsoleServer(RegularFile launcher)
	{
		super(launcher);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getDefaultPort()
	{
		return StringReversionServer.PORT;
	}

	@Override
	protected Server createServer(int port)
	{
		return new StringReversionServer();
	}

	@Override
	public String getApplicationName()
	{
		return "javafarm";
	}

	@Override
	public String getAuthor()
	{
		return "Luc Hogie";
	}

	@Override
	public License getLicence()
	{
		return License.UNLICENSED;
	}

	@Override
	public String getYear()
	{
		return "2008-2011";
	}

	@Override
	public String getShortDescription()
	{
		return "Reverse the given string";
	}

}
