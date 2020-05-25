package jthing.net;

public interface NeighborhoodListener
{
	void peerJoined(Peer peer, Neighborhood protocol);

	void peerLeft(Peer peer, Neighborhood protocol);
}
