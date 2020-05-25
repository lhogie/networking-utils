package jthing.queue;

import java.util.function.Consumer;

import jthing.net.Message;
import toools.thread.Q;

public class DeliverToQueue implements Consumer<QMessage>
{
	private final MultiQueue<Message> queues;
	private final MultiQueueListener<Message> listener;

	public DeliverToQueue(MultiQueue<Message> queues, MultiQueueListener<Message> l)
	{
		this.queues = queues;
		this.listener = l;
	}

	@Override
	public void accept(QMessage msg)
	{
		Q<Message> q = queues.getQueue(msg.targetQueueId);

		if (q == null)
		{
			q = queues.createQueue(msg.targetQueueId, true);

			if (listener != null)
				listener.newQueue(msg.targetQueueId, q);
		}

		q.add_blocking(msg);
	}
}
