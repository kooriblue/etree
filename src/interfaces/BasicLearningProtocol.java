package interfaces;


import models.InstanceHolder;
import models.UpModelMessage;
import models.DownModelMessage;

//基本的学习类接口
public interface BasicLearningProtocol {
	public int size();
	
	public void activeThread(int mode);
	public void passiveDownModelMsg(DownModelMessage message);
    public void passiveUpModelMsg(UpModelMessage message);
	
	public InstanceHolder getInstanceHolder();
	public void setInstenceHolder(InstanceHolder instances);
	
	public ModelHolder getModelHolder(int index);
	public void setModelHolder(int index, ModelHolder modelHolder);
	public boolean add(ModelHolder modelHolder);
	public ModelHolder remove(int index);
	
	public void resetBandwidth();
	
	public void setBandwidth(int bandwidth);
	public int getBandwidth();
}
