package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class TopoUtil {

    private static int[][] g;
    private static int[][] minDelayMatrix;

    /**
     * Requires numpy and cyaron,
     * Use command to install them,
     * pip(or conda) install numpy
     * pip(or conda) install cyaron
     * @param n
     * @param delayMean
     * @param delayVar
     */
    public static void generatedGraph(int n, int delayMean, int delayVar) {
        Process pr;
        String exe = "python";

        // It must be the absolute path where the python script in.
        String command = "D:/koori/JavaDevelopment/etree/data/gen.py";

        String[] cmd = new String[] {exe, command, String.valueOf(n),
                String.valueOf(delayMean), String.valueOf(delayVar)};

        try {
            pr = Runtime.getRuntime().exec(cmd);

            InputStream is = pr.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            String str = dis.readLine();
            pr.waitFor();
            System.out.println(str);

        } catch (Exception e) { }
    }

    /**
     * Returns the adjacency matrix of the network,
     * If value is 0x7fffffff, then there is no edge between two nodes
     * else it represents the delay between two nodes.
     *
     * Notice that the index of node starts with 0.
     *
     * @param n Network size
     * @param filePath The path of graph data file
     * @return Adjacency matrix
     */
    public static void getGraph(int n, String filePath) {
        g = new int[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                g[i][j] = i == j ? 0 : 0x7fffffff;

        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bf = new BufferedReader(fr);
            String str;

            while ((str = bf.readLine()) != null) {
                String[] temp = str.split(" ");
                int from = Integer.parseInt(temp[0])-1;
                int to = Integer.parseInt(temp[1])-1;
                g[from][to] = Integer.parseInt(temp[2]);
                g[to][from] = Integer.parseInt(temp[2]);
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//         for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++)
//                System.out.printf("%15d", g[i][j]);
//            System.out.println();
//         }
    }

    /**
     * Returns the minimum delay from start(node index) to end(node index),
     * if minDelay = 0x7ffffff, it means that message can not from start to end.
     *
     * Implemented by Dijkstra with heap
     *
     * @param start message from
     * @return the minimum delay
     */
    private static int[] getSingelNodeMinDelay(int start) {
        class Edge implements Comparable<Edge>{
            int to , cost;
            Edge(int to_,int cost_){
                to = to_;
                cost = cost_;
            }
            @Override
            public int compareTo(Edge o) {
                return this.cost - o.cost;
            }
        }

        int n = g.length;
        boolean[] vis = new boolean[n];
        int[] dis = new int[n];

        for (int i = 0; i < n; i++) dis[i] = 0x7fffffff;
        Queue<Edge> que = new PriorityQueue<>();
        que.add(new Edge(start, 0));
        dis[start] = 0;
        while (!que.isEmpty()) {
            Edge top = que.poll();
            int u = top.to;

            if (dis[u] < top.cost) continue;
            if (vis[u]) continue;

            vis[u] = true;

            for (int to = 0; to < n; to++) {
                if (u != to && g[u][to] != 0x7fffffff) {
                    int delay = g[u][to];

                    if (!vis[to] && dis[to] > dis[u] + delay) {
                        dis[to] = dis[u]+delay;
                        que.add(new Edge(to, dis[to]));
                    }
                }
            }
        }
        return dis;
    }

    public static void generateMinDelayMatrix() {
        minDelayMatrix = new int[g.length][g.length];
        for (int nodeIndex = 0; nodeIndex < g.length; nodeIndex++) {
            int[] singleNodeDelayArray = getSingelNodeMinDelay(nodeIndex);
            for (int i = 0; i < g.length; i++) {
                minDelayMatrix[nodeIndex][i] = singleNodeDelayArray[i];
            }
        }
    }

    public static int getMinDelay(int start, int end) {
        return minDelayMatrix[start][end];
    }
    
    public static int findParameterServerId(ArrayList<Integer> nodeIdList, float aggregationRatio) {
        ArrayList<Integer> theDelaysAtAggregationRatio = new ArrayList<>();
        int k = Math.round(nodeIdList.size() * (1 - aggregationRatio)) + 1;
        for (int i = 0; i < nodeIdList.size(); i++) {
            PriorityQueue<Integer> largeK = new PriorityQueue<>(k + 1);
            for (int j = 0; j < nodeIdList.size(); j++) {
                if (i == j) {
                    continue;
                }
                largeK.add(minDelayMatrix[nodeIdList.get(i)][nodeIdList.get(j)]);
                if (largeK.size() > k) {
                        largeK.poll();
                }
            }
            theDelaysAtAggregationRatio.add(largeK.poll());
        }
//        System.out.println(theDelaysAtAggregationRatio);
        int selectedNodeId = nodeIdList.get(0);
        int minDelay = theDelaysAtAggregationRatio.get(0);
        for (int nodeIndex = 1; nodeIndex < theDelaysAtAggregationRatio.size(); nodeIndex++) {
            if (theDelaysAtAggregationRatio.get(nodeIndex) < minDelay) {
                minDelay = theDelaysAtAggregationRatio.get(nodeIndex);
                selectedNodeId = nodeIdList.get(nodeIndex);
            }
        }
        return selectedNodeId;
    }
    
    public static ArrayList<ArrayList<Integer>> getGraphPartitionResult(ArrayList<Integer> nodeIdList, int k) {
        ArrayList<ArrayList<Integer>> clusterList = new ArrayList<>(3);
        for (int i = 0; i < k; i++) {
            ArrayList<Integer> cluster = new ArrayList<>(nodeIdList.size());
            clusterList.add(cluster);
        }
        Random random = new Random();
        int[] clusterCenterNodeId = new int[k];
        HashSet<Integer> hashSet = new HashSet<>();
        for (int i = 0; i < k; i++) {
            int randomNodeIndex = random.nextInt(nodeIdList.size());
            while (hashSet.contains(randomNodeIndex)) {
                randomNodeIndex = random.nextInt(nodeIdList.size());
            }
            hashSet.add(randomNodeIndex);
            clusterCenterNodeId[i] = nodeIdList.get(randomNodeIndex);
        }
        boolean terminateFlag = false;
        while (!terminateFlag) {
            terminateFlag = true;
            for (int i = 0; i < k; i++) {
                clusterList.get(i).clear();
            }
            for (int i = 0; i < nodeIdList.size(); i++) {
                int nearestClusterCenter = 0;
                int minDelay = TopoUtil.getMinDelay(nodeIdList.get(i), clusterCenterNodeId[0]);
                for (int j = 1; j < k; j++) {
                    if (TopoUtil.getMinDelay(nodeIdList.get(i), clusterCenterNodeId[j]) < minDelay) {
                        nearestClusterCenter = j;
                        minDelay = TopoUtil.getMinDelay(nodeIdList.get(i), clusterCenterNodeId[j]);
                    }
                }
                clusterList.get(nearestClusterCenter).add(nodeIdList.get(i));
            }
            for (int i = 0; i < k; i++) {
                int minTotalDelay = Integer.MAX_VALUE;
                int newCenterNodeId = clusterCenterNodeId[i];
                for (int j = 0; j < clusterList.get(i).size(); j++) {
                    int totalDelay = 0;
                    for (Integer nodeId : clusterList.get(i)) {
                        if (nodeId == clusterList.get(i).get(j)) {
                            continue;
                        }
                        totalDelay += TopoUtil.getMinDelay(nodeId, clusterList.get(i).get(j));
                    }
                    if (totalDelay < minTotalDelay) {
                        minTotalDelay = totalDelay;
                        newCenterNodeId = clusterList.get(i).get(j);
                    }
                }
                if (newCenterNodeId != clusterCenterNodeId[i]) {
                    terminateFlag = false;
                    clusterCenterNodeId[i] = newCenterNodeId;
                }
            }
        }
        return clusterList;
    }
    
    public static ArrayList<ArrayList<Integer>> getClustering() {
        GraphClustering pG = new GraphClustering(g);
        ArrayList<ArrayList<Integer>> result = pG.getGraphPartitionResult(22);
        return result;
    }

//    public static void main(String[] args) {
//        getGraph(1000, "D:/koori/JavaDevelopment/etree/data/data1000.in");
//        generateMinDelayMatrix();
//        ArrayList<Integer> index = new ArrayList<Integer>();
//        for (int i = 0; i < 1000; i++) {
//            index.add(i);
//        }
////        getGraph(100, "/Users/xiyu/Downloads/data100.in");
//        ArrayList<ArrayList<Integer>> arrayList = getGraphPartitionResult(index, 10);
//        System.out.println("minDelay: from 0 -> 3: " + getMinDelay(0, 3));
//    }
}



