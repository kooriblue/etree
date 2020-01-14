package protocol;

import java.security.SecureRandom;
import java.util.Vector;

import com.sun.javafx.image.IntToBytePixelConverter;
import com.sun.org.apache.bcel.internal.generic.NEW;

import interfaces.AbstractProtocol;
import interfaces.Model;
import interfaces.ModelHolder;
import message.OnlineSessionMessage;
import models.BoundedModelHolder;
import models.LogisticRegression;
import models.UpModelMessage;
import models.DownModelMessage;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.ControlEvent;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import utils.DenseVector;
import utils.Aggregate;
import utils.Compress;

//����ѧϰЭ��
public class ETreeLearningProtocol extends AbstractProtocol{
    private static final String PAR_MODELHOLDERNAME = "modelHolderName";
    private static final String PAR_MODELNAME = "modelName";
    private static final String PAR_COMPRESS = "compress";
    
    private final String modelHolderName;
    private final String modelName;
    
    private ModelHolder models; // �ýڵ���ΪҶ�ڵ㱣���ģ��
    private ModelHolder innerModel; // �ýڵ���Ϊ�ڶ���ڵ��������ģ��
    
    private ModelHolder mainModels; // ���ڵ��յ���ģ��
    private static ModelHolder[] innerModels; // �ڶ���ڵ��յ���ģ��
    //�������
    private static int bandwidth = 2000;
    //ѹ������
    private final int compress;
    
    // ���ĸ��ڵ�
    private static long rootID;
    // �ڶ���ڵ�
    private static long[] innerIDs;
    
    // �ڶ���ڵ���
    private static int numOfInnerNodes;
    
    public ETreeLearningProtocol(String prefix) {
        modelHolderName = Configuration.getString(prefix + "." + PAR_MODELHOLDERNAME);
        modelName = Configuration.getString(prefix + "." + PAR_MODELNAME);
        compress = Configuration.getInt(prefix + "." + PAR_COMPRESS);
        mainModels = new BoundedModelHolder(100);
        init(prefix);
    }
    
    public ETreeLearningProtocol(String prefix, double delayMean, double delayVar, String modelHolderName, String modelName,int compress) {
        this.prefix = prefix;
        this.delayMean = delayMean;
        this.delayVar = delayVar;
        this.modelHolderName = modelHolderName;
        this.modelName = modelName;
        this.compress = compress;
        mainModels = new BoundedModelHolder(100);
        init(prefix);
    }
    
    protected void init(String prefix) {
        try {
            super.init(prefix);
            models = (ModelHolder)Class.forName(modelHolderName).newInstance();
            models.init(prefix);
            Model model = (Model)Class.forName(modelName).newInstance();
            model.init(prefix);
            models.add(model);
        } 
        catch (Exception e) {
            throw new RuntimeException("Exception occured in initialization of " + getClass().getCanonicalName() + ": " + e);
        }
    }
    
