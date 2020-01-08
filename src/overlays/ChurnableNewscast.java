package overlays;

import interfaces.Churnable;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.extras.mj.ednewscast.CycleMessage;
import peersim.extras.mj.ednewscast.EdNewscast;

//有扰动的EdNewscast类
public class ChurnableNewscast extends EdNewscast implements Churnable{
	protected int cacheSize;
	private long sessionLength = 10000;
	private final String prefix;
	protected final int mode;
	private static final String PAR_MODE = "mode";
	
	public ChurnableNewscast(String prefix) {
	    super(prefix);
	    cacheSize = Configuration.getInt(prefix + ".cache");
	    mode = Configuration.getInt(prefix + "."+PAR_MODE);
	    this.prefix = prefix;
	}
	
	public ChurnableNewscast clone() {
		return new ChurnableNewscast(prefix);
	}
	
	public long getSessionLength() {
		return sessionLength;
	}
	
	public void setSessionLength(long sessionLength) {
		this.sessionLength = sessionLength;
	}
	
	//给各个节点分配邻居
	public void initSession(Node node, int protocol) {
		deleteNeighbors();
		long ID = node.getID();
		//联邦学习
		if(mode==0) {
			if(ID == 0) {
				while (degree() < cacheSize) {
					int onlineNeighbor = CommonState.r.nextInt(Network.size());
			    	if ( Network.get(onlineNeighbor).getFailState() != Fallible.DOWN
			          && Network.get(onlineNeighbor).getFailState() != Fallible.DEAD
			          && Network.get(onlineNeighbor).getID() != node.getID())
			    		addNeighbor(Network.get(onlineNeighbor));
			    }
			}
			else 
				addNeighbor(Network.get(0));
		}
		
		//Gossip学习
		if(mode==1) {
			while (degree() < cacheSize) {
				int onlineNeighbor = CommonState.r.nextInt(Network.size());
		    	if ( Network.get(onlineNeighbor).getFailState() != Fallible.DOWN
		          && Network.get(onlineNeighbor).getFailState() != Fallible.DEAD
		          && Network.get(onlineNeighbor).getID() != node.getID())
		    		addNeighbor(Network.get(onlineNeighbor));
			}
		}
		


	    EDSimulator.add(0, CycleMessage.inst, node, protocol);
	}
}
