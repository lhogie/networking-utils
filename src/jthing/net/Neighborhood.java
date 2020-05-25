package jthing.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import toools.thread.Threads;

public class Neighborhood {
	private final Protocol protocol;
	private final Map<Peer, Long> name_lastSeenDate = new HashMap<>();
	private final long timeOutMs;
	public final List<NeighborhoodListener> listeners = new ArrayList<NeighborhoodListener>();

	public Neighborhood(Protocol protocol, long timeOutMs) {
		this.protocol = protocol;
		this.timeOutMs = timeOutMs;
		Threads.loop(timeOutMs, () -> true, () -> removeOutDated());
	}

	private synchronized void removeOutDated() {
		Iterator<Entry<Peer, Long>> i = name_lastSeenDate.entrySet().iterator();

		while (i.hasNext()) {
			Entry<Peer, Long> e = i.next();

			if (System.currentTimeMillis() - e.getValue() > timeOutMs) {
				i.remove();
				listeners.forEach(l -> l.peerLeft(e.getKey(), this));
			}
		}
	}

	public synchronized void messageJustReceivedFrom(Peer peer) {
		if ( ! name_lastSeenDate.containsKey(peer)) {
			listeners.forEach(l -> l.peerJoined(peer, this));
		}

		name_lastSeenDate.put(peer, System.currentTimeMillis());
	}

	public synchronized void seenDead(Peer peer) {
		if (name_lastSeenDate.containsKey(peer)) {
			name_lastSeenDate.remove(peer);
			listeners.forEach(l -> l.peerLeft(peer, this));
		}
	}

	public Set<Peer> peers() {
		return name_lastSeenDate.keySet();
	}
}