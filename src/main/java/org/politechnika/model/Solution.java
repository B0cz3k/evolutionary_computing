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

    /**
     * Returns a copy of the node IDs list. Use this when you need to modify the list.
     */
    public List<Integer> getNodeIds() {
        return new ArrayList<>(nodeIds);
    }
    
    /**
     * Returns an unmodifiable view of the node IDs list. 
     * Use this for read-only access (much faster than getNodeIds()).
     */
    public List<Integer> getNodeIdsReadOnly() {
        return nodeIds;
    }
    
    /**
     * Get node ID at specific index. Faster than getNodeIds().get(i).
     */
    public int getNodeAt(int index) {
        return nodeIds.get(index);
    }
    
    /**
     * Get size of solution. Faster than getNodeIds().size().
     */
    public int size() {
        return nodeIds.size();
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
