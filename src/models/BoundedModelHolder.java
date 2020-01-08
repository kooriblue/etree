package models;

import interfaces.Model;
import interfaces.ModelHolder;
import java.util.Vector;
import peersim.config.Configuration;

//有界的模型保存类
public class BoundedModelHolder implements ModelHolder{
	private static final long serialVersionUID = 6987014948023941901L;
	private static final String PAR_CAPACITY = "capacity";
	
	public static final int MAX_CAPACITY = 200;
	private Vector<Model> models;
	private int capacity;
	
	public BoundedModelHolder() {
	    models = new Vector<Model>();
	    capacity = MAX_CAPACITY;
	}
	
	public BoundedModelHolder(int capacity) {
	    models = new Vector<Model>();
	    this.capacity = capacity;
	}
	
	public BoundedModelHolder(Vector<Model> models,int capacity) {
	    this.models = new Vector<Model>();
	    for (int i = 0; i < models.size(); i++) {
	    	this.models.add((Model)models.get(i).clone());
		}
	    this.capacity = capacity;
	}
	
	public Object clone(){
		return new BoundedModelHolder(models, capacity);
	}
	
	@Override
	public void init(String prefix) {
	    models = new Vector<Model>();
	    capacity = Configuration.getInt(prefix + "." + PAR_CAPACITY, 1);
	}

	@Override
	public int size() {
		return models.size();
	}

	@Override
	public Model getModel(int index) {
		return models.get(index);
	}

	@Override
	public void setModel(int index, Model model) {
		models.set(index, model);
	}

	@Override
	public boolean add(Model model) {
		if (models.add(model)) {
			if (models.size() > capacity) {
	          models.remove(0);
	        }
	        return true;
	      }
	      return false;
	}

	@Override
	public Model remove(int index) {
		return models.remove(index);
	}

	@Override
	public void clear() {
		models.clear();
		
	}
	
	public String toString() {
		return models.toString();
	}

}
