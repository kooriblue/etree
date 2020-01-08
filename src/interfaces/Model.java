package interfaces;

import utils.DenseVector;
import java.io.Serializable;
import java.util.Vector;

//基本模型类接口
public interface Model extends Serializable, Cloneable{
	
	public void init(String prefix);
	
	public Object clone();
	
	public void update(DenseVector dv,double label);
	
	public int getNumOfClasses();
	
	public void setNumOfClasses(int numOfClasses);
}
