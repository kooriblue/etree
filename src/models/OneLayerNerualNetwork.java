package models;

import java.util.Vector;
import interfaces.Model;
import utils.DenseVector;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.cdsim.CDProtocol;

//单层神经网络
public class OneLayerNerualNetwork implements Model,CDProtocol{
    private static final long serialVersionUID = -7254362879969974697L;
    
    protected static final String PAR_LAMBDA = "OneLayerNerualNetwork.lambda";
    protected static final String PAR_L = "OneLayerNerualNetwork.l";
    protected static final String PAR_STORAGE = "OneLayerNerualNetwork.storage";
    
    protected Vector<DenseVector> weight;
    protected int numOfClasses = 10;
    protected double age;
    protected double lambda = 10000;
    protected double l = 0.0001;
    protected int storage;
    
    public OneLayerNerualNetwork() {
        this.weight = new Vector<DenseVector>();
        for (int i = 0; i < numOfClasses; i++) {
            this.weight.add(new DenseVector());
        }
        this.age = 0;
    }
    
    public OneLayerNerualNetwork(int length) {
        this.weight = new Vector<DenseVector>();
        for (int i = 0; i < numOfClasses; i++) {
            this.weight.add(new DenseVector(length));
        }
        this.age = 0;
    }
    
    public OneLayerNerualNetwork(Vector<DenseVector> weight,double age,double lambda,int numOfClasses) {
        this.weight = new Vector<DenseVector>();
        for (int i = 0; i < numOfClasses; i++) {
            this.weight.add((DenseVector) weight.get(i).clone());
        }
        this.age = age;
        this.lambda = lambda;
        this.numOfClasses = numOfClasses;
    }
    
    @Override
    public void init(String prefix) {
        this.lambda = Configuration.getDouble(prefix + "." + PAR_LAMBDA, 10000);
        this.l = Configuration.getDouble(prefix + "." + PAR_L, 0.0001);
        this.storage = Configuration.getInt(prefix + "." + PAR_STORAGE, 16);
        this.weight = new Vector<DenseVector>();
        for (int i = 0; i < numOfClasses; i++) {
            this.weight.add(new DenseVector(this.storage));
        }
        this.age = 0;   
    }
    
    @Override
    public Object clone() {
        return new OneLayerNerualNetwork(weight,age,lambda,numOfClasses);
        
    }

    public void update(DenseVector dv,double label) {
        DenseVector tmpDv = (DenseVector) dv.clone();
        Vector<Double> predictY = classifyVeotor(tmpDv);
        int tmpY = new Double(label).intValue();
        age++;
        double learningrate = lambda/age;
//        // 只需更新权重中label对应的那一行
//        DenseVector term1 = (DenseVector) weight.get(classNo).clone();
//        DenseVector tmpX = (DenseVector) dv.clone();
//        DenseVector term2 = tmpX.Mul(predictY.get(classNo)-1).Add(term1, l);
//        weight.get(classNo).Add(term2, -learningrate);
        for (int i = 0; i < numOfClasses; i++) {
            double error = 0.0;
            if (i != tmpY) {
                error = -predictY.get(i);
            } else {
                error = 1 - predictY.get(i);
            }
            weight.get(i).Mul(1.0 - l*learningrate);
            weight.get(i).Add((DenseVector) dv.clone(), error*learningrate);
        }
        
    }
    
    public void update(DenseVector[] dv,double[] label) {
        age+=label.length;
        double learningrate = lambda/age;
        double[][] error = new double[label.length][numOfClasses];
        for (int i = 0; i < label.length; i++) {
            DenseVector tmpDv = (DenseVector) dv[i].clone();
            Vector<Double> predictY = classifyVeotor(tmpDv);
            int tmpY = new Double(label[i]).intValue();
            for (int j = 0; j < numOfClasses; j++) {
                if (j != tmpY) {
                    error[i][j] = -predictY.get(j);
                } else {
                    error[i][j] = 1 - predictY.get(j);
                }
            }
        }
        Vector<DenseVector> sum = new Vector<DenseVector>();
        for (int i = 0; i < numOfClasses; i++) {
            sum.add(new DenseVector(dv[0].getSize()));
        }   
        for (int i = 0; i < label.length; i++) {
            DenseVector tmpDv = (DenseVector) dv[i].clone();
            for (int j = 0; j < numOfClasses; j++) {
                sum.get(j).Add(tmpDv, learningrate*error[i][j]);
            }
        }
        for (int i = 0; i < numOfClasses; i++) {
            weight.get(i).Mul(1.0 - l*learningrate);
            weight.get(i).Add(sum.get(i));
        }
    }
    
