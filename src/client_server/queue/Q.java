package client_server.queue;

public abstract class Q
{
	private final int port;

	public Q(int port)
	{
		this.port = port;
	}

	public abstract Object getOrWait();
}
