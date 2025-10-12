package org.politechnika.algorithm.greedy_heuristics;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class NearestNeighborAnyPosition implements Algorithm {

    @Override
    public Solution solve(Instance instance, int startNode) {
        int nodesToSelect = instance.getNodesToSelect();
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        path.add(startNode);
        visited.add(startNode);

        while (path.size() < nodesToSelect) {
            int bestNode = -1;
            int bestPosition = -1;
            double bestIncrease = Double.MAX_VALUE;

            for (int candidateNode = 0; candidateNode < instance.getTotalNodes(); candidateNode++) {
                if (visited.contains(candidateNode)) {
                    continue;
                }

                for (int position = 0; position <= path.size(); position++) {
                    double increase = ObjectiveFunction.calculateInsertionCost(
                            instance, path, candidateNode, position);

                    if (increase < bestIncrease) {
                        bestIncrease = increase;
                        bestNode = candidateNode;
                        bestPosition = position;
                    }
                }
            }

            if (bestNode == -1) {
                throw new RuntimeException("Could not find next node to add");
            }

            path.add(bestPosition, bestNode);
            visited.add(bestNode);
        }

        double objectiveValue = ObjectiveFunction.calculate(instance, path);

        return new Solution(path, objectiveValue, getName(), startNode);
    }

    @Override
    public String getName() {
        return "Nearest Neighbor (Any Position)";
    }
}
