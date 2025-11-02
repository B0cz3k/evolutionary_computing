package org.politechnika.util;

import org.politechnika.model.Instance;
import org.politechnika.model.Node;

import java.util.*;


public class CandidateEdges {
    private final int candidateCount;
    private final Map<Integer, List<Integer>> candidateNeighbors;

    public CandidateEdges(Instance instance, int candidateCount) {
        this.candidateCount = candidateCount;
        this.candidateNeighbors = new HashMap<>();
        precomputeCandidates(instance);
    }

    private void precomputeCandidates(Instance instance) {
        for (Node node : instance.getNodes()) {
            List<NodeDistance> distances = new ArrayList<>();
            
            for (Node other : instance.getNodes()) {
                if (node.getId() != other.getId()) {
                    double cost = instance.getDistance(node.getId(), other.getId()) + other.getCost();
                    distances.add(new NodeDistance(other.getId(), cost));
                }
            }

            distances.sort(Comparator.comparingDouble(nd -> nd.distance));
            List<Integer> nearest = new ArrayList<>();
            int count = Math.min(candidateCount, distances.size());
            for (int i = 0; i < count; i++) {
                nearest.add(distances.get(i).nodeId);
            }
            
            candidateNeighbors.put(node.getId(), nearest);
        }
    }

    public List<Integer> getCandidateNeighbors(int nodeId) {
        return candidateNeighbors.getOrDefault(nodeId, Collections.emptyList());
    }

    public boolean isCandidate(int nodeId1, int nodeId2) {
        List<Integer> neighbors = candidateNeighbors.get(nodeId1);
        return neighbors != null && neighbors.contains(nodeId2);
    }

    public boolean isCandidateEdge(int nodeId1, int nodeId2) {
        return isCandidate(nodeId1, nodeId2) || isCandidate(nodeId2, nodeId1);
    }

    private static class NodeDistance {
        final int nodeId;
        final double distance;

        NodeDistance(int nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }
}
