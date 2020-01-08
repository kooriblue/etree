package database;

import utils.DenseVector;
import models.InstanceHolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Vector;

//文件读取类
public class DatabaseReader {
	private InstanceHolder trainSet,testSet;
	
	private int numOfClasses,numOfFeatures;
	
	private DenseVector means;
	
	private static File trainFile,testFile;
	private static DatabaseReader dbReader;
	
	protected DatabaseReader(final File trainFile,final File testFile) throws IOException{
		this.means = new DenseVector();
		
		trainSet = parseFile(trainFile);
		testSet = parseFile(testFile);
		
		for (int i = 0; i < trainSet.getSize(); i++) {
			means.Add(trainSet.getInstance(i));
		}
		
		this.numOfClasses = Math.max(trainSet.getNumOfClasses(), testSet.getNumOfClasses());
		this.numOfFeatures = Math.max(trainSet.getNumOfFeatures(), testSet.getNumOfFeatures());
	}
	
	protected InstanceHolder parseFile(final File file) throws IOException{
		if(file == null || !file.exists()) {
			throw new IOException("The file " + file.toString() + "doesn't exist!");
		}
		
		Vector<DenseVector> instances = new Vector<DenseVector>();
		Vector<Double> labels = new Vector<Double>();
		DenseVector instance;
		double label;
		
		BufferedReader bReader = new BufferedReader(new FileReader(file));
		
		int numOfClasses = -1;
		int numOfFeatures = -1;
		String line;
		String[] data;
		
		while((line = bReader.readLine()) != null) {
			if (line.length() == 0)
				continue;
			
			line = line.replace(" ", "");
			data = line.split(",");
			
			int length = data.length - 1;
			
			if (numOfFeatures < length)
				numOfFeatures = length;
			
			label = Double.parseDouble(data[length]);
			
			if(numOfClasses < label + 1)
				numOfClasses = (int)label + 1;
			
			instance = new DenseVector(length);
			double [] tmpInstance = new double [length];
			for (int i = 0; i < length; i++) {
				double value = Double.parseDouble(data[i]);
				tmpInstance[i] = value;
			}
			instances.add(new DenseVector(tmpInstance));
			labels.add(label);
		}
		
		bReader.close();
		
		
		return new InstanceHolder((numOfClasses == 1) ? 0 : numOfClasses,numOfFeatures,instances,labels);
	}
	
	public static DatabaseReader createDatabaseReader(String className,final File trainFile,final File testFile) throws Exception{
		if(dbReader == null || !dbReader.getClass().getCanonicalName().equals(className)
				|| !trainFile.equals(DatabaseReader.trainFile) || !testFile.equals(DatabaseReader.testFile)) {
			DatabaseReader.trainFile = trainFile;
			DatabaseReader.testFile = testFile;
		    Class<? extends DatabaseReader> dataBaseReaderClass = (Class<? extends DatabaseReader>) Class.forName(className);
		    Constructor<? extends DatabaseReader> dbrConst = dataBaseReaderClass.getDeclaredConstructor(File.class, File.class);
		    DatabaseReader.dbReader = dbrConst.newInstance(trainFile, testFile);
		}
		return dbReader;
	}
	
	public InstanceHolder getTrainSet() {
		return this.trainSet;
	}
	
	public InstanceHolder getTestSet() {
		return this.testSet;
	}
	
	public int getNumOfClasses() {
		return this.numOfClasses;
	}
	
	public int getNumOfFeatures() {
		return this.numOfFeatures;
	}
	
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("[+] TrainSet: " + DatabaseReader.trainFile + "\n");
		sBuffer.append(trainSet.toString());
		sBuffer.append("[+] TestSet: " + DatabaseReader.testFile + "\n");
		sBuffer.append(testSet.toString());
		return sBuffer.toString();	
	}
	
	
	
}
