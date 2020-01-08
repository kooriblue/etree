package utils;

import java.io.Serializable;
import java.util.Random;

//获取randomDouble的类
public class randomDouble implements Serializable{
	private static final long serialVersionUID = 1482949396349153558L;
	private final Random rand;
	private final double start;
	private final double range;
	
	public randomDouble(double start, double range, long seed) {
		this.start = start;
		this.range = range;
		rand = new Random(seed);
	}
	
	public randomDouble(double start, double range){
		this(start, range, System.currentTimeMillis());
	}
	
	public double nextDouble() {
		double res = start + range * rand.nextGaussian();
		return Math.pow(Math.E, res);
	}
	

}
