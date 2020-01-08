package utils;

//论文中的两种Compress
public class Compress {
	public static DenseVector CompressNone(DenseVector v) {
		return v;
	}
	
	public static DenseVector CompressSubsampling(DenseVector v,int[] d) {
		DenseVector tmp = new DenseVector(v.getSize());
		for (int i = 0; i < d.length; i++) {
			tmp.setValue(d[i], v.getValue(d[i]));
		}
		return tmp;
	}
}
