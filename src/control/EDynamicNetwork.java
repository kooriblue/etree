
package control;

import java.security.SecureRandom;
import java.util.Vector;

import interfaces.BasicLearningProtocol;
import interfaces.Churnable;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import utils.Exponentially;


public class EDynamicNetwork implements Control{
public static int index = 0;
public static int count = 0;
private Vector<Node> removeNodes = new Vector<Node>();
private static final String PAR_MODE = "mode";
protected final int mode;

public EDynamicNetwork(String prefix) {
	mode = Configuration.getInt(prefix + "."+PAR_MODE);
}
@Override
public boolean execute(){	
	//开始时初始化每个节点的session
	if(index == 0) {
		index++;
		for (int i = 0; i < Network.size(); i++) {
            Node node = Network.get(i);
            for (int j = 0; j < node.protocolSize(); j++) {
            	Protocol prot = node.getProtocol(j);
	            if (prot instanceof Churnable) {
			        Churnable churnableProt = (Churnable) prot;
			        //这里用81.0*3而不是27.0是因为数字太小的话网络中的节点就全部退出了，到后面就没节点了
			        //不过该参数并不影响模拟，只需要模拟的小轮也增加想同的倍数即可
			        int sessionLength = Exponentially.eRand(1/(81.0*3));
			        churnableProt.setSessionLength(sessionLength);
	            }
            }

		}
		
	}
	
	//减少session并删除节点
	for (int i = 0; i < Network.size(); i++) {
        Node node = Network.get(i);
        if ( node.getFailState() == Fallible.DOWN || node.getFailState() == Fallible.DEAD )
        	continue;
        for (int j = 0; j < node.protocolSize(); j++) {
        	Protocol prot = node.getProtocol(j);
            if (prot instanceof Churnable) {
		        Churnable churnableProt = (Churnable) prot;
		        long sessionLength = churnableProt.getSessionLength();
		        if(mode==0) {
			        if(node.getID()!=0) { 
			        	churnableProt.setSessionLength(sessionLength-1);
			        }
		        }
		        else {
		        	churnableProt.setSessionLength(sessionLength-1);
				}
		        
		        if(churnableProt.getSessionLength()<=0) {
		        	removeNodes.add((Node)node.clone());
		        	Network.remove(i);
		        	count ++;
		        }
            }
        }
	}
	
	//增加节点
	SecureRandom secureRandom = new SecureRandom();
	int pro = secureRandom.nextInt(100);
	if(pro>70) {
		for (int j = 0; j < count; j++) {
			Node newnode = null;
			int proNum = secureRandom.nextInt(100);
			//50%的概率从已经退出的节点中选择增加的节点
			if(removeNodes.size()>0 && proNum%2==0) {
				int nodeNum = secureRandom.nextInt(removeNodes.size());
				newnode = removeNodes.get(nodeNum);
				removeNodes.remove(nodeNum);
			}
			//50%的概率选择新节点
			else 
				newnode = (Node) Network.prototype.clone();
			Network.add(newnode);
		      for (int k = 0; k < newnode.protocolSize(); k++) {
		          Protocol prot = newnode.getProtocol(k);
		          if(proNum%2!=0) {
			          if(prot instanceof BasicLearningProtocol) {
			        	  BasicLearningProtocol blProt = (BasicLearningProtocol) prot;
			        	  blProt.setInstenceHolder(InstanceLoader.getInstance(40,"data/spambase/spambase.tra","data/spambase/spambase.tes"));
			          }
		          }
		          if (prot instanceof Churnable) {
				      Churnable churnableProt = (Churnable) prot;
				      //同上所说
				      int sessionLength = Exponentially.eRand(1/(81.0*3));
				      churnableProt.initSession(newnode, k);
				      churnableProt.setSessionLength(sessionLength);
		          }
		        }
		}
		count = 0;
	}
	
		
	return false;
}
}

