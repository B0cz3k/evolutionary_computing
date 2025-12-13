package org.politechnika.algorithm.local_search.route_moves;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwapEdges implements RouteMove {
    private final int nodeIndex1;
    private final int nodeIndex2;
    public SwapEdges(Integer nodeIndex1, Integer nodeIndex2) {
        this.nodeIndex1 = Math.min(nodeIndex1, nodeIndex2);
        this.nodeIndex2 = Math.max(nodeIndex1, nodeIndex2);
    }

    public Tuple<Integer> firstEdge(Solution solution) {
        int size = solution.size();
        int prevIndex = nodeIndex1 == 0 ? size - 1 : nodeIndex1 - 1;
        return new Tuple<>(solution.getNodeAt(prevIndex), solution.getNodeAt(nodeIndex1));
    }

    public Tuple<Integer> secondEdge(Solution solution) {
        int size = solution.size();
        int nextIndex = nodeIndex2 == size - 1 ? 0 : nodeIndex2 + 1;
        return new Tuple<>(solution.getNodeAt(nodeIndex2), solution.getNodeAt(nextIndex));
    }

    @Override
    public double delta(Solution solution, Instance instance) {
        int size = solution.size();
        int prevNodeIndex = nodeIndex1 == 0 ? size - 1 : nodeIndex1 - 1;
        int nextNodeIndex = nodeIndex2 == size - 1 ? 0 : nodeIndex2 + 1;

        int prevNode = solution.getNodeAt(prevNodeIndex);
        int nextNode = solution.getNodeAt(nextNodeIndex);
        int node1 = solution.getNodeAt(nodeIndex1);
        int node2 = solution.getNodeAt(nodeIndex2);

        return -instance.getDistance(prevNode, node1) -
                instance.getDistance(nextNode, node2) +
                instance.getDistance(prevNode, node2) +
                instance.getDistance(nextNode, node1);
    }


    @Override
    public Solution applyMove(Solution solution, Instance instance) {
        // Use getNodeIds() here since we need to modify the list
        List<Integer> newIds = solution.getNodeIds();
        Collections.reverse(newIds.subList(nodeIndex1, nodeIndex2 + 1));

        double newObjective = solution.getObjectiveValue() + delta(solution, instance);
        return new Solution(newIds, newObjective, solution.getAlgorithmName(), newIds.getFirst());
    }
}
