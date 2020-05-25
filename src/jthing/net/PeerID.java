package jthing.net;

import java.io.Serializable;

public class PeerID implements Serializable
{
	public final Object value;

	public PeerID(Object value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return value.toString();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return ((PeerID) o).value.equals(value);
	}
	
	@Override
	public int hashCode()
	{
		return value.hashCode();
	}
	
}
