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
        List<Integer> nodeIds = new ArrayList<>(solution.getNodeIds());
        int n = nodeIds.size();

        for (int swap = 0; swap < perturbationStrength; swap++) {
            int i = random.nextInt(n);
            int j = random.nextInt(n);

            while (Math.abs(i - j) <= 1 || (i == 0 && j == n - 1) || (j == 0 && i == n - 1)) {
                j = random.nextInt(n);
            }
            
            if (i > j) {
                int temp = i;
                i = j;
                j = temp;
            }

            Collections.reverse(nodeIds.subList(i + 1, j + 1));
        }

        double objectiveValue = org.politechnika.util.ObjectiveFunction.calculate(instance, nodeIds);
        
        return new Solution(
            nodeIds,
            objectiveValue,
            "Perturbed",
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
