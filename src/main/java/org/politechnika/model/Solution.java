package org.politechnika.model;

import java.util.ArrayList;
import java.util.List;


public class Solution {
    private final List<Integer> nodeIds;
    private final double objectiveValue;
    private final String algorithmName;
    private final int startNode;
    private final long executionTimeMs;

    public Solution(List<Integer> nodeIds, double objectiveValue, String algorithmName, int startNode) {
        this(nodeIds, objectiveValue, algorithmName, startNode, 0);
    }

    public Solution(List<Integer> nodeIds, double objectiveValue, String algorithmName, int startNode, long executionTimeMs) {
        this.nodeIds = new ArrayList<>(nodeIds);
        this.objectiveValue = objectiveValue;
        this.algorithmName = algorithmName;
        this.startNode = startNode;
        this.executionTimeMs = executionTimeMs;
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

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    @Override
    public String toString() {
        return String.format("Solution{algorithm=%s, startNode=%d, objective=%.2f, time=%dms, nodes=%s}",
                algorithmName, startNode, objectiveValue, executionTimeMs, nodeIds);
    }
}
