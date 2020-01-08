package utils;

//论文中的三种Merge
public class Merge {
	public static DenseVector MergeNone(double t,DenseVector dv,double tr,DenseVector dvr) {
		return (DenseVector)dvr.clone();
	}
	
	public static DenseVector MergeAverage(double t,DenseVector dv,double tr,DenseVector dvr) {
		if(tr==0) 
			return dv;
		DenseVector tmp = new DenseVector(dv.getSize());
		double alpha = 1.0*tr/(t+tr);
		tmp.Add(dvr,alpha);
		tmp.Add(dv,1 - alpha);
		return tmp;
	}
	
	public static DenseVector MergeSubsampled(double t,DenseVector dv,double tr,DenseVector dvr) {
		if(tr==0) 
			return dv;
		DenseVector tmp = (DenseVector)dv.clone();
		double alpha = 1.0*tr/(t+tr);
		//System.out.println(alpha);
		for (int i = 0; i < dvr.getSize(); i++) {
			if(dvr.getValue(i) != 0.0) {
				double value = (1-alpha)*dv.getValue(i) + alpha*dvr.getValue(i);
				tmp.setValue(i, value);
			}

		}
		return tmp;
	}
}
