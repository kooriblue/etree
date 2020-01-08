
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
	//��ʼʱ��ʼ��ÿ���ڵ��session
	if(index == 0) {
		index++;
		for (int i = 0; i < Network.size(); i++) {
            Node node = Network.get(i);
            for (int j = 0; j < node.protocolSize(); j++) {
            	Protocol prot = node.getProtocol(j);
	            if (prot instanceof Churnable) {
			        Churnable churnableProt = (Churnable) prot;
			        //������81.0*3������27.0����Ϊ����̫С�Ļ������еĽڵ��ȫ���˳��ˣ��������û�ڵ���
			        //�����ò�������Ӱ��ģ�⣬ֻ��Ҫģ���С��Ҳ������ͬ�ı�������
			        int sessionLength = Exponentially.eRand(1/(81.0*3));
			        churnableProt.setSessionLength(sessionLength);
	            }
            }

		}
		
	}
	
	//����session��ɾ���ڵ�
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
	
	//���ӽڵ�
	SecureRandom secureRandom = new SecureRandom();
	int pro = secureRandom.nextInt(100);
	if(pro>70) {
		for (int j = 0; j < count; j++) {
			Node newnode = null;
			int proNum = secureRandom.nextInt(100);
			//50%�ĸ��ʴ��Ѿ��˳��Ľڵ���ѡ�����ӵĽڵ�
			if(removeNodes.size()>0 && proNum%2==0) {
				int nodeNum = secureRandom.nextInt(removeNodes.size());
				newnode = removeNodes.get(nodeNum);
				removeNodes.remove(nodeNum);
			}
			//50%�ĸ���ѡ���½ڵ�
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
				      //ͬ����˵
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

