package org.politechnika.algorithm;

import org.politechnika.algorithm.local_search.LocalSearchLM;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.algorithm.local_search.LocalSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class ILS implements Algorithm {
    
    private final long timeLimitMs;
    private final long seed;
    private final int perturbationStrength;
    private int localSearchRuns = 0;

    public ILS(long timeLimitMs, long seed, int perturbationStrength) {
        this.timeLimitMs = timeLimitMs;
        this.seed = seed;
        this.perturbationStrength = perturbationStrength;
    }
    
    @Override
    public Solution solve(Instance instance, int startNode) {
        Random random = new Random(seed);
        long startTime = System.currentTimeMillis();
        localSearchRuns = 0;

        RandomSolution randomSolution = new RandomSolution(seed);
        LocalSearch localSearch = new LocalSearch(randomSolution, "edge", "steepest");
        Solution currentSolution = localSearch.solve(instance, startNode);
        localSearchRuns++;
        
        Solution bestSolution = currentSolution;

        while (System.currentTimeMillis() - startTime < timeLimitMs) {
            Solution perturbedSolution = perturb(currentSolution, instance, random);

            Solution improvedSolution = applyLocalSearch(perturbedSolution, instance);
            localSearchRuns++;

            if (improvedSolution.getObjectiveValue() <= currentSolution.getObjectiveValue()) {
                currentSolution = improvedSolution;

                if (currentSolution.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                    bestSolution = currentSolution;
                }
            }
        }

        return new Solution(
            bestSolution.getNodeIds(),
            bestSolution.getObjectiveValue(),
            getName(),
            startNode
        );
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
        
        LocalSearch localSearch = new LocalSearchLM(wrapper);
        return localSearch.solve(instance, solution.getStartNode());
    }

    private Solution perturb(Solution solution, Instance instance, Random random) {
        List<Integer> currentNodes = new ArrayList<>(solution.getNodeIds());
        int size = currentNodes.size();

        int quarter = size / 4;
        int pos1 = 1 + random.nextInt(quarter);
        int pos2 = pos1 + 1 + random.nextInt(quarter);
        int pos3 = pos2 + 1 + random.nextInt(quarter);

        List<Integer> segA = currentNodes.subList(0, pos1);
        List<Integer> segB = currentNodes.subList(pos1, pos2);
        List<Integer> segC = currentNodes.subList(pos2, pos3);
        List<Integer> segD = currentNodes.subList(pos3, size);

        // A -> D -> C -> B
        List<Integer> newIds = new ArrayList<>();
        newIds.addAll(segA);
        newIds.addAll(segD); // Swapped
        newIds.addAll(segC); // Original order preserved in segment
        newIds.addAll(segB); // Swapped

        double objectiveValue = org.politechnika.util.ObjectiveFunction.calculate(instance, newIds);

        return new Solution(
                newIds,
                objectiveValue,
                "Perturbed (Double-Bridge)",
                solution.getStartNode()
        );
    }
    
    @Override
    public String getName() {
        return String.format("ILS (timeLimit=%dms, pertStrength=%d, runs=%d)", 
                             timeLimitMs, perturbationStrength, localSearchRuns);
    }
    
    public int getLocalSearchRuns() {
        return localSearchRuns;
    }
}
