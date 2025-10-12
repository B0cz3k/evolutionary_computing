package org.politechnika.util;

import org.politechnika.model.Node;

import java.util.List;


public class DistanceCalculator {

    public static int calculateDistance(Node node1, Node node2) {
        double dx = node1.getX() - node2.getX();
        double dy = node1.getY() - node2.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return (int) Math.round(distance);
    }

    public static int[][] buildDistanceMatrix(List<Node> nodes) {
        int n = nodes.size();
        int[][] matrix = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = calculateDistance(nodes.get(i), nodes.get(j));
                }
            }
        }

        return matrix;
    }
}
