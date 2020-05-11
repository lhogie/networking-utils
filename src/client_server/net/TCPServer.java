package client_server.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import toools.io.IORuntimeException;
import toools.net.TCPConnection;

/**
 * Abstract base class for all servers that accept TCP connections and
 * start a new thread for each incoming connection.<br>
 * 
 * Launched threads are created using the function
 * {@link #createThread(Runnable)} and are
 * given a name returned by the function {@link #buildThreadName(Socket)}.<br>
 * 
 * The thread objects are instance of {@link TCPServerThread} or a subclass of
 * it. This class
 * has a single field named {@link TCPServerThread#stopThread} that can be set
 * to true with the
 * function {@link TCPServerThread#requestStop()} and the running thread can
 * call
 * {@link TCPServerThread#mustStop()} to know the current state of this field.
 * 
 * @see #newIncomingConnection(TCPConnection)
 * 
 * @author Luc Hogie
 * @author Nicolas Chleq
 *
 */
public abstract class TCPServer extends Server
{
	public TCPServer(int port)
	{
		super(port);
	}

	private ServerSocket serverSocket;
	protected AtomicInteger nbStartedThreads = new AtomicInteger(0);
	protected AtomicInteger nbFinishedThreads = new AtomicInteger(0);
	protected AtomicInteger nbJoinedThreads = new AtomicInteger(0);
	private List<TCPServerThread> finishedThreads = Collections
			.synchronizedList(new ArrayList<TCPServerThread>());

	protected TCPServerThread createThread()
	{
		return new TCPServerThread();
	}

	protected TCPServerThread createThread(Runnable target)
	{
		return new TCPServerThread(target);
	}

	protected TCPServerThread getCurrentThread()
	{
		Thread current = Thread.currentThread();
		
		if (current instanceof TCPServerThread)
			return (TCPServerThread) current;
		
		return null;
	}

	/**
	 * Called in a separate thread each time a new connection is established
	 * with the server.
	 * 
	 * @param connection a TCPConnection used to communicate with the client
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected abstract void newIncomingConnection(TCPConnection connection)
			throws IOException, ClassNotFoundException;

	protected String buildThreadName(Socket socket)
	{
		return "TCPServer@" + getPort() + "/" + socket.getInetAddress().getHostName()
				+ ":" + socket.getPort();
	}

	@Override
	public void startInTheForeground() throws IOException, ClassNotFoundException
	{
		serverSocket = new ServerSocket(getPort());

		while ( ! mustStop())
		{
			Socket socket = null;
			try
			{
				socket = serverSocket.accept();
			}
			catch (SocketException e)
			{ // happens if the socket is closed
				if (e.getMessage().compareTo("Socket closed") == 0)
					break;
				else
				{
					e.printStackTrace();
					throw e;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				throw e;
			}
			final TCPConnection connection = new TCPConnection(socket);

			TCPServerThread thread = createThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						newIncomingConnection(connection);
					}
					catch (EOFException e)
					{
						if (getCurrentThread().mustStop())
							return;
						else
						{
							e.printStackTrace();
							throw new IORuntimeException(e);
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
						throw new IORuntimeException(e);
					}
					catch (IORuntimeException e)
					{
						if (e.getCause() != null)
						{
							// connection was closed, nothing to process
							// anymore.
							if (e.getCause() instanceof EOFException)
							{
								if (getCurrentThread().mustStop())
									return;
							}
						}
						e.printStackTrace();
						throw e;
					}
					catch (ClassNotFoundException e)
					{
						e.printStackTrace();
						throw new IORuntimeException(e);
					}
					finally
					{
						// System.err.println("Thread TCPServer@" + getPort() +
						// " Exiting.");
						//connection.close();
						synchronized (finishedThreads)
						{
							finishedThreads.add(getCurrentThread());
						}
						nbFinishedThreads.incrementAndGet();
					}
				}
			});
			thread.setName(buildThreadName(socket));
			// synchronized (threads)
			// {
			// threads.add(thread);
			// }
			nbStartedThreads.incrementAndGet();
			thread.start();
			// join with the finished threads
			synchronized (finishedThreads)
			{
				for (TCPServerThread fThread : finishedThreads)
				{
					try
					{
						fThread.join(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					nbJoinedThreads.incrementAndGet();
				}
				finishedThreads.clear();
			}
		}
		serverSocket.close();
	}

	private boolean stopRequested = false;

	protected boolean mustStop()
	{
		return stopRequested;
	}

	@Override
	public void stop()
	{
		if (serverSocket == null)
			throw new IllegalArgumentException("server not started");
		stopRequested = true;
		super.stop();
		// for (TCPServerThread thread : threads)
		// {
		// thread.requestStop();
		// thread.interrupt();
		// }
		// for (TCPServerThread thread : threads)
		// {
		// try
		// {
		// thread.join();
		// }
		// catch (InterruptedException e)
		// {
		// }
		// }
		try
		{
			serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.out);
		}
		System.out.println(getClass().getName() + "@" + getPort() + " shutdown.");
	}

	/**
	 * A subclass of Thread that implements a simple mechanism to allow the
	 * thread to
	 * terminate in a clean way if needed. The thread can call the
	 * {@link #mustStop()}
	 * function to know if it has to terminate. Other threads can call the
	 * {@link #requestStop()}
	 * function to ask for the thread to terminate.<br>
	 * This class can be instantiated with an instance of {@link Runnable}, or
	 * sub-classed with
	 * a redefinition of the {@link #run()} function. When instantiated with a
	 * {@link Runnable}
	 * the thread must call the {@link TCPServer#getCurrentThread()} function to
	 * get its
	 * {@link TCPServerThread} instance in order to be able to use the
	 * {@link #mustStop()} function.
	 * 
	 * @author Nicolas Chleq
	 *
	 */
	public static class TCPServerThread extends Thread
	{
		public TCPServerThread()
		{
			super();
		}

		public TCPServerThread(Runnable target)
		{
			super(target);
		}

		private boolean stopThread = false;

		public boolean mustStop()
		{
			return stopThread;
		}

		public void requestStop()
		{
			stopThread = true;
		}
	}
}
