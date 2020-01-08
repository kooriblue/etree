package utils;

import java.util.Random;

//获取呈指数分布的随机数
public class Exponentially {
	public static int eRand (double lambda) {
	    Random random = new Random();
	    double u = random.nextDouble();

	    int x = 0;
	    double cdf = 0;
	    while (u >= cdf) {
	        x ++;
	        cdf = 1 - Math.exp(-1.0 * lambda * x);
	    }
	    return x;
	}
}
