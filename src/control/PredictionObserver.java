package control;

import models.InstanceHolder;
import models.LogisticRegression;
import models.OneLayerNerualNetwork;
import interfaces.BasicLearningProtocol;
import interfaces.Model;
import interfaces.ModelHolder;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import database.DatabaseReader;
import peersim.Simulator;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.reports.GraphObserver;

//predict并打印Loss的类
public class PredictionObserver extends GraphObserver{
	private static final String PAR_PROT = "protocol";
	private static final String PAR_SUFFIX = "suffix";
	private static final String PAR_FORMAT = "format";
	private static final String PAR_TRAINFILE = "trainFile";
	private static final String PAR_TESTFILE = "testFile";
	protected final int pid;
	
	protected final String format;
	protected final File testFile;
	protected final File trainFile;
	
	protected InstanceHolder test;
	protected String printSuffix = "";
	private static int index = 0;
	private Vector<Double> lossArr = new Vector<Double>();
	
	
	public PredictionObserver(String prefix) throws Exception {
		super(prefix);
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	    format = Configuration.getString(prefix + "." + PAR_FORMAT, "");
	    printSuffix = Configuration.getString(prefix + "." + PAR_SUFFIX, "");
	    trainFile = new File(Configuration.getString(prefix + "." + PAR_TRAINFILE));
	    testFile = new File(Configuration.getString(prefix + "." + PAR_TESTFILE));
	    DatabaseReader dReader = DatabaseReader.createDatabaseReader("database.DatabaseReader", trainFile, testFile);
	    setTestSet(dReader.getTestSet());
	}
	
	protected Set<Integer> generateIndices() {
	    TreeSet<Integer> indices = new TreeSet<Integer>();
	    for (int i = 0; i < g.size(); i ++) {
	      indices.add(i);
	    }
	    return indices;
	  }
	
	//计算整个网络中的loss的平均值并打印
	public boolean execute() {
		int count = 0;
		updateGraph();
		Set<Integer> idxSet = generateIndices();
		double sumloss = 0.0;
		for(int i: idxSet) {
			Protocol p = ((Node) g.getNode(i)).getProtocol(pid);
		    if (p instanceof BasicLearningProtocol) {
		    	int numOfHolders = ((BasicLearningProtocol)p).size();
		    	for (int holderIndex = 0; holderIndex < numOfHolders; holderIndex++){
		    		ModelHolder modelHolder = ((BasicLearningProtocol)p).getModelHolder(holderIndex);
		    		for(int j=0;j<modelHolder.size();j++) {
		    			LogisticRegression m = (LogisticRegression)modelHolder.getModel(j);
		    			double loss = m.ZeroOneErrorCompute(test.getInstances(),test.getLabels());
		    			sumloss += loss;
		    			count ++;
		    		}
		    	}
		    }
		}
		lossArr.add(sumloss/count);
		Simulator.addLoss(sumloss/count);
		if(index>20*47)
			System.out.println(getClass().getCanonicalName() + " - "+lossArr.toString() + "\n");
			
			
		index++;
		System.out.printf(CommonState.getTime()+"    "+ index + "    "+sumloss/count+"\n");
		
		//每一轮变化后重置带宽
		BasicLearningProtocol ab = (BasicLearningProtocol)((Node) g.getNode(0)).getProtocol(pid);
		ab.resetBandwidth();

			
		return false;
	}
	
	
	public void setTestSet(InstanceHolder test) {
		this.test = test;
	}
	
	public InstanceHolder getTestSet() {
		return test;
	}
	
	public String getPrintSuffix() {
		return printSuffix;
	}
	
	public void setPrintSuffix(String printSuffix) {
		this.printSuffix = printSuffix;
	}
	
}
