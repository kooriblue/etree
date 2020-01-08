package utils;

//论文中的三种Aggregate
public class Aggregate {
	public static DenseVector AggregateDefault(DenseVector[] dv) {
		DenseVector tmp = new DenseVector(dv[0].getSize());
		int size = dv.length;
		for (int i = 0; i < size; i++) {
			tmp.Add(dv[i]);
		}
		tmp.Mul(1.0/size);
		return tmp;
	}
	
	public static DenseVector AggregateSubsampled(DenseVector[] dv,int d,int s) {
		DenseVector tmp = new DenseVector(dv[0].getSize());
		int size = dv.length;
		for (int i = 0; i < size; i++) {
			tmp.Add((DenseVector)dv[i].clone());
		}
		tmp.Mul(1.0*d/(size*s));
		
		return tmp;
	}
	
	public static DenseVector AggregateSubsampledImproved(DenseVector[] dv) {
		DenseVector tmp = new DenseVector(dv[0].getSize());
		int size = dv.length;
		for (int i = 0; i < dv[0].getSize(); i++) {
			double value = 0.0; // sum of values
			int count = 0; // subsampled dimensions
			for (int j = 0; j < size; j++) {
				double  tmpValue = dv[j].getValue(i);
				if(tmpValue != 0.0) {
					value += tmpValue;
					count++;
				}
			}
			if(count!=0)
				value /= count;
			tmp.setValue(i, value);
		}
		return tmp;
	}
}
