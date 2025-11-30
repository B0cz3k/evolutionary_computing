package org.politechnika.algorithm;

import org.politechnika.algorithm.greedy_regret.RegretK2NNAny;
import org.politechnika.algorithm.local_search.LocalSearchLM;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class LNS implements Algorithm {

    private final long timeLimitMs;
    private final long seed;
    private final boolean useLocalSearchAfterRepair;
    private final double destructionRate;
    private final double costWeight;
    private final double edgeWeight;
    private int iterations = 0;

    public LNS(long timeLimitMs, long seed, boolean useLocalSearchAfterRepair,
               double destructionRate, double costWeight, double edgeWeight) {
        this.timeLimitMs = timeLimitMs;
        this.seed = seed;
        this.useLocalSearchAfterRepair = useLocalSearchAfterRepair;
        this.destructionRate = destructionRate;
        this.costWeight = costWeight;
        this.edgeWeight = edgeWeight;
    }

    public LNS(long timeLimitMs, long seed, boolean useLocalSearchAfterRepair) {
        this(timeLimitMs, seed, useLocalSearchAfterRepair, 0.35, 0.8, 0.2);
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        Random random = new Random(seed);
        long startTime = System.currentTimeMillis();
        iterations = 0;

        RandomSolution randomSolution = new RandomSolution(seed);
        Solution initialSolution = randomSolution.solve(instance, startNode);

        Solution currentSolution = applyLocalSearch(initialSolution, instance);
        Solution bestSolution = currentSolution;

        while (System.currentTimeMillis() - startTime < timeLimitMs) {
            iterations++;

            List<Integer> destroyedSolution = destroy(currentSolution, instance, random);

            Solution repairedSolution = repair(destroyedSolution, instance, random);

            Solution improvedSolution = repairedSolution;
            if (useLocalSearchAfterRepair) {
                improvedSolution = applyLocalSearch(repairedSolution, instance);
            }

            if (improvedSolution.getObjectiveValue() <= currentSolution.getObjectiveValue()) {
                currentSolution = improvedSolution;

                if (currentSolution.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                    bestSolution = currentSolution;
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return new Solution(
            bestSolution.getNodeIds(),
            bestSolution.getObjectiveValue(),
            getName(),
            startNode,
            executionTime
        );
    }

    private List<Integer> destroy(Solution solution, Instance instance, Random random) {
        List<Integer> currentNodes = new ArrayList<>(solution.getNodeIds());
        int nodesToRemove = Math.max(1, (int) (currentNodes.size() * destructionRate));

        for (int i = 0; i < nodesToRemove; i++) {
            if (currentNodes.size() <= 1) break;

            double minCost = Double.MAX_VALUE;
            double maxCost = Double.MIN_VALUE;
            for (int nodeId : currentNodes) {
                double cost = instance.getNode(nodeId).getCost();
                minCost = Math.min(minCost, cost);
                maxCost = Math.max(maxCost, cost);
            }

            double minEdge = Double.MAX_VALUE;
            double maxEdge = Double.MIN_VALUE;
            for (int j = 0; j < currentNodes.size(); j++) {
                int nodeId = currentNodes.get(j);
                int prevIdx = (j - 1 + currentNodes.size()) % currentNodes.size();
                int nextIdx = (j + 1) % currentNodes.size();

                int prevNodeId = currentNodes.get(prevIdx);
                int nextNodeId = currentNodes.get(nextIdx);

                double edgeLength = instance.getDistance(prevNodeId, nodeId) +
                                   instance.getDistance(nodeId, nextNodeId);
                minEdge = Math.min(minEdge, edgeLength);
                maxEdge = Math.max(maxEdge, edgeLength);
            }

            double[] removalProbs = new double[currentNodes.size()];
            double totalProb = 0;

            for (int j = 0; j < currentNodes.size(); j++) {
                int nodeId = currentNodes.get(j);

                double cost = instance.getNode(nodeId).getCost();
                double normalizedCost = (maxCost > minCost) ?
                    (cost - minCost) / (maxCost - minCost) : 0.5;

                int prevIdx = (j - 1 + currentNodes.size()) % currentNodes.size();
                int nextIdx = (j + 1) % currentNodes.size();
                int prevNodeId = currentNodes.get(prevIdx);
                int nextNodeId = currentNodes.get(nextIdx);

                double edgeLength = instance.getDistance(prevNodeId, nodeId) +
                                   instance.getDistance(nodeId, nextNodeId);
                double normalizedEdge = (maxEdge > minEdge) ?
                    (edgeLength - minEdge) / (maxEdge - minEdge) : 0.5;

                double combinedScore = costWeight * normalizedCost + edgeWeight * normalizedEdge;

                removalProbs[j] = combinedScore + 0.01;
                totalProb += removalProbs[j];
            }

            for (int j = 0; j < removalProbs.length; j++) {
                removalProbs[j] /= totalProb;
            }

            double rand = random.nextDouble();
            double cumProb = 0;
            int removeIdx = 0;
            for (int j = 0; j < removalProbs.length; j++) {
                cumProb += removalProbs[j];
                if (rand <= cumProb) {
                    removeIdx = j;
                    break;
                }
            }

            currentNodes.remove(removeIdx);
        }

        return currentNodes;
    }

    private Solution repair(List<Integer> partialSolution, Instance instance, Random random) {
        RegretK2NNAny regretAlgorithm = new RegretK2NNAny(0.5, 0.5);
        int startNode = partialSolution.isEmpty() ? 0 : partialSolution.get(0);
        return regretAlgorithm.solveFromPartialSolution(instance, partialSolution, startNode);
    }

    private Solution applyLocalSearch(Solution solution, Instance instance) {
        Algorithm wrapper = new Algorithm() {
            @Override
            public Solution solve(Instance inst, int start) {
                return solution;
            }

            @Override
            public String getName() {
                return "Wrapper";
            }
        };

        LocalSearchLM localSearch = new LocalSearchLM(wrapper);
        return localSearch.solve(instance, solution.getStartNode());
    }

    @Override
    public String getName() {
        String lsStatus = useLocalSearchAfterRepair ? "with_LS" : "without_LS";
        return String.format("LNS_%s (time=%dms, destruct=%.0f%%, cost_w=%.2f, edge_w=%.2f, iters=%d)",
                             lsStatus, timeLimitMs, destructionRate * 100, costWeight, edgeWeight, iterations);
    }

    public int getIterations() {
        return iterations;
    }
}

