package models;

import interfaces.Model;
import interfaces.ModelHolder;
import peersim.core.Node;

//往上传的模型消息类
public class UpModelMessage implements ModelHolder{
    private static final long serialVersionUID = -1650466332477125351L;
    private Node src;
    private final ModelHolder models;
    private long sessionID;
    private int type;
    
    public UpModelMessage(Node src, ModelHolder models, long sessionID, int type) {
        this.src = src;
        this.models = (ModelHolder)models.clone();
        this.sessionID = sessionID;
        this.type = type;
    }
    
    @Override
    public Object clone() {
        return new UpModelMessage(src, models, sessionID, type);
    }
    
    public void setSrc(Node src) {
        this.src = src;
    }
    
    public Node getSrc() {
        return src;
    }
    
    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }
    
    public long getSessionID() {
        return sessionID;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getType() {
        return type;
    }

    @Override
    public void init(String prefix) {
        models.init(prefix);
    }

    @Override
    public int size() {
        return models.size();
    }

    @Override
    public Model getModel(int index) {
        return models.getModel(index);
    }

    @Override
    public void setModel(int index, Model model) {
        models.setModel(index, model);
    }

    @Override
    public boolean add(Model model) {
        return models.add(model);
    }

    @Override
    public Model remove(int index) {
        return models.remove(index);
    }

    @Override
    public void clear() {
        models.clear();
    }
    
    

}