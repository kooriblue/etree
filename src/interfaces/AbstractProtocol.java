package interfaces;

import models.InstanceHolder;
import models.UpModelMessage;
import models.DownModelMessage;

import com.sun.org.apache.bcel.internal.generic.NEW;

import message.*;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import protocol.ETreeLearningProtocol;

//抽象学习类
public abstract class AbstractProtocol implements EDProtocol, BasicLearningProtocol{
	protected static final String PAR_DELAYMEAN = "delayMean";
	protected double delayMean = Double.POSITIVE_INFINITY;
	
	protected static final String PAR_DELAYVAR = "delayVar";
	protected double delayVar = 1.0;

    protected static final String PAR_AGGREGATE_TIME = "aggregateTime";
    protected double aggregateTime = 3.0;

    protected static final String PAR_AGGREGATE_COUNT = "aggregateCount";
    protected int aggregateCount = 2;

    protected static final String PAR_ROOT_AGGREGATE_TIME = "rootAggregateTime";
    protected double rootAggregateTime = 7.0;
    
    protected static final String PAR_INNER_TO_ROOT_DELAY = "innerToRootDelay";
    protected double innerToRootDelay = 1.0;
    
	protected InstanceHolder instances;
	
	protected Node currentNode;
	protected int currentProtocolID = -1;
	protected long currentTime = -1;
	protected String prefix;
	
	protected long sessionLength = 10000;
	protected int sessionID = 0;

	// 已聚合的次数，每满一次aggregateCount清零
	private int aggregatedCount = 0;
	
	protected void init(String prefix) {
		this.prefix = prefix;
		delayMean = Configuration.getDouble(prefix + "." + PAR_DELAYMEAN, Double.POSITIVE_INFINITY);
		delayVar = Configuration.getDouble(prefix + "." + PAR_DELAYVAR, 1.0);
		aggregateTime = Configuration.getDouble(prefix + "." + PAR_AGGREGATE_TIME, 3.0);
		aggregateCount = Configuration.getInt(prefix + "." + PAR_AGGREGATE_COUNT, 2);
		rootAggregateTime = Configuration.getDouble(prefix + "." + PAR_ROOT_AGGREGATE_TIME, 7);
	}
	
	@Override
	public abstract Object clone();
	
	//将模型发送给一个随机的邻居
	protected void sendToRandomNeighbor(DownModelMessage message) {
		message.setSrc(currentNode);
		Linkable overlay = getOverlay();
		Node randomNode = overlay.getNeighbor(CommonState.r.nextInt(overlay.degree()));
		getTransport().send(currentNode, randomNode, message, currentProtocolID);
	}
	
	//将模型发送给指定邻居
	protected void sendToNeighbor(DownModelMessage message, Node dest) {
	    message.setSrc(currentNode);
	    getTransport().send(currentNode, dest, message, currentProtocolID);
	}
	
	
	protected void sendToWholeNeighbor(DownModelMessage message) {
        message.setSrc(currentNode);
        Linkable overlay = getOverlay();
        Node randomNode = null; // 获取当前节点的第一个邻居
        for (int i = 0; i < overlay.degree(); i++) {
            randomNode = overlay.getNeighbor(i);
            getTransport().send(currentNode, randomNode, message, currentProtocolID);
        }
    }
	
	//联邦学习中子节点将训练好的模型传回主节点
//	protected void sendBack(DownModelMessage message) {
//		message.setSrc(currentNode);
//		Node mainNode = Network.get(0);
//		getTransport().send(currentNode, mainNode, message, currentProtocolID);
//	}
	
	protected void sendBack(UpModelMessage message, long rootID) {
        message.setSrc(currentNode);
        Node destNode = Network.get((int) rootID);
        getTransport().send(currentNode, destNode, message, currentProtocolID);
    }
	
	//ED协议中需要实现的主要方法processEvent
	public void processEvent(Node currentNode, int currentProtocolID, Object messageObj, long time) {
		this.currentNode = currentNode;
		this.currentProtocolID = currentProtocolID;
		this.currentTime = time;
			    
		if ( messageObj instanceof ActiveThreadMessage || (messageObj instanceof OnlineSessionMessage && 
		((OnlineSessionMessage)messageObj).sessionID == sessionID) ) {
		    
		    // 是根节点，将已聚合次数重置为0
		    if (currentNode.getID() == ETreeLearningProtocol.getRoot()) {
		        activeThread(0);
		        EDSimulator.add((long) rootAggregateTime, new OnlineSessionMessage(sessionID), currentNode, currentProtocolID);
		    }
		    // 是内节点，已聚合次数加1
		    else {
		        aggregatedCount++;
		        // 未达到aggregateCount
		        if (aggregatedCount % aggregateCount != 0) {
		            activeThread(1);
		            EDSimulator.add((long) aggregateTime, new OnlineSessionMessage(sessionID), currentNode, currentProtocolID);
		        }
		        // 达到aggregateCount
		        else {
		            activeThread(2);
		            EDSimulator.add((long)(aggregateTime+innerToRootDelay), new OnlineSessionMessage(sessionID), currentNode, currentProtocolID);
		        }
		    }

//			if (!Double.isInfinite(delayMean)) {
//				int delay = (int)(delayMean + CommonState.r.nextGaussian()*delayVar);
//				delay = (delay > 0) ? delay : 1;
//				EDSimulator.add(delay, new OnlineSessionMessage(sessionID), currentNode, currentProtocolID);
//			}
		} 
		else if (messageObj instanceof DownModelMessage) {
		    
			if (currentTime >= CommonState.getTime()) {
			    passiveDownModelMsg((DownModelMessage)messageObj);
			}
			
		} else if (messageObj instanceof UpModelMessage) {
		    
		    if (currentTime >= CommonState.getTime()) {
		        passiveUpModelMsg((UpModelMessage)messageObj);
		    }
		    
		}
			
	}
	
	protected Linkable getOverlay() {
		return (Linkable) currentNode.getProtocol(FastConfig.getLinkable(currentProtocolID));
	}
	
	protected Transport getTransport() {
		return ((Transport) currentNode.getProtocol(FastConfig.getTransport(currentProtocolID)));
	}
	
	protected BasicLearningProtocol getCurrentProtocol() {
	    return (BasicLearningProtocol) currentNode.getProtocol(currentProtocolID);
	}
	
	public int getPID() {
		if (currentProtocolID < 0) {
			throw new RuntimeException("The PID is small than 0!");
		}
		return currentProtocolID;
	}
	
	public void setInstenceHolder(InstanceHolder instances) {
		this.instances = instances;
	}
	
	public InstanceHolder getInstanceHolder() {
		return instances;
	}
	
	public long getSessionLength() {
		return sessionLength;
	}
	
	public void setSessionLength(long sessionLength) {
		this.sessionLength = sessionLength;
	}
	
	public void initSession(Node node, int protocol) {
		sessionID ++;
		EDSimulator.add(0, new OnlineSessionMessage(sessionID), node, protocol);
	}
	

	
}
