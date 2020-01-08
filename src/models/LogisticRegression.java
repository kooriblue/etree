package models;

import java.util.Vector;
import interfaces.Model;
import utils.DenseVector;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.cdsim.CDProtocol;

//基本的逻辑回归（二分类）
public class LogisticRegression implements Model,CDProtocol{
	private static final long serialVersionUID = -7254362879969974698L;
	
	protected static final String PAR_LAMBDA = "LogisticRegression.lambda";
	protected static final String PAR_L = "LogisticRegression.l";
	protected static final String PAR_STORAGE = "LogisticRegression.storage";
	
	protected DenseVector weight;
	protected int numOfClasses = 2;
	protected double age;
	protected double lambda = 10000;
	protected double l = 0.000001;
	protected int storage;
	
	public LogisticRegression() {
		this.weight = new DenseVector();
		this.age = 0;
	}
	
	public LogisticRegression(int length) {
		this.weight = new DenseVector(length);
		this.age = 0;
	}
	
		
	public LogisticRegression(DenseVector weight,double age,double lambda,int numOfClasses) {
		this.weight = (DenseVector) weight.clone();
		this.age = age;
		this.lambda = lambda;
		this.numOfClasses = numOfClasses;
	}
	
	@Override
	public void init(String prefix) {
		this.lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 10000);
		this.l = Configuration.getDouble(prefix + "." + PAR_L, 0.000001);
		this.storage = Configuration.getInt(prefix + "." + PAR_STORAGE, 16);
		this.weight = new DenseVector(this.storage);
		this.age = 0;	
	}
	
	@Override
	public Object clone() {
		return new LogisticRegression(weight,age,lambda,numOfClasses);
		
	}

	public void update(DenseVector dv,double label) {
		DenseVector tmpDv = ((DenseVector) dv.clone());
		double probability = classifyVeotor(tmpDv);
		double error = label - probability;
		age++;
		double learningrate = lambda/age;
		weight.Mul(1.0 - l*learningrate);
		weight.Add(tmpDv, error*learningrate);
	}
	
	public void update(DenseVector[] dv,double[] label) {
		double[] error = new double[label.length];
		age+=label.length;
		double learningrate = lambda/age;
		for (int i = 0; i < label.length; i++) {
			DenseVector tmpDv = ((DenseVector) dv[i].clone());
			double probability = classifyVeotor(tmpDv);
			error[i] = label[i] - probability;
		}
		DenseVector sum = new DenseVector(weight.getSize());
		for (int i = 0; i < label.length; i++) {
			DenseVector tmpDv = ((DenseVector) dv[i].clone());
			sum.Add(tmpDv, learningrate*error[i]);
		}
		sum.Mul(1.0/label.length);
		weight.Mul(1.0 - l*learningrate);
		weight.Add(sum);
	}
	
	public void update(Vector<DenseVector> dv, Vector<Double> label) {
		double[] error = new double[label.size()];
		age+=label.size();
		double learningrate = lambda/age;
		for (int i = 0; i < label.size(); i++) {
			DenseVector tmpDv = ((DenseVector) dv.get(i).clone());
			double probability = classifyVeotor(tmpDv);
			error[i] = label.get(i) - probability;
		}
		DenseVector sum = new DenseVector(weight.getSize());
		for (int i = 0; i < label.size(); i++) {
			DenseVector tmpDv = ((DenseVector) dv.get(i).clone());
			sum.Add(tmpDv, learningrate*error[i]);
		}
		sum.Mul(1.0/label.size());
		weight.Mul(1.0 - l*learningrate);
		weight.Add(sum);
		
	}
	
	public void Add(DenseVector dv,double alpha) {
		weight.Add(dv, alpha);
	}
	
	public void setWeight(DenseVector dv) {
		weight = (DenseVector)dv.clone();
	}
	
	
	public double sigmoid(double src) {
		double res = 1.0 / ( 1.0 + Math.exp(-src));
		return res;
	}
	
	public double classifyVeotor(DenseVector x) {
		double prob = 0;
		DenseVector tmp = ((DenseVector)x.clone());
		prob = tmp.Mul(weight);
		prob = sigmoid(prob);
		return prob;
	}
	
	public double predict(DenseVector instance) {
		DenseVector tmpDv = ((DenseVector)instance.clone());
		double prob = classifyVeotor(instance);
		if(prob>=0.5)
			return 1.0;
		else
			return 0.0;
	}
	
	public double ZeroOneErrorCompute(DenseVector[] x,double[] y) {
		double sum = 0.0;
		for (int i = 0; i < x.length; i++) {
			DenseVector tmpX = (DenseVector) x[i].clone();
			double tmpY = y[i];
			double predictY = predict(tmpX);
			sum += (tmpY == predictY) ? 0.0 : 1.0;
		}
		return sum/x.length;
	}
	
	public double ZeroOneErrorCompute(Vector<DenseVector> x, Vector<Double> y) {
		double sum = 0.0;
		for (int i = 0; i < x.size(); i++) {
			DenseVector tmpX = (DenseVector) x.get(i).clone();
			double tmpY = y.get(i);
			double predictY = predict(tmpX);
			sum += (tmpY == predictY) ? 0.0 : 1.0;
		}
		return sum/x.size();

	}
	
	public DenseVector getWeight() {
		return this.weight;
	}
	

	@Override
	public int getNumOfClasses() {
		return numOfClasses;
	}

	@Override
	public void setNumOfClasses(int numOfClasses) {
		this.numOfClasses = numOfClasses;
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// TODO Auto-generated method stub
		
	}
	
	public void addAge(double value) {
		this.age += value;
	}
	
	public double getAge() {
		return age;
	}
	
	public void setAge(double value) {
		this.age = value;
	}
	

}
