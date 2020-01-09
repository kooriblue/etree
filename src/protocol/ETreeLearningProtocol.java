package protocol;

import java.security.SecureRandom;
import java.util.Vector;

import com.sun.javafx.image.IntToBytePixelConverter;

import interfaces.AbstractProtocol;
import interfaces.Model;
import interfaces.ModelHolder;
import models.BoundedModelHolder;
import models.LogisticRegression;
import models.UpModelMessage;
import models.DownModelMessage;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.transport.Transport;
import utils.DenseVector;
import utils.Aggregate;
import utils.Compress;

//联邦学习协议
public class ETreeLearningProtocol extends AbstractProtocol{
	private static final String PAR_MODELHOLDERNAME = "modelHolderName";
	private static final String PAR_MODELNAME = "modelName";
	private static final String PAR_COMPRESS = "compress";
	
	private final String modelHolderName;
	private final String modelName;
	
	private ModelHolder models; // 该节点作为叶节点保存的模型
	private ModelHolder innerModel; // 该节点作为第二层节点所保存的模型
	
	private static ModelHolder mainModels; // 根节点收到的模型
	private static ModelHolder[] innerModels; // 第二层节点收到的模型
	//网络带宽
	private static int bandwidth = 200;
	//压缩倍数
	private final int compress;
	
	// 树的根节点
	private static long rootID;
	// 第二层节点
	private static long[] innerIDs;
	
	// 第二层节点数
	private static int numOfInnerNodes;
	
	public ETreeLearningProtocol(String prefix) {
		modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
		modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
		compress = Configuration.getInt(prefix + "." + PAR_COMPRESS);
		init(prefix);
	}
	
	public ETreeLearningProtocol(String prefix, double delayMean, double delayVar, String modelHolderName, String modelName,int compress) {
	    this.prefix = prefix;
	    this.delayMean = delayMean;
	    this.delayVar = delayVar;
	    this.modelHolderName = modelHolderName;
	    this.modelName = modelName;
	    this.compress = compress;
	    init(prefix);
	}
	
