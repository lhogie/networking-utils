package jthing.queue;

import jthing.net.Message;
import toools.thread.Q;

public interface MultiQueueListener<M>
{
	void newQueue(Object name, Q<M> q);
}
