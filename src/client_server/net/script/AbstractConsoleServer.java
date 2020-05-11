package client_server.net.script;

import java.io.IOException;

import client_server.net.Server;
import j4u.CommandLine;
import j4u.CommandLineApplication;
import toools.io.file.RegularFile;

public abstract class AbstractConsoleServer extends CommandLineApplication
{
	public AbstractConsoleServer(RegularFile launcher)
	{
		super(launcher);
		addOption("--port", "-p", "[0-9]+", getDefaultPort() + "",
				"define the port to listen to");

	}

	@Override
	public int runScript(CommandLine cmdLine) throws IOException
	{
		int port = Integer.valueOf(getOptionValue(cmdLine, "-p"));
		createServer(port).startInBackground();
		return 0;
	}

	public abstract int getDefaultPort();

	protected abstract Server createServer(int port);

}
