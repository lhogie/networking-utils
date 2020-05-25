package jthing.queue;

import java.util.HashMap;
import java.util.Map;

import toools.thread.Q;

public class MultiQueue<M> {
	private Map<Object, Q<M>> id_queue = new HashMap<>();
	public MultiQueueListener<M> listener;

	public MultiQueue() {
		createQueue(null, true);
	}

	protected Q<M> getDefaultQueue() {
		return id_queue.get(null);
	}

	public Q<M> createQueue(Object id, boolean notifyListeners) {
		Q<M> q = new Q<>(100);
		id_queue.put(id, q);

		if (notifyListeners && listener != null) {
			new Thread(() -> listener.newQueue(id, q)).start();
		}

		return q;
	}

	public void deleteQueue(Object id) {
		id_queue.remove(id);
	}

	public Q<M> getQueue(Object id) {
		Q<M> q = id_queue.get(id);

		if (q == null) {
			q = createQueue(id, false);
		}

		return q;
	}
}
