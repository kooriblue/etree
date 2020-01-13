/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package peersim.transport;

import java.util.Vector;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import utils.TopoUtil;


/**
 * Implement a transport layer that reliably delivers messages with a random
 * delay, that is drawn from the configured interval according to the uniform
 * distribution.
 *
 * @author Alberto Montresor
 * @version $Revision: 1.14 $
 */
public final class UniformRandomTransport implements Transport
{

//---------------------------------------------------------------------
//Parameters
//---------------------------------------------------------------------

/** 
 * String name of the parameter used to configure the minimum latency.
 * @config
 */	
private static final String PAR_MINDELAY = "mindelay";	
	
/** 
 * String name of the parameter used to configure the maximum latency.
 * Defaults to {@value #PAR_MINDELAY}, which results in a constant delay.
 * @config 
 */	
private static final String PAR_MAXDELAY = "maxdelay";	
	
//---------------------------------------------------------------------
//Fields
//---------------------------------------------------------------------

/** Minimum delay for message sending */
private final long min;
	
/** Difference between the max and min delay plus one. That is, max delay is
* min+range-1.
*/
private final long range;

/**
 * 路由之间的delay
 */
private static Vector<Vector<Double>> delays;

	
//---------------------------------------------------------------------
//Initialization
//---------------------------------------------------------------------

/**
 * Reads configuration parameter.
 */
public UniformRandomTransport(String prefix)
{
    if (delays == null) {
        delays = new Vector<Vector<Double>>();
    
        int group_num = Configuration.getInt("GROUP_NUM");
        for (int i = 0; i < group_num; i++) {
            Vector<Double> vector = new Vector<Double>();
            for (int j = 0; j < group_num; j++) {
                if (i == j) {
                    vector.add(0.0);
                } else if (i > j) {
                    vector.add(delays.get(j).get(i));
                } else {
                    vector.add(1.0 + CommonState.r.nextDouble()*2.0);
                }
            }
            delays.add(vector);
        }
    }
    
    
	min = Configuration.getLong(prefix + "." + PAR_MINDELAY);
	long max = Configuration.getLong(prefix + "." + PAR_MAXDELAY,min);
	if (max < min) 
	   throw new IllegalParameterException(prefix+"."+PAR_MAXDELAY, 
	   "The maximum latency cannot be smaller than the minimum latency");
	range = max-min+1;
}

//---------------------------------------------------------------------

/**
* Returns <code>this</code>. This way only one instance exists in the system
* that is linked from all the nodes. This is because this protocol has no
* node specific state.
*/
public Object clone()
{
	return this;
}

//---------------------------------------------------------------------
//Methods
//---------------------------------------------------------------------

/**
 * Delivers the message with a random
 * delay, that is drawn from the configured interval according to the uniform
 * distribution.
*/
public void send(Node src, Node dest, Object msg, int pid)
{
	// avoid calling nextLong if possible
//	long delay = (range==1?min:min + CommonState.r.nextLong(range));
//    long delay = delays.get(src.getRouterID()).get(dest.getRouterID()).longValue();
    long delay = TopoUtil.getMinDelay((int)src.getIDinNetwork(),
                                        (int)dest.getIDinNetwork());
	EDSimulator.add(delay, msg, dest, pid);
}

/**
 * Returns a random
 * delay, that is drawn from the configured interval according to the uniform
 * distribution.
*/
public long getLatency(Node src, Node dest)
{
	return (range==1?min:min + CommonState.r.nextLong(range));
}

/**
 * 设置各路由之间的delay
 */
public static void setDelay(int i, int j, double delay) {
    delays.get(i).set(j, delay);
}

}
