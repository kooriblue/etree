package interfaces;

import peersim.core.Node;

//���Ŷ�����Ľӿ�
public interface Churnable {
	public void setSessionLength(long sessionLength);
	public long getSessionLength();
	public void initSession(Node node, int protocol);
}
