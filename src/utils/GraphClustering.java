package utils;

import java.util.ArrayList;

public class GraphClustering {
    private static final int INF = Integer.MAX_VALUE;   // 最大值
    private int[][] mMatrix;
    private int[][] minTree;

    public GraphClustering(int[][] matrix) {
        mMatrix = new int[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                mMatrix[i][j] = matrix[i][j];
            }
        }
        minTree = new int[matrix.length][matrix.length];
    }

    public ArrayList<ArrayList<Integer>> getGraphPartitionResult(int threshold) {
        prim();
        for (int i = 0; i < minTree.length; i++) {
            for (int j = 0; j < minTree.length; j++) {
                if (minTree[i][j] > threshold) {
                    minTree[i][j] = 0;
                }
            }
        }
        boolean[] visited = new boolean[minTree.length];       // 顶点访问标记

        for (int i = 0; i < minTree.length; i++)
            visited[i] = false;

        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for (int i = 0; i < minTree.length; i++) {
            if (!visited[i]) {
                ArrayList<Integer> nodeCollector = new ArrayList<>();
                nodeCollector.add(i);
                DFS(i, visited, nodeCollector);
                result.add(nodeCollector);
            }
        }

        return result;
    }

    private ArrayList<Integer> getAdjacentNode(int node) {
        ArrayList<Integer> adjacentNodes = new ArrayList<>();
        for (int i = 0; i < minTree.length; i++) {
            if (minTree[node][i] > 0) {
                adjacentNodes.add(i);
            }
        }
        return adjacentNodes;
    }

    private void DFS(int i, boolean[] visited, ArrayList<Integer> nodeCollector) {
        visited[i] = true;
        ArrayList<Integer> adjacentNodes = getAdjacentNode(i);
        for (Integer node : adjacentNodes) {
            if (!visited[node]) {
                nodeCollector.add(node);
                DFS(node, visited, nodeCollector);
            }
        }
    }

    private void prim() {
        int num = mMatrix.length;
        int[] weights = new int[num];
        int[] weightsStartNode = new int[num];

        for (int i = 0; i < num; i++ ) {
            weights[i] = mMatrix[0][i];
            weightsStartNode[i] = 0;
        }

        for (int i = 1; i < num; i++) {
            int j = 0;
            int k = 0;
            int min = INF;
            // 在未被加入到最小生成树的顶点中，找出权值最小的顶点。
            while (j < num) {
                // 若weights[j]=0，意味着"第j个节点已经被排序过"(或者说已经加入了最小生成树中)。
                if (weights[j] != 0 && weights[j] < min) {
                    min = weights[j];
                    k = j;
                }
                j++;
            }
            minTree[weightsStartNode[k]][k] = min;
            minTree[k][weightsStartNode[k]] = min;
            weights[k] = 0;
            for (j = 0 ; j < num; j++) {
                if (weights[j] != 0 && mMatrix[k][j] < weights[j]) {
                    weights[j] = mMatrix[k][j];
                    weightsStartNode[j] = k;
                }
            }
        }
    }

    public static void main(String[] args) {
        int matrix[][] = {
                /*1*//*2*//*3*//*4*//*5*//*6*//*7*/
                /*1*/ {   0,  12, INF, INF, INF,  16,  14},
                /*2*/ {  12,   0,  10, INF, INF,   7, INF},
                /*3*/ { INF,  10,   0,   3,   5,   6, INF},
                /*4*/ { INF, INF,   3,   0,   4, INF, INF},
                /*5*/ { INF, INF,   5,   4,   0,   2,   8},
                /*6*/ {  16,   7,   6, INF,   2,   0,   9},
                /*7*/ {  14, INF, INF, INF,   8,   9,   0}};
        GraphClustering pG = new GraphClustering(matrix);
        ArrayList<ArrayList<Integer>> result = pG.getGraphPartitionResult(3);
        for (ArrayList<Integer> tmp : result) {
            for (Integer i : tmp) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }
}
