package client_server.net.script;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import j4u.CommandLine;
import j4u.CommandLineApplication;
import toools.io.FullDuplexDataConnection2;
import toools.io.file.RegularFile;
import toools.net.TCPConnection;

public abstract class AbstractConsoleClient extends CommandLineApplication
{

	public AbstractConsoleClient(RegularFile launcher)
	{
		super(launcher);
		addOption("--hostname", "-h", ".+", null, "define the host to connect to");
		addOption("--port", "-p", "[0-9]+", getDefaultPort() + "",
				"define the port to connect to");
		addOption("--transport", "-t", "tcp|udp", "tcp",
				"define the transport layer to use");
	}

	@Override
	public int runScript(CommandLine cmdLine) throws UnknownHostException
	{

		InetAddress hostname = InetAddress
				.getByName(getOptionValue(cmdLine, "--hostname"));
		int port = Integer.valueOf(getOptionValue(cmdLine, "-p"));

		try
		{
			printMessage("Attempting to connect to host " + hostname + " on port " + port
					+ "...");
			FullDuplexDataConnection2 connection = new TCPConnection(hostname, port,
					1000);
			process(connection);
			printMessage("Connection closed");
		}
		catch (UnknownHostException e)
		{
			printFatalError("Unknown host " + hostname);
			return 1;
		}
		catch (IOException e)
		{
			printFatalError("Unable to connect");
			return 2;
		}

		return 0;
	}

	public abstract void process(FullDuplexDataConnection2 connection);

	public abstract int getDefaultPort();

}
