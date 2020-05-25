package jthing.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Route extends ArrayList<Peer>
{
	public Peer source()
	{
		return get(0);
	}

	public Peer last()
	{
		return get(size() - 1);
	}

	public Route reverse()
	{
		Route r = new Route();
		r.addAll(this);
		Collections.reverse(r);
		return r;
	}

	public int nbCycles()
	{
		return size() - new HashSet<>(this).size();
	}
}