    /*
       *�ۺϲ��ַ�ģ�͸������ӽڵ�
       *mode: 0-���ڵ�ۺϣ�1-�ڽڵ�ۺϷַ���Ҷ�ڵ㣬2-�ڽڵ�ۺϷ������ڵ�     */
    @Override
    public void activeThread(int mode) {
        if (models != null && models.size() > 0){
            long ID = currentNode.getID();
            
            // ���ڵ�ۺ�ģ�ͷ����ڶ���ڵ�
            if(ID == rootID) {
                int numOfChildren = ((Linkable)currentNode.getProtocol(2)).degree();
                if(mainModels.size()>=(int)(numOfChildren*aggregatePercent)) {
                    LogisticRegression lg = (LogisticRegression)models.getModel(models.size()-1).clone();
                    double sumAge = 0.0;
                    DenseVector[] dvs = new DenseVector[mainModels.size()];
                    for (int i = 0; i < mainModels.size(); i++) {
                        LogisticRegression tmp =  (LogisticRegression)mainModels.getModel(i);
                        sumAge += tmp.getAge();
                        dvs[i] = (DenseVector)tmp.getWeight().clone();
                    }
                    sumAge /= mainModels.size();
                    lg.addAge(sumAge);
                    DenseVector tmp = Aggregate.AggregateSubsampledImproved(dvs);
                    lg.Add(tmp, -1.0);
                    models.add(lg);
                    mainModels = new BoundedModelHolder(100);
                }
                
                long newSessionID = Network.getAggregateNode((int) currentNode.getID()).getSessionID()+1;
                Network.getAggregateNode((int) currentNode.getID()).setSessionID(newSessionID);
                
//                if (CommonState.getTime() > 0 && newSessionID % 2 == 0) {
                if (CommonState.getTime() > 0) {
                    EDSimulator.add(0, new ControlEvent(EDSimulator.controls[0], 
                            EDSimulator.ctrlSchedules[0], 0), null, -1, 0);
                }
                
                
                Model m = models.getModel(models.size() - 1);
                ModelHolder mh = new BoundedModelHolder(1);
                mh.add(m);
                Linkable overlay = (Linkable)currentNode.getProtocol(2);
                int degree = overlay.degree();
                for (int i = 0; i < degree; i++) {
                    sendToNeighbor(new DownModelMessage(Network.getAggregateNode((int) currentNode.getID()), mh, newSessionID, 2), Network.getAggregateNode((int) overlay.getNeighbor(i).getID()));
                }
            }
            else {
                // �ۺ�
                if(mainModels.size()>=1) {
                    LogisticRegression lg = (LogisticRegression)models.getModel(models.size()-1).clone();
                    double sumAge = 0.0;
                    DenseVector[] dvs = new DenseVector[mainModels.size()];
                    for (int i = 0; i < mainModels.size(); i++) {
                        LogisticRegression tmp =  (LogisticRegression)mainModels.getModel(i);
                        sumAge += tmp.getAge();
                        dvs[i] = (DenseVector)tmp.getWeight().clone();
                    }
                    sumAge /= mainModels.size();
                    lg.addAge(sumAge);
                    DenseVector tmp = Aggregate.AggregateSubsampledImproved(dvs);
                    lg.Add(tmp, -1.0);
                    models.add(lg);
                    mainModels = new BoundedModelHolder(100);
                }

                
                long newSessionID = Network.getAggregateNode((int) currentNode.getID()).getSessionID()+1;
                Network.getAggregateNode((int) currentNode.getID()).setSessionID(newSessionID);
                
                
                Model m = models.getModel(models.size() - 1);
                ModelHolder mh = new BoundedModelHolder(1);
                mh.add(m);
                
                // ���͸�Ҷ�ڵ�
                if (mode == 1) {
                    sendToWholeNeighbor(new DownModelMessage(Network.getAggregateNode((int) currentNode.getID()), mh, newSessionID, 1)); 
                }
                // ���͸����ڵ�
                else {
                    sendBack(new UpModelMessage(Network.getAggregateNode((int) currentNode.getID()), mh, newSessionID, 2), rootID);
                }
            }
        }
    }
    
    // �յ��ϲ�ڵ�ģ�͵Ĳ��������ڵ㵽�ڶ���ڵ㣬�ڶ���ڵ㵽Ҷ�ڵ㣩
    @Override
    public void passiveDownModelMsg(DownModelMessage message) {
        // �ж��Ƿ�Ϊ���µ�ģ��
        long sessionID = message.getSessionID();
        if (sessionID != Network.getAggregateNode((int) message.getSrc().getID()).getSessionID()) {
            return;
        }
        
        for (int i = 0; message != null && i < message.size(); i ++) {
            long ID = currentNode.getID();
            
            long srcID = message.getSrc().getID();
            //�ڶ���ڵ���յ����ڵ�ģ�ͣ��·���Ҷ�ڵ�
            if (srcID == rootID) {
                Model model = message.getModel(i);
                ModelHolder mh = new BoundedModelHolder(1);
                mh.add(model);
                sendToWholeNeighbor(new DownModelMessage(currentNode, mh, Network.getAggregateNode((int) currentNode.getID()).getSessionID(), 1));
            }
            
            // Ҷ�ڵ���յ��ڶ���ڵ�ģ�ͣ����±���ģ�ͣ�sendback
            else {
                Model model = message.getModel(i);
                LogisticRegression tmp = (LogisticRegression)model.clone();
                DenseVector tmpDv = (DenseVector)tmp.getWeight().clone();
                SecureRandom secureRandom = new SecureRandom();
                //Mini-batch
                int batchsize = 10;
                for (int j = 0; j < instances.getSize()/batchsize; j++) {
                    Vector<DenseVector> tmpX = new Vector<DenseVector>();
                    Vector<Double> tmpY = new Vector<Double>();
                    for (int k = 0; k < batchsize; k++) {
                        tmpX.add(instances.getInstance(j*batchsize+k));
                        tmpY.add(instances.getLabel(j*batchsize+k));
                    }
                    tmp.update(tmpX,tmpY);
                }
                tmpDv.Add(tmp.getWeight(), -1.0);
                LogisticRegression lg = (LogisticRegression)model.clone();
                DenseVector dv = (DenseVector)tmpDv.clone();
                boolean[] bool = new boolean[dv.getSize()];
                int randInt = 0;
                int length = (int)Math.ceil(dv.getSize()/compress);
                int[] d = new int[length];
                for (int j = 0; j < length; j++) {
                    do {
                        randInt = secureRandom.nextInt(dv.getSize());
                    }while(bool[randInt]);
                    bool[randInt] = true;
                    d[j] = randInt;
                }
                
                dv = Compress.CompressSubsampling(dv, d);
                lg.setWeight(dv);
                lg.setAge(instances.getSize());
                
                models.add(tmp);
//                if(bandwidth<0) {
//                    return;
//                }
//                bandwidth -= length;
                ModelHolder mh = new BoundedModelHolder(1);
                mh.add(lg);
                sendBack(new UpModelMessage(currentNode, mh, sessionID, 1), currentNode.getParentID());
            }
        }
    }
    