    public void update(Vector<DenseVector> dv, Vector<Double> label) {
        age+=label.size();
        double learningrate = lambda/age;
//        for (int i = 0; i < label.size(); i++) {
//            DenseVector tmpDv = (DenseVector) dv.get(i).clone();
//            Vector<Double> predictY = classifyVeotor(tmpDv);
//            // 只需更新权重中label对应的那一行
//            int classNo = new Double(label.get(i)).intValue();
//            DenseVector term1 = (DenseVector) weight.get(classNo).clone();
//            DenseVector tmpX = (DenseVector) dv.get(i).clone();
//            DenseVector term2 = tmpX.Mul(predictY.get(classNo)-1).Add(term1, l);
//            weight.get(classNo).Add(term2, -learningrate);
//        }
        double[][] error = new double[label.size()][numOfClasses];
        for (int i = 0; i < label.size(); i++) {
            DenseVector tmpDv = (DenseVector) dv.get(i).clone();
            Vector<Double> predictY = classifyVeotor(tmpDv);
            int tmpY = new Double(label.get(i)).intValue();
            for (int j = 0; j < numOfClasses; j++) {
                if (j != tmpY) {
                    error[i][j] = -predictY.get(j);
                } else {
                    error[i][j] = 1 - predictY.get(j);
                }
            }
        }
        Vector<DenseVector> sum = new Vector<DenseVector>();
        for (int i = 0; i < numOfClasses; i++) {
            sum.add(new DenseVector(dv.get(0).getSize()));
        }   
        for (int i = 0; i < label.size(); i++) {
            DenseVector tmpDv = (DenseVector) dv.get(i).clone();
            for (int j = 0; j < numOfClasses; j++) {
                sum.get(j).Add(tmpDv, learningrate*error[i][j]);
            }
        }
        for (int i = 0; i < numOfClasses; i++) {
            weight.get(i).Mul(1.0 - l*learningrate);
            weight.get(i).Add(sum.get(i));
        }
    }

    public void Add(Vector<DenseVector> dv,double alpha) {
        for (int i = 0; i < weight.size(); i++) {
            weight.get(i).Add(dv.get(i), alpha);
        }
    }
    
    public void setWeight(Vector<DenseVector> dv) {
        for (int i = 0; i < weight.size(); i++) {
            weight.set(i, dv.get(i));
        }
    }
    
    public Vector<Double> classifyVeotor(DenseVector x) {
        Vector<Double> prob = new Vector<Double>();
        DenseVector tmp = ((DenseVector)x.clone());
        for (int i = 0; i < numOfClasses; i++) {
            prob.add(tmp.Mul(weight.get(i)));
        }
        prob = softmax(prob);
        return prob;
    }
    
    public Vector<Double> softmax(Vector<Double> src) {
        Vector<Double> res = new Vector<Double>();
        double denominator = 0.0;
        Vector<Double> numerators = new Vector<Double>();
        for (int i = 0; i < src.size(); i++) {
            double numerator = Math.exp(src.get(i));
            numerators.add(numerator);
            denominator += numerator;
        }
        for (int i = 0; i < src.size(); i++) {
            res.add(numerators.get(i) / denominator);
        }
        return res;
    }
    
    public Vector<Double> predict(DenseVector instance) {
        DenseVector tmpDv = (DenseVector)instance.clone();
        Vector<Double> prob = classifyVeotor(tmpDv);
        return prob;
    }
    
    public double ZeroOneErrorCompute(DenseVector[] x,double[] y) {
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            DenseVector tmpX = (DenseVector) x[i].clone();
            int tmpY = new Double(y[i]).intValue();
            Vector<Double> predictY = predict(tmpX);
            sum += (tmpY == getMaxIndex(predictY)) ? 0.0 : 1.0;
        }
        return sum/x.length;
    }
    
    public double ZeroOneErrorCompute(Vector<DenseVector> x, Vector<Double> y) {
        double sum = 0.0;
        for (int i = 0; i < x.size(); i++) {
            DenseVector tmpX = (DenseVector) x.get(i).clone();
            int tmpY = new Double(y.get(i)).intValue();
            Vector<Double> predictY = predict(tmpX);
            sum += (tmpY == getMaxIndex(predictY)) ? 0.0 : 1.0;
        }
        return sum/x.size();

    }
    
    public double CrossEntropyErrorCompute(DenseVector[] x, double[] y) {
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            DenseVector tmpX = (DenseVector) x[i].clone();
            Vector<Double> predictY = predict(tmpX);
            int classNo = new Double(y[i]).intValue();
            sum -= Math.log(predictY.get(classNo));
        }
        return sum / x.length;
    }
    
    public double CrossEntropyErrorCompute(Vector<DenseVector> x, Vector<Double> y) {
        double sum = 0.0;
        for (int i = 0; i < x.size(); i++) {
            DenseVector tmpX = (DenseVector) x.get(i).clone();
            Vector<Double> predictY = predict(tmpX);
            int classNo = new Double(y.get(i)).intValue();
            double term = Math.log(predictY.get(classNo));
            sum -= term;
        }
        return sum / x.size();
    }
    
    public int getMaxIndex(Vector<Double> vector) {
        int max = 0;
        for (int i = 1; i < vector.size(); i++) {
            if (vector.get(i).compareTo(vector.get(max)) > 0) {
                max = i;
            }
        }
        return max;
    }
    
    public Vector<DenseVector> getWeight() {
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
