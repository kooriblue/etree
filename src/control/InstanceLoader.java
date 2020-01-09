package control;

import models.InstanceHolder;
import database.DatabaseReader;
import interfaces.BasicLearningProtocol;

import java.io.File;
import java.security.SecureRandom;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;
import protocol.ETreeLearningProtocol;


//读取实例的类
public class InstanceLoader implements Control{
	private static final String PAR_PROT = "protocol";
	private static final String PAR_TRAINFILE = "trainFile";
	private static final String PAR_TESTFILE = "testFile";
	private static final String PAR_SIZE = "samplesPerNode";
	private static final String PAR_READERCLASS = "readerClass";
	  
	protected final int pid;
	protected final File trainFile;
	protected String readerClassName;
	protected DatabaseReader dReader;
	protected final File testFile;
	protected final int samplesPerNode;
	
	//config.txt初始化该实例
	public InstanceLoader(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	    trainFile = new File(Configuration.getString(prefix + "." + PAR_TRAINFILE));
	    testFile = new File(Configuration.getString(prefix + "." + PAR_TESTFILE));
	    samplesPerNode = Configuration.getInt(prefix + "." + PAR_SIZE, 74);
	    readerClassName = Configuration.getString(prefix + "." + PAR_READERCLASS, "database.DatabaseReader");
	}
	
	//用于动态网络中分配给新节点实例
	public static InstanceHolder getInstance(int samplesPerNode,String tranFileName,String testFileName) {
		try {
			DatabaseReader dReader;
			dReader = DatabaseReader.createDatabaseReader("database.DatabaseReader", new File(tranFileName), new File(testFileName));
			
			SecureRandom secureRandom = new SecureRandom();
			int labelType = secureRandom.nextInt(10000)%2;
			InstanceHolder instances = new InstanceHolder(dReader.getTrainSet().getNumOfClasses(),dReader.getTrainSet().getNumOfFeatures());
			int numOfSamples = dReader.getTrainSet().getSize();
			int index = 0;
			index = secureRandom.nextInt(numOfSamples);

			boolean[] bool = new boolean[numOfSamples];
			for (int j = 0; j < samplesPerNode; j++) {
				do {
					index = secureRandom.nextInt(numOfSamples);
				}while(labelType!=dReader.getTrainSet().getLabel(index)||bool[index]==true);
				bool[index] = true;
				instances.add(dReader.getTrainSet().getInstance(index),dReader.getTrainSet().getLabel(index));
			}
			instances.add(dReader.getTrainSet().getInstance(index),dReader.getTrainSet().getLabel(index));
			return instances;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	//模拟器初始化阶段分配给各个节点新实例
	@Override
	public boolean execute() {
		try {
			dReader = DatabaseReader.createDatabaseReader(readerClassName, trainFile, testFile);
			
			int numOfSamples = dReader.getTrainSet().getSize();
			//System.out.print(Network.size());
			int ind = 0;
			for (int i = 0; i < Network.size(); i++) {
			    if (i != ETreeLearningProtocol.getRoot()) {
			        Node node = Network.get(i);
    				Protocol protocol = node.getProtocol(pid);
    				if(protocol instanceof BasicLearningProtocol) {
    					BasicLearningProtocol lp = (BasicLearningProtocol)protocol;
    					InstanceHolder instances = new InstanceHolder(dReader.getTrainSet().getNumOfClasses(),dReader.getTrainSet().getNumOfFeatures());
    					for (int j = 0; j < samplesPerNode; j++) {
    						instances.add(dReader.getTrainSet().getInstance((ind * samplesPerNode + j) % numOfSamples),dReader.getTrainSet().getLabel((ind * samplesPerNode + j) % numOfSamples));
    						
    					}
    					
    					lp.setInstenceHolder(instances);
    				}
    				else
    					throw new RuntimeException("The protocol " + pid + " have to implement BasicLearningProtocol interface!");
    				ind++;
			    }
			}
		} catch(Exception e) {
			throw new RuntimeException("Exception has occurred in InstanceLoader!", e);
		}
		return false;
	}

}
