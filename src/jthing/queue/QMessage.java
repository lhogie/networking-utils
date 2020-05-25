package jthing.queue;

import jthing.net.Message;

public class QMessage extends Message
{
	private static final long serialVersionUID = 1L;

	public final Object targetQueueId;
	public final Object returnTargetQueueId;

	public QMessage(Object content, Object targetQueueID, Object returnTargetQueue)
	{
		super(content);
		this.targetQueueId = targetQueueID;
		this.returnTargetQueueId = returnTargetQueue;
	}
	
	@Override
	public String toString()
	{
		String s= super.toString();
		s += ", targetQueue: " + targetQueueId;
		
		if (returnTargetQueueId != null)
		{
			s += ", returnTargetQueue: " + returnTargetQueueId;
		}
		
		return s;
	}
}
