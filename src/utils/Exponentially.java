package utils;

import java.util.Random;

//��ȡ��ָ���ֲ��������
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
