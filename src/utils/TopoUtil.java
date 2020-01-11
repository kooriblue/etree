package utils;


import java.io.*;
import java.util.PriorityQueue;
import java.util.Queue;

public class TopoUtil {

    public static void generatedGraph(int n, int delayMean, int delayVar) {
        Process pr;
        String exe = "python";
        String command = "/Users/huangjiaming/Documents/developer/etree/data/gen.py";

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
     * If value is 0, then there is no edge between two nodes
     * else it represents the delay between two nodes.
     *
     * Notice that the index of node starts with 0.
     *
     * @param n Network size
     * @param filePath The path of graph data file
     * @return Adjacency matrix
     */
    public static int[][] getGraph(int n, String filePath) {
        int[][] res = new int[n][n];

        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                String[] temp = str.split(" ");
                int from = Integer.parseInt(temp[0])-1;
                int to = Integer.parseInt(temp[1])-1;
                res[from][to] = Integer.parseInt(temp[2]);
                res[to][from] = Integer.parseInt(temp[2]);
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

         for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++)
         System.out.print(res[i][j] + " ");
         System.out.println();
         }

        return res;
    }

    /**
     * Returns the minimum delay from start(node index) to end(node index)
     *
     * Implemented by Dijkstra with heap
     *
     * @param graph
     * @param start message from
     * @param end message to
     * @return the minimum delay
     */
    public static int getMinDelay(int[][] graph, int start, int end) {
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


        int n = graph.length;
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
                if (u != to && graph[u][to] > 0) {
                    int delay = graph[u][to];

                    if (!vis[to] && dis[to] > dis[u] + delay) {
                        dis[to] = dis[u]+delay;
                        que.add(new Edge(to, dis[to]));
                    }
                }
            }
        }
        return dis[end];
    }



    public static void main(String[] args) {
        int[][] g = getGraph(5, "/Users/huangjiaming/Documents/developer/etree/data/data.in");
        System.out.println(getMinDelay(g, 0, 3));
    }
}
