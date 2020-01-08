package control;

import com.sun.org.apache.bcel.internal.generic.NEW;

import interfaces.Model;
import interfaces.ModelHolder;
import message.ActiveThreadMessage;
import models.BoundedModelHolder;
import models.LogisticRegression;
import models.DownModelMessage;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import protocol.ETreeLearningProtocol;

//Message初始化的类
public class MessageInitializer implements Control{
	private static final String PAR_PROT = "protocol";
	private final int pid;
	private static final String PAR_DELAY = "delay";
	private final int delay;
	private static final String PAR_AGGREGATE_TIME = "aggregateTime";
	private final int aggregateTime;
    private static final String PAR_ROOT_AGGREGATE_TIME = "rootAggregateTime";
    private final int rootAggregateTime;
	
	public MessageInitializer(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		delay = Configuration.getInt(prefix + "." + PAR_DELAY, 0);
		aggregateTime = Configuration.getInt(prefix + "." + PAR_AGGREGATE_TIME, 3);
		rootAggregateTime = Configuration.getInt(prefix + "." + PAR_ROOT_AGGREGATE_TIME, 7);
	}	
	
	@Override
	public boolean execute() {
	    Model model = new LogisticRegression(57);
	    ModelHolder mh = new BoundedModelHolder(1);
	    mh.add(model);
	    for (int i = 0; i < Network.size(); i++) {
	        if ((long)i != ETreeLearningProtocol.getRoot()) {
	            EDSimulator.add(0, new DownModelMessage(Network.get((int) Network.get(i).getParentID()), mh), Network.get(i), 1);
	        }
	    }
	    
	    long[] innerIDs = ETreeLearningProtocol.getInner();
	    for (int i = 0; i < innerIDs.length; i++) {
	        EDSimulator.add(aggregateTime, ActiveThreadMessage.getInstance(), Network.get((int) innerIDs[i]), pid);
	    }
	    
	    EDSimulator.add(rootAggregateTime, ActiveThreadMessage.getInstance(), Network.get((int) ETreeLearningProtocol.getRoot()), pid);
	    
		return false;
	}
}
