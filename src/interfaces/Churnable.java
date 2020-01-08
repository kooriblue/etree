package interfaces;

import peersim.core.Node;

//可扰动的类的接口
public interface Churnable {
	public void setSessionLength(long sessionLength);
	public long getSessionLength();
	public void initSession(Node node, int protocol);
}