	protected void init(String prefix) {
	    try {
	    	super.init(prefix);
	    	models = (ModelHolder)Class.forName(modelHolderName).newInstance();
	    	models.init(prefix);
	    	Model model = (Model)Class.forName(modelName).newInstance();
	    	model.init(prefix);
	    	models.add(model);
	    	
            innerModel = (ModelHolder)Class.forName(modelHolderName).newInstance();
            innerModel.init(prefix);
            Model m = (Model)Class.forName(modelName).newInstance();
            m.init(prefix);
            innerModel.add(m);
	    } 
	    catch (Exception e) {
	    	throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": " + e);
	    }
	}
	
	/*
	   *聚合并分发模型给所有子节点
	   *mode: 0-根节点聚合，1-内节点聚合分发到叶节点，2-内节点聚合发到根节点
	 */
	@Override
	public void activeThread(int mode) {
		if (models != null && models.size() > 0){
		    long ID = currentNode.getID();
		    
		    // 根节点聚合模型发给第二层节点
		    if(ID == rootID) {
		    	if(mainModels.size()>=1) {
			    	LogisticRegression lg = (LogisticRegression)models.getModel(models.size()-1).clone();
			    	double sumAge = 0.0;
			    	DenseVector[] dvs = new DenseVector[mainModels.size()];
			    	for (int i = 0; i < mainModels.size(); i++) {
			    		LogisticRegression tmp =  (LogisticRegression)mainModels.getModel(i);
			    		sumAge += tmp.getAge();
			    		dvs[i] = (DenseVector)tmp.getWeight().clone();
					}
			    	sumAge /= mainModels.size();
			    	lg.addAge(sumAge);
			    	DenseVector tmp = Aggregate.AggregateSubsampledImproved(dvs);
			    	lg.Add(tmp, -1.0);
			    	models.add(lg);
			    	mainModels = new BoundedModelHolder(numOfInnerNodes);
		    	}
		    	
		    	CommonState.setTime(currentTime);
		    	
				Model m = innerModel.getModel(innerModel.size() - 1);
			    ModelHolder mh = new BoundedModelHolder(1);
			    mh.add(m);
		    	for (int i = 0; i < innerIDs.length; i++) {
		    	    sendToNeighbor(new DownModelMessage(currentNode, mh), Network.get((int) innerIDs[i]));
		    	}
		    }
		    else {
		        int index = 0;
		        for (int i = 0; i < innerIDs.length; i++) {
		            if (ID == innerIDs[i]) {
		                index = i;
		                break;
		            }
		        }
		        
		        // 聚合
		        if(innerModels[index].size()>=1) {
                    LogisticRegression lg = (LogisticRegression)innerModel.getModel(innerModel.size()-1).clone();
                    double sumAge = 0.0;
                    DenseVector[] dvs = new DenseVector[innerModels[index].size()];
                    for (int i = 0; i < innerModels[index].size(); i++) {
                        LogisticRegression tmp =  (LogisticRegression)innerModels[index].getModel(i);
                        sumAge += tmp.getAge();
                        dvs[i] = (DenseVector)tmp.getWeight().clone();
                    }
                    sumAge /= innerModels[index].size();
                    lg.addAge(sumAge);
                    DenseVector tmp = Aggregate.AggregateSubsampledImproved(dvs);
                    lg.Add(tmp, -1.0);
                    innerModel.add(lg);
                    innerModels[index] = new BoundedModelHolder(((Linkable)Network.get((int) innerIDs[index]).getProtocol(2)).degree());
                }
                
                CommonState.setTime(currentTime);
                
                Model m = innerModel.getModel(innerModel.size() - 1);
                ModelHolder mh = new BoundedModelHolder(1);
                mh.add(m);
                
		        // 发送给叶节点
		        if (mode == 1) {
	                sendToWholeNeighbor(new DownModelMessage(currentNode, mh)); 
		        }
		        // 发送给根节点
		        else {
		            sendBack(new UpModelMessage(currentNode, mh), rootID);
		        }
		    }
		}
	}
	
	// 收到上层节点模型的操作（根节点到第二层节点，第二层节点到叶节点）
	@Override
	public void passiveDownModelMsg(DownModelMessage message) {
		for (int i = 0; message != null && i < message.size(); i ++) {
			long ID = currentNode.getID();
            
            long srcID = message.getSrc().getID();
            //第二层节点接收到根节点模型，下发给叶节点
            if (srcID == rootID) {
                Model model = message.getModel(i);
                ModelHolder mh = new BoundedModelHolder(1);
                mh.add(model);
                sendToWholeNeighbor(new DownModelMessage(currentNode, mh));
            }
            // 叶节点接收到第二层节点模型，更新本地模型，sendback
            else {
                Model model = message.getModel(i);
                LogisticRegression tmp = (LogisticRegression)model.clone();
                DenseVector tmpDv = (DenseVector)tmp.getWeight().clone();
                SecureRandom secureRandom = new SecureRandom();
                //Mini-batch
                int batchsize = 10;
                for (int j = 0; j < instances.getSize()/batchsize; j++) {
                    Vector<DenseVector> tmpX = new Vector<DenseVector>();
                    Vector<Double> tmpY = new Vector<Double>();
                    for (int k = 0; k < batchsize; k++) {
                        tmpX.add(instances.getInstance(j*batchsize+k));
                        tmpY.add(instances.getLabel(j*batchsize+k));
                    }
                    tmp.update(tmpX,tmpY);
                }
                tmpDv.Add(tmp.getWeight(), -1.0);
                LogisticRegression lg = (LogisticRegression)model.clone();
                DenseVector dv = (DenseVector)tmpDv.clone();
                boolean[] bool = new boolean[dv.getSize()];
                int randInt = 0;
                int length = (int)Math.ceil(dv.getSize()/compress);
                int[] d = new int[length];
                for (int j = 0; j < length; j++) {
                    do {
                        randInt = secureRandom.nextInt(dv.getSize());
                    }while(bool[randInt]);
                    bool[randInt] = true;
                    d[j] = randInt;
                }
                
                dv = Compress.CompressSubsampling(dv, d);
                lg.setWeight(dv);
                lg.setAge(instances.getSize());
                
                models.add(tmp);
                if(bandwidth<0) {
                    return;
                }
                bandwidth -= length;
                ModelHolder mh = new BoundedModelHolder(1);
                mh.add(lg);
                sendBack(new UpModelMessage(currentNode, mh), currentNode.getParentID());
            }
		}
	}
	
	// 收到下层节点模型的操作（叶节点到第二层节点，第二层节点到根节点）
    @Override
    public void passiveUpModelMsg(UpModelMessage message) {
        for (int i = 0; message != null && i < message.size(); i ++) {
            long ID = currentNode.getID();
            
            // 根节点收到第二层节点的模型
            if(ID == rootID) {
                Model model = message.getModel(i);
                mainModels.add(model);
            }
            // 第二层节点收到叶节点的模型
            else {
                int index = 0;
                for (int j = 0; j < innerIDs.length; j++) {
                    if (ID == innerIDs[j]) {
                        index = j;
                        break;
                    }
                }
                
                Model model = message.getModel(i);
                innerModels[index].add(model);
            }
        }
    }
	
	@Override
	public int size() {
		return (models == null) ? 0 : 1;
	}
	
	@Override
	public ModelHolder getModelHolder(int index) {
		return models;
	}
	
	@Override
	public void setModelHolder(int index, ModelHolder modelHolder) {
		this.models = modelHolder;
	}
	
	@Override
	public boolean add(ModelHolder modelHolder) {
		setModelHolder(0, modelHolder);
		return true;
	}
	
	@Override
	public ModelHolder remove(int index) {
		ModelHolder tmp = models;
		models = null;
	    return tmp;
	}

	@Override
	public Object clone() {
		return new ETreeLearningProtocol(prefix, delayMean, delayVar, modelHolderName, modelName, compress);
	}
	
	public int getBandwidth() {
		return bandwidth;
	}
	
	public void resetBandwidth() {
		this.bandwidth = 200;
	}
	
	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}
	
	public static void setRoot(int ID) {
	    rootID = ID;
	}
	
	public static void setInner(int[] IDs) {
	    numOfInnerNodes = IDs.length;
	    if (innerIDs == null) {
	        innerIDs = new long[numOfInnerNodes];
	    }
	    for (int i = 0; i < numOfInnerNodes; i++) {
	        innerIDs[i] = IDs[i];
	    }
	    
	    mainModels = new BoundedModelHolder(numOfInnerNodes);
	    innerModels = new BoundedModelHolder[numOfInnerNodes];
	    for (int i = 0; i < numOfInnerNodes; i++) {
            innerModels[i] = new BoundedModelHolder(((Linkable)Network.get((int) innerIDs[i]).getProtocol(2)).degree());
        }
	}
	
	public static long getRoot() {
	    return rootID;
	}
	
	public static long[] getInner() {
	    return innerIDs;
	}
}
