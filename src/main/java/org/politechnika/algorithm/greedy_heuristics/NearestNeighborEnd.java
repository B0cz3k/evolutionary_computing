package org.politechnika.algorithm.greedy_heuristics;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class NearestNeighborEnd implements Algorithm {

    @Override
    public Solution solve(Instance instance, int startNode) {
        int nodesToSelect = instance.getNodesToSelect();
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        path.add(startNode);
        visited.add(startNode);

        while (path.size() < nodesToSelect) {
            int lastNode = path.getLast();
            int bestNode = -1;
            double bestIncrease = Double.MAX_VALUE;

            for (int candidateNode = 0; candidateNode < instance.getTotalNodes(); candidateNode++) {
                if (visited.contains(candidateNode)) {
                    continue;
                }

                double increase = instance.getDistance(lastNode, candidateNode) +
                                 instance.getNode(candidateNode).getCost();

                if (increase < bestIncrease) {
                    bestIncrease = increase;
                    bestNode = candidateNode;
                }
            }

            if (bestNode == -1) {
                throw new RuntimeException("Could not find next node to add");
            }

            path.add(bestNode);
            visited.add(bestNode);
        }

        double objectiveValue = ObjectiveFunction.calculate(instance, path);

        return new Solution(path, objectiveValue, getName(), startNode);
    }

    @Override
    public String getName() {
        return "Nearest Neighbor (End)";
    }
}
