package org.politechnika.model;

import java.util.ArrayList;
import java.util.List;


public class Solution {
    private final List<Integer> nodeIds;
    private final double objectiveValue;
    private final String algorithmName;
    private final int startNode;

    public Solution(List<Integer> nodeIds, double objectiveValue, String algorithmName, int startNode) {
        this.nodeIds = new ArrayList<>(nodeIds);
        this.objectiveValue = objectiveValue;
        this.algorithmName = algorithmName;
        this.startNode = startNode;
    }

    public List<Integer> getNodeIds() {
        return new ArrayList<>(nodeIds);
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getStartNode() {
        return startNode;
    }

    @Override
    public String toString() {
        return String.format("Solution{algorithm=%s, startNode=%d, objective=%.2f, nodes=%s}",
                algorithmName, startNode, objectiveValue, nodeIds);
    }
}
