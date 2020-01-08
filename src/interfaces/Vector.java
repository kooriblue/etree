package interfaces;

import java.io.Serializable;

//基本向量接口
public class Vector implements Serializable{

	private static final long serialVersionUID = 3755682713779501759L;

	public int index;

	public double value;

	public Vector(int index, double value) {
	  this.index = index;
	  this.value = value;
	}
	
	public String toString() {
	  return index + " " + value;
	}
}
