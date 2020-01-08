package models;

import utils.DenseVector;
import java.io.Serializable;
import java.util.Vector;

//´æ´¢ÊµÀýµÄÀà
public class InstanceHolder implements Serializable {

	private static final long serialVersionUID = -1528144531778004024L;

	private int size;
	private Vector<DenseVector> instances;
	private Vector<Double> labels;
	private final int numOfClasses;
	private final int numOfFeatures;
	
	public InstanceHolder(int numOfClasses,int numOfFeatures) {
		this.numOfClasses = numOfClasses;
		this.numOfFeatures = numOfFeatures;
		size = 0;
		instances = new Vector<DenseVector>();
		labels = new Vector<Double>();
	}
	
	public InstanceHolder(int numOfClasses,int numOfFeatures,Vector<DenseVector> instances, Vector<Double> labels) {
		this.numOfClasses = numOfClasses;
		this.numOfFeatures = numOfFeatures;
		this.size = instances.size();
		this.instances = instances;
		this.labels = labels;
	}
	
	public InstanceHolder(InstanceHolder ih) {
		numOfClasses = ih.numOfClasses;
		numOfFeatures = ih.numOfFeatures;
		size = ih.size;
		instances = new Vector<DenseVector>();
		labels = new Vector<Double>();
		for (int i = 0; i < size; i++) {
			instances.add((DenseVector)ih.instances.get(i).clone());
			labels.add((double)ih.labels.get(i));
		}
	}
	
	public Object clone() {
		return new InstanceHolder(this);
	}
	
	public int getSize() {
		return this.size;
	}
	
	public Vector<DenseVector> getInstances(){
		return this.instances;
	}
	
	public DenseVector getInstance(int index){
		return this.instances.get(index);
	}
	
	public Vector<Double> getLabels(){
		return this.labels;
	}
	
	public double getLabel(int index) {
		return this.labels.get(index);
	}
	
	public int getNumOfClasses() {
		return this.numOfClasses;
	}
	
	public int getNumOfFeatures() {
		return this.numOfFeatures;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; i++) {
			sb.append(labels.get(i));
			sb.append('\n');
			DenseVector instance = instances.get(i);
			sb.append(instance.toString());
			sb.append('\n');
		}
		return sb.toString();
	}
	public boolean add(DenseVector instance, double label){
		if (instances.add(instance)) {
			if (labels.add(label)) {
				size ++;
				return true;
			}
			instances.remove(instances.size() -1);
	    }
		return false;
	}
	
}
