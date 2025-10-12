package org.politechnika.algorithm.greedy_heuristics;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GreedyCycle implements Algorithm {

    @Override
    public Solution solve(Instance instance, int startNode) {
        int nodesToSelect = instance.getNodesToSelect();
        List<Integer> cycle = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        cycle.add(startNode);
        visited.add(startNode);

        int secondNode = findNearestNode(instance, startNode, visited);
        if (secondNode != -1) {
            cycle.add(secondNode);
            visited.add(secondNode);
        }

        if (cycle.size() < nodesToSelect && cycle.size() >= 2) {
            int thirdNode = findBestThirdNode(instance, cycle, visited);
            if (thirdNode != -1) {
                cycle.add(thirdNode);
                visited.add(thirdNode);
            }
        }

        if (nodesToSelect == 1) {
            double objectiveValue = ObjectiveFunction.calculate(instance, cycle);
            return new Solution(cycle, objectiveValue, getName(), startNode);
        }

        while (cycle.size() < nodesToSelect) {
            int bestNode = -1;
            int bestPosition = -1;
            double bestIncrease = Double.MAX_VALUE;

            for (int candidateNode = 0; candidateNode < instance.getTotalNodes(); candidateNode++) {
                if (visited.contains(candidateNode)) {
                    continue;
                }

                for (int i = 0; i < cycle.size(); i++) {
                    int currentNode = cycle.get(i);
                    int nextNode = cycle.get((i + 1) % cycle.size());

                    double removedDistance = instance.getDistance(currentNode, nextNode);
                    double addedDistance = instance.getDistance(currentNode, candidateNode) +
                                          instance.getDistance(candidateNode, nextNode);
                    double candidateCost = instance.getNode(candidateNode).getCost();

                    double increase = addedDistance - removedDistance + candidateCost;

                    if (increase < bestIncrease) {
                        bestIncrease = increase;
                        bestNode = candidateNode;
                        bestPosition = i + 1;
                    }
                }
            }

            if (bestNode == -1) {
                throw new RuntimeException("Could not find next node to add");
            }

            cycle.add(bestPosition, bestNode);
            visited.add(bestNode);
        }

        double objectiveValue = ObjectiveFunction.calculate(instance, cycle);

        return new Solution(cycle, objectiveValue, getName(), startNode);
    }

    private int findNearestNode(Instance instance, int fromNode, Set<Integer> visited) {
        int bestNode = -1;
        double bestValue = Double.MAX_VALUE;

        for (int i = 0; i < instance.getTotalNodes(); i++) {
            if (visited.contains(i)) {
                continue;
            }

            double value = instance.getDistance(fromNode, i) + instance.getNode(i).getCost();
            if (value < bestValue) {
                bestValue = value;
                bestNode = i;
            }
        }

        return bestNode;
    }

    private int findBestThirdNode(Instance instance, List<Integer> cycle, Set<Integer> visited) {
        if (cycle.size() < 2) {
            return -1;
        }

        int firstNode = cycle.get(0);
        int secondNode = cycle.get(1);

        int bestNode = -1;
        double bestObjective = Double.MAX_VALUE;

        for (int i = 0; i < instance.getTotalNodes(); i++) {
            if (visited.contains(i)) {
                continue;
            }

            double objective = instance.getDistance(firstNode, secondNode) +
                             instance.getDistance(secondNode, i) +
                             instance.getDistance(i, firstNode) +
                             instance.getNode(firstNode).getCost() +
                             instance.getNode(secondNode).getCost() +
                             instance.getNode(i).getCost();

            if (objective < bestObjective) {
                bestObjective = objective;
                bestNode = i;
            }
        }

        return bestNode;
    }

    @Override
    public String getName() {
        return "Greedy Cycle";
    }
}
