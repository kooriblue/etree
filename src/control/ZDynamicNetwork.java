
package control;

import java.security.SecureRandom;
import java.util.Vector;

import interfaces.BasicLearningProtocol;
import interfaces.Churnable;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;


public class ZDynamicNetwork implements Control{
//论文中每个大轮中down和up节点的百分比
public static double[] up = {5.256410256410251, 8.076923076923082, 8.076923076923082, 8.461538461538462, 8.717948717948714, 8.589743589743588, 8.333333333333334, 7.948717948717955, 7.307692307692307, 7.307692307692307, 7.820512820512828, 8.076923076923082, 7.948717948717955, 8.461538461538462, 8.717948717948714, 8.717948717948714, 8.974358974358983, 9.358974358974363, 9.487179487179489, 9.871794871794869, 10.128205128205138, 10.512820512820515, 9.999999999999995, 8.589743589743588, 7.948717948717955, 8.076923076923082, 8.076923076923082, 8.461538461538462, 8.717948717948714, 8.589743589743588, 8.333333333333334, 7.948717948717955, 7.307692307692307, 7.307692307692307, 7.692307692307686, 8.205128205128208, 7.948717948717955, 8.589743589743588, 8.717948717948714, 8.717948717948714, 8.974358974358983, 9.358974358974363, 9.615384615384615, 9.871794871794869, 10.128205128205138, 10.512820512820515, 10.128205128205138, 8.589743589743588};
public static double[] down = {5.5128205128205146, 7.564102564102562, 7.564102564102562, 8.717948717948719, 9.487179487179487, 10.000000000000002, 9.102564102564104, 8.846153846153847, 7.948717948717949, 7.948717948717949, 8.589743589743591, 9.487179487179487, 9.102564102564104, 9.23076923076923, 9.23076923076923, 9.23076923076923, 8.974358974358974, 8.846153846153847, 9.102564102564104, 8.974358974358974, 8.846153846153847, 8.717948717948719, 8.717948717948719, 8.333333333333332, 7.948717948717949, 7.435897435897436, 7.564102564102562, 8.717948717948719, 9.358974358974358, 10.000000000000002, 9.102564102564104, 8.846153846153847, 7.948717948717949, 7.948717948717949, 8.589743589743591, 9.487179487179487, 9.102564102564104, 9.23076923076923, 9.23076923076923, 9.487179487179487, 9.102564102564104, 8.846153846153847, 9.102564102564104, 8.974358974358974, 8.846153846153847, 8.717948717948719, 8.717948717948719, 8.46153846153846}; 

public static int count = 0;
public static int index = 0;
private int downNum = -1;
private int upNum = -1;
private int[] downTime;
private int[] upTime;
private Vector<Node> removeNodes = new Vector<Node>();
private static final String PAR_MODE = "mode";
protected final int mode;

public ZDynamicNetwork(String prefix) {
	mode = Configuration.getInt(prefix + "."+PAR_MODE);
}
@Override
public boolean execute(){	
	SecureRandom secureRandom = new SecureRandom();
	if(count == 0 && index == 0) {
		downNum = (int)Math.ceil(Network.size()*down[index]/100);
		upNum = (int)Math.ceil(Network.size()*up[index]/100);
		downTime = new int[downNum];
		upTime = new int[upNum];
		
		//随机选出在每20小轮中down的时间
		for (int i = 0; i < downNum; i++) {
			downTime[i] = secureRandom.nextInt(20);
		}
		
		//随机选出在每20小轮中up的时间
		for (int i = 0; i < upNum; i++) {
			upTime[i] = secureRandom.nextInt(20);
		}
	}
	//删除节点（联邦学习中不能删除中心节点）
	for (int i = 0; i < downNum; i++) {
		if(downTime[i] == count) {
			int removeNum = CommonState.r.nextInt(Network.size());
			if(mode==0) {
				while(removeNum == 0)
					removeNum = CommonState.r.nextInt(Network.size());
			}
			removeNodes.add((Node) Network.get(removeNum).clone());
			Network.remove(removeNum);
		}
	}
	
	//增加节点
	for (int i = 0; i < upNum; i++) {
		if(upTime[i] == count) {
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
		    	for (int j = 0; j < newnode.protocolSize(); j++) {
		    		Protocol prot = newnode.getProtocol(j);
		    		if(proNum%2!=0) {
		    			if(prot instanceof BasicLearningProtocol) {
		    				BasicLearningProtocol blProt = (BasicLearningProtocol) prot;
		        	  	  	blProt.setInstenceHolder(InstanceLoader.getInstance(40,"data/spambase/spambase.tra","data/spambase/spambase.tes"));
		    			}  
		    		}

		    		if (prot instanceof Churnable) {
		    			Churnable churnableProt = (Churnable) prot;
		    			churnableProt.setSessionLength(10000);
		    			churnableProt.initSession(newnode, j);
		    		}
		        }
		}
	}
	
	count++;
	
	//每一大轮过后更新downtime和uptime
	if(count == 21) {
		count = 0;		
		index ++;
		downNum = (int)Math.ceil(Network.size()*down[index]/100);
		upNum = (int)Math.ceil(Network.size()*up[index]/100);
		downTime = new int[downNum];
		upTime = new int[upNum];
		for (int i = 0; i < downNum; i++) {
			downTime[i] = secureRandom.nextInt(20);
		}
		
		for (int i = 0; i < upNum; i++) {
			upTime[i] = secureRandom.nextInt(20);
		}
	}
		
		

	
	return false;
}
}

