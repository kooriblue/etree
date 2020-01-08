package interfaces;

import java.io.Serializable;

//基本模型保存类的接口
public interface ModelHolder extends Serializable, Cloneable{
	public void init(String prefix);
	
	public Object clone();
	
	public int size();
	
	public Model getModel(final int index);
	
	public void setModel(final int index,final Model model);
	
	public boolean add(final Model model);
	
	public Model remove(final int index);
	
	public void clear();
}
