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
		
package peersim.graph;

import java.util.*;

import message.ActiveThreadMessage;
import peersim.config.Configuration;
import peersim.core.AggregateNode;
import peersim.core.GeneralNode;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import protocol.ETreeLearningProtocol;
import utils.GraphClustering;
import utils.TopoUtil;

/**
* Contains static methods for wiring certain kinds of graphs. The general
* contract of all methods is that they accept any graph and add edges
* as specified in the documentation.
*/
public class GraphFactory {

/** Disable instance construction */
private GraphFactory() {}

// ===================== public static methods ======================
// ==================================================================

/**
* Random graph. Generates randomly k directed edges out of each node.
* The neighbors
* (edge targets) are chosen randomly without replacement from the nodes of the
* graph other than the source node (i.e. no loop edge is added).
* If k is larger than N-1 (where N is the number of nodes) then k is set to
* be N-1 and a complete graph is returned.
* @param g the graph to be wired
* @param k samples to be drawn for each node
* @param r source of randomness
* @return returns g for convenience
*/
//public static Graph wireKOut( Graph g, int k, Random r ) {
//
//	final int n = g.size();
//	if( n < 2 ) return g;
//	
//	if( n <= k ) k=n-1;
//	int[] nodes = new int[n];
//	for(int i=0; i<nodes.length; ++i) nodes[i]=i;
//	for(int i=0; i<n; ++i)
//	{
//		int j=0;
//		while(j<k)
//		{
//		    // 确保不会选到重复的邻居
//			int newedge = j+r.nextInt(n-j);
//			int tmp = nodes[j];
//			nodes[j] = nodes[newedge];
//			nodes[newedge] = tmp;
//			if( nodes[j] != i )
//			{
//				g.setEdge(i,nodes[j]);
//				j++;
//			}
//		}
//	}
//	
//	return g;
//}

/**
* Random graph. Generates randomly k directed edges out of each node.
* The neighbors
* (edge targets) are chosen randomly without replacement from the nodes of the
* graph other than the source node (i.e. no loop edge is added).
* If k is larger than N-1 (where N is the number of nodes) then k is set to
* be N-1 and a complete graph is returned.
* @param g the graph to be wired
* @param k samples to be drawn for each node
* @param r source of randomness
* @return returns g for convenience
*/
public static Graph wireTree( Graph g, int k, Random r ) {

    final int n = g.size();
    if( n < 2 ) return g;
    
    int[] indexes = new int[n];
    for(int i=0; i<indexes.length; ++i) indexes[i]=i;
    
//    // 选择根节点
//    int ind = 0;
//    int rootID = ind + r.nextInt(n - ind);
//    int tmp = indexes[ind];
//    indexes[ind] = indexes[rootID];
//    indexes[rootID] = tmp;
//    ind++;
//    
//    
//    // 分组
//    int[] nodeIDs = new int[k]; // 第二层节点的ID
//    int numOfLeaves = (n - 1) / k;
//    for (int i = 0; i < k; i++) {
//        for (int j = 0; j < numOfLeaves; j++) {
//            int ID = ind + r.nextInt(n - ind);
//            tmp = indexes[ind];
//            indexes[ind] = indexes[ID];
//            indexes[ID] = tmp;
//            if (j == 0) {
//                nodeIDs[i] = indexes[ind];
//            }
//            g.setEdge(nodeIDs[i], indexes[ind]);
//            Network.get(indexes[ind]).setParentID(nodeIDs[i]);
//            ind++;
//        }
//    }
//    
//    int left = (n - 1) % k;
//    if (left > 0) {
//        for (int i = 0; i < left; i++) {
//            int ID = ind + r.nextInt(n - ind);
//            tmp = indexes[ind];
//            indexes[ind] = indexes[ID];
//            indexes[ID] = tmp;
//            g.setEdge(nodeIDs[i], indexes[ind]);
//            Network.get(indexes[ind]).setParentID(nodeIDs[i]);
//            ind++;
//        }
//    }
//    
//    if (j >= n) {
//        return g;
//    }
    
    TopoUtil.getGraph(100, "D:/koori/JavaDevelopment/etree/data/data100.in");
    ArrayList<ArrayList<Integer>> clusters = TopoUtil.getClustering();
    
    int numOfAggregateNodes = 0;
    // 分组
    for (int i = 0; i < clusters.size(); i++) {
        AggregateNode node = new AggregateNode("");
        node.setIndex(numOfAggregateNodes);
        node.setIDinNetwork(clusters.get(i).get(0));
        numOfAggregateNodes++;
        Network.addAggregateNode(node);
        for (int j = 0; j < clusters.get(i).size(); j++) {
            int ID = clusters.get(i).get(j);
            g.setEdge(node.getIndex(), ID, 0);
            Network.get(ID).setParentID(node.getIndex());
        }
    }
    
    // 设置根节点
    AggregateNode node = new AggregateNode("");
    node.setIndex(numOfAggregateNodes);
    node.setIDinNetwork(clusters.get(0).get(0));
    Network.addAggregateNode(node);
    for (int i = 0; i < k; i++) {
        g.setEdge(numOfAggregateNodes, i, 1);
        Network.getAggregateNode(i).setParentID(numOfAggregateNodes);
    }
    
    ETreeLearningProtocol.setRoot(numOfAggregateNodes);
    
    
    // 打印当前建的树
    System.out.println("Root: " + ETreeLearningProtocol.getRoot() + "(" + Network.getAggregateNode((int) ETreeLearningProtocol.getRoot()).getIDinNetwork() + ") ");
    
    System.out.println("Inner Nodes: ");
    
    Linkable linkable = (Linkable) Network.getAggregateNode((int) ETreeLearningProtocol.getRoot()).getProtocol(2);
    int len = linkable.degree();
    
    int[] inds = new int[len];

    for (int i = 0; i < len; i++) {
        inds[i] = (int) linkable.getNeighbor(i).getID();
        System.out.print(inds[i] + "(" + Network.getAggregateNode((int)inds[i]).getIDinNetwork() + ")(" + Network.getAggregateNode((int)inds[i]).getParentID() + ") ");
    }
    System.out.println();
    
    for (int i = 0; i < inds.length; i++) {
        System.out.print("Leaves of Node " + inds[i] + "(" + Network.getAggregateNode((int)inds[i]).getIDinNetwork() + ") " + ": ");
        Linkable ol = (Linkable)Network.getAggregateNode((int) inds[i]).getProtocol(2);
        int num = ol.degree();
        for (int l = 0; l < num; l++) {
            System.out.print(ol.getNeighbor(l).getID() + "(" + ol.getNeighbor(l).getParentID() + ") ");
        }
        System.out.println();
    }
    
    return g;
}

// -------------------------------------------------------------------
/*
public static void main(String[] pars) {
	
	int n = Integer.parseInt(pars[0]);
	//int k = Integer.parseInt(pars[1]);
	Graph g = new BitMatrixGraph(n);
	
	//wireWS(g,20,.1,new Random());
	//GraphIO.writeChaco(new UndirectedGraph(g),System.out);
	
	//wireScaleFreeBA(g,3,new Random());
	//wireKOut(g,k,new Random());
	//wireRegRootedTree(g,k);
	wireHypercube(g);
	GraphIO.writeNeighborList(g,System.out);
}
*/
}

