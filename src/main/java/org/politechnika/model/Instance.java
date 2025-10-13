package org.politechnika.model;

import java.util.List;


public class Instance {
    private final String name;
    private final List<Node> nodes;
    private final int[][] distanceMatrix;
    private final int nodesToSelect;

    public Instance(String name, List<Node> nodes, int[][] distanceMatrix) {
        this.name = name;
        this.nodes = nodes;
        this.distanceMatrix = distanceMatrix;
        this.nodesToSelect = (int) Math.ceil(nodes.size() / 2.0);
    }

    public String getName() {
        return name;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getNodesToSelect() {
        return nodesToSelect;
    }

    public int getTotalNodes() {
        return nodes.size();
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    public int getDistance(int nodeId1, int nodeId2) {
        return distanceMatrix[nodeId1][nodeId2];
    }

    @Override
    public String toString() {
        return String.format("Instance{name=%s, totalNodes=%d, nodesToSelect=%d}",
                name, nodes.size(), nodesToSelect);
    }
}