    // �յ��²�ڵ�ģ�͵Ĳ�����Ҷ�ڵ㵽�ڶ���ڵ㣬�ڶ���ڵ㵽���ڵ㣩
    @Override
    public void passiveUpModelMsg(UpModelMessage message) {
        
        for (int i = 0; message != null && i < message.size(); i ++) {
            long ID = currentNode.getID();
            
            // ���ڵ��յ��ڶ���ڵ��ģ��
            if(ID == rootID) {
                int numOfChildren = ((Linkable)currentNode.getProtocol(2)).degree();
                if (mainModels.size() >= (int)(numOfChildren*aggregatePercent)) {
                    EDSimulator.add(0, new OnlineSessionMessage(0), Network.getAggregateNode((int) currentNode.getID()), currentProtocolID, 0);
                } else {
                    if (message.getSessionID() == Network.getAggregateNode((int) currentNode.getID()).getSessionID()*aggregateCount) {
                        Model model = message.getModel(i);
                        ((ETreeLearningProtocol)Network.getAggregateNode((int) currentNode.getID()).getProtocol(1)).addMainModel(model);
                    }
                }
            }
            // �ڶ���ڵ��յ�Ҷ�ڵ��ģ��
            else {
                int numOfChildren = ((Linkable)currentNode.getProtocol(2)).degree();
                if (numOfChildren > 1) {
                    if (mainModels.size() >= (int)(numOfChildren*aggregatePercent)) {
                        EDSimulator.add(0, new OnlineSessionMessage(0), Network.getAggregateNode((int) currentNode.getID()), currentProtocolID, 0);
                    } else {
                        if (message.getSessionID() == Network.getAggregateNode((int) currentNode.getID()).getSessionID()) {
                            Model model = message.getModel(i);
                            ((ETreeLearningProtocol)Network.getAggregateNode((int) currentNode.getID()).getProtocol(1)).addMainModel(model);
                        }
                    }
                } else {
                    Model model = message.getModel(i);
                    ((ETreeLearningProtocol)Network.getAggregateNode((int) currentNode.getID()).getProtocol(1)).addMainModel(model);
                    EDSimulator.add(0, new OnlineSessionMessage(0), Network.getAggregateNode((int) currentNode.getID()), currentProtocolID, 0);
                }
            }
        }
    }
    
    @Override
    public int size() {
        return (models == null) ? 0 : 1;
    }
    
    @Override
    public ModelHolder getModelHolder(int index) {
        return models;
    }
    
    @Override
    public void setModelHolder(int index, ModelHolder modelHolder) {
        this.models = modelHolder;
    }
    
    @Override
    public boolean add(ModelHolder modelHolder) {
        setModelHolder(0, modelHolder);
        return true;
    }
    
    @Override
    public ModelHolder remove(int index) {
        ModelHolder tmp = models;
        models = null;
        return tmp;
    }

    @Override
    public Object clone() {
        return new ETreeLearningProtocol(prefix, delayMean, delayVar, modelHolderName, modelName, compress);
    }
    
    public int getBandwidth() {
        return bandwidth;
    }
    
    public void resetBandwidth() {
        this.bandwidth = 2000;
    }
    
    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }
    
    public static void setRoot(int ID) {
        rootID = ID;
    }
    
//    public static void setInner(int[] IDs) {
//        numOfInnerNodes = IDs.length;
//        if (innerIDs == null) {
//            innerIDs = new long[numOfInnerNodes];
//        }
//        for (int i = 0; i < numOfInnerNodes; i++) {
//            innerIDs[i] = IDs[i];
//        }
//        
//        mainModels = new BoundedModelHolder(numOfInnerNodes);
//        innerModels = new BoundedModelHolder[numOfInnerNodes];
//        for (int i = 0; i < numOfInnerNodes; i++) {
//            innerModels[i] = new BoundedModelHolder(((Linkable)Network.get((int) innerIDs[i]).getProtocol(2)).degree());
//        }
//    }
    
    public static long getRoot() {
        return rootID;
    }
    
    public static long[] getInner() {
        return innerIDs;
    }
    
    public void addMainModel(Model m) {
        this.mainModels.add(m);
    }
}
