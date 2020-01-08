package protocol;

import java.security.SecureRandom;
import java.util.Vector;

import interfaces.AbstractProtocol;
import interfaces.ModelHolder;
import interfaces.Model;
import models.*;
import utils.*;
import peersim.config.Configuration;


//Gossip学习协议
public class GossipLearningProtocol extends AbstractProtocol{
	private static final String PAR_MODELHOLDERNAME = "modelHolderName";
	private static final String PAR_MODELNAME = "modelName";
	private static final String PAR_COMPRESS = "compress";
	
	private final String modelHolderName;
	private final String modelName;
	private ModelHolder models;
	//网络带宽
	private static int bandwidth = 200;
	//压缩倍数
	private final int compress;
	
	public GossipLearningProtocol(String prefix) {
		modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
		modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
		compress = Configuration.getInt(prefix + "." + PAR_COMPRESS);
		init(prefix);
	}
	
	public GossipLearningProtocol(String prefix, double delayMean, double delayVar, String modelHolderName, String modelName,int compress) {
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
	    } 
	    catch (Exception e) {
	    	throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": " + e);
	    }
	}
	
	//传输模型给随机邻居
	@Override
	public void activeThread(int mode) {
		if (models != null && models.size() > 0){
			Model m = models.getModel(models.size() - 1);
			LogisticRegression lg = (LogisticRegression)m.clone();
			DenseVector dv = (DenseVector)lg.getWeight().clone();
			SecureRandom secureRandom = new SecureRandom();
			boolean[] bool = new boolean[dv.getSize()];
			int randInt = 0;
			int length = (int)Math.ceil(dv.getSize()/compress);
			int[] d = new int[length];
			for (int i = 0; i < length; i++) {
				do {
					randInt = secureRandom.nextInt(dv.getSize());
				}while(bool[randInt]);
				bool[randInt] = true;
				d[i] = randInt;
			}
			dv = Compress.CompressSubsampling(dv, d);
			lg.setWeight(dv);
			bandwidth -= length;
		    ModelHolder mh = new BoundedModelHolder(1);
		    mh.add(lg);
		    sendToRandomNeighbor(new DownModelMessage(currentNode, mh));
		}
	}
	
	//接收到邻居传来的模型并进行进一步地训练去更新本地模型
	@Override
	public void passiveDownModelMsg(DownModelMessage message) {
		for (int i = 0; message != null && i < message.size(); i ++) {
			if(bandwidth<=0)
				return;
			Model model = message.getModel(i);
			LogisticRegression tmp = (LogisticRegression)models.getModel(models.size()-1).clone();
			LogisticRegression lg = (LogisticRegression)model;
			double t = tmp.getAge();
			double tr = lg.getAge();
			DenseVector dv = Merge.MergeNone(t, tmp.getWeight(), tr, lg.getWeight());
			double age = Math.max(t,tr);
			tmp.setWeight(dv);
			tmp.setAge(age);
			
			
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
			
			models.add(tmp);
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
		return new GossipLearningProtocol(prefix, delayMean, delayVar, modelHolderName, modelName,compress);
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

    @Override
    public void passiveUpModelMsg(UpModelMessage message) {
        // TODO Auto-generated method stub
        
    }


}
