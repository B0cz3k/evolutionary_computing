package org.politechnika.algorithm.greedy_regret;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegretK2GreedyCycle implements Algorithm {
    double greedWeight;
    double regretWeight;

    public RegretK2GreedyCycle(double greedWeight, double regretWeight) {
        if (greedWeight < 0 || regretWeight < 0) {
            throw new IllegalArgumentException("Arguments need to be greater or equal 0!");
        }
        this.greedWeight = greedWeight;
        //So that we can search for the min score which means min greed and max regret
        this.regretWeight = -1.0*regretWeight;
    }

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

        if (nodesToSelect == 1) {
            double objectiveValue = ObjectiveFunction.calculate(instance, cycle);
            return new Solution(cycle, objectiveValue, getName(), startNode);
        }

        while (cycle.size() < nodesToSelect) {
            int bestNode = -1;
            int bestPosition = -1;
            double bestScore = Double.MAX_VALUE;

            for (int candidateNode = 0; candidateNode < instance.getTotalNodes(); candidateNode++) {
                if (visited.contains(candidateNode)) {
                    continue;
                }

                //Track two lowest increases
                double bestIncrease = Double.MAX_VALUE;
                double secondBestIncrease = Double.MAX_VALUE;

                //Track the best insertion position
                int bestLocalPosition = -1;

                for (int i = 0; i < cycle.size(); i++) {
                    int currentNode = cycle.get(i);
                    int nextNode = cycle.get((i + 1) % cycle.size());

                    double removedDistance = instance.getDistance(currentNode, nextNode);
                    double addedDistance = instance.getDistance(currentNode, candidateNode) +
                            instance.getDistance(candidateNode, nextNode);
                    double candidateCost = instance.getNode(candidateNode).getCost();

                    double increase = addedDistance - removedDistance + candidateCost;

                    if (increase < bestIncrease) {
                        secondBestIncrease = bestIncrease;
                        bestIncrease = increase;
                        bestLocalPosition = i + 1;
                    }
                    else if (increase < secondBestIncrease) {
                        secondBestIncrease = increase;
                    }
                }

                double regret = secondBestIncrease - bestIncrease;
                double score = this.regretWeight*regret + this.greedWeight*bestIncrease;

                if (score < bestScore) {
                    bestScore = score;
                    bestPosition = bestLocalPosition;
                    bestNode = candidateNode;
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

    @Override
    public String getName() {
        return String.format("Greedy 2 Regret Cycle %s", greedWeight == 0.0? "": String.format("GreedP = %.2f", greedWeight/(greedWeight - regretWeight)));
    }
}

