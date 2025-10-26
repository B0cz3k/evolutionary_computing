package org.politechnika.algorithm.greedy_regret;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegretK2NNAny implements Algorithm {
    double greedWeight;
    double regretWeight;

    public RegretK2NNAny(double greedWeight, double regretWeight) {
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
        List<Integer> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        path.add(startNode);
        visited.add(startNode);

        while (path.size() < nodesToSelect) {
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

                for (int position = 0; position <= path.size(); position++) {
                    double increase = ObjectiveFunction.calculateInsertionCost(
                            instance, path, candidateNode, position);

                    //Ghetto priority queue
                    if (increase < bestIncrease) {
                        secondBestIncrease = bestIncrease;
                        bestIncrease = increase;

                        bestLocalPosition = position;
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

            path.add(bestPosition, bestNode);
            visited.add(bestNode);
        }

        double objectiveValue = ObjectiveFunction.calculate(instance, path);

        return new Solution(path, objectiveValue, getName(), startNode);
    }

    @Override
    public String getName() {
        return String.format("Greedy 2 Regret Nearest Neighbor (Any Position) %s", greedWeight == 0.0? "": String.format("GreedP = %.2f", greedWeight/(greedWeight - regretWeight)));
    }
}