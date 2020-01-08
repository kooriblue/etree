package models;

import interfaces.Model;
import interfaces.ModelHolder;
import peersim.core.Node;

//往上传的模型消息类
public class UpModelMessage implements ModelHolder{
    private static final long serialVersionUID = -1650466332477125351L;
    private Node src;
    private final ModelHolder models;
    
    public UpModelMessage(Node src, ModelHolder models) {
        this.src = src;
        this.models = (ModelHolder)models.clone();
    }
    
    @Override
    public Object clone() {
        return new DownModelMessage(src, models);
    }
    
    public void setSrc(Node src) {
        this.src = src;
    }
    
    public Node getSrc() {
        return src;
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