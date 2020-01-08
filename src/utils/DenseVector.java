package utils;

import interfaces.Vector;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

//稠密的向量类
public class DenseVector implements Serializable, Iterable<Vector>, Comparable<DenseVector>  {

	private static final long serialVersionUID = -5812502132483668485L;

	private static int defaultStorage= 16;
	private double[] values;
	
	
	public DenseVector() {
		this(defaultStorage);
	}
	
	public DenseVector(int storage) {
		values = new double[storage];
		for (int i = 0; i < values.length; i++) {
			values[i] = 0.0;
		}
	}
	
	public DenseVector(DenseVector dv) {
		this(dv.values.length);
		System.arraycopy(dv.values, 0, values, 0, dv.values.length);
	}
	
	public DenseVector(double[] vector) {
		this(vector.length);
		for (int i = 0; i < vector.length; i++) {
				values[i] = vector[i];
		}
	}
	
	public Object clone() {
		return new DenseVector(this);
	}
	
	public double getValue(int index) {
		return values[index];
	}
	
	public void setValue(int index,double value) {
		values[index] = value;
	}
	
	public double[] getValues() {
		return values;
	}
	
	public DenseVector Add(DenseVector ds,double alpha) {
		if(alpha==0.0)
			return this;
		
		for (int i = 0; i < values.length; i++) {
			this.values[i] += ds.values[i]*alpha;
		}
		
		return this;
	}
	
	public DenseVector Add(DenseVector ds) {
		return this.Add(ds,1.0);
	}
	
	public DenseVector Mul(double alpha) {
		for (int i = 0; i < values.length; i++) {
			values[i] *= alpha;
		}
		return this;
	}
	
	public double Mul(DenseVector ds,double alpha) {
		double res = 0.0;
		for (int i = 0; i < values.length; i++) {
			res += values[i]*ds.values[i]*alpha;
		}
		return res;
	}
	
	public double Mul(DenseVector ds) {
		double res = 0.0;
		for (int i = 0; i < values.length; i++) {
			res += values[i]*ds.values[i];
		}
		return res;
	}
	
	public String toString() {
		return Arrays.toString(values);
	}

	public int getSize() {
		return values.length;
	}
	
	
	
	@Override
	public int compareTo(DenseVector ds) {
		for (int i = 0; i < Math.min(values.length,ds.values.length); i++) {
			if(values[i]<ds.values[i])
				return -1;
			else if (values[i]>ds.values[i]) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	public Iterator<Vector> iterator() {
		return null;
	}
	
}
