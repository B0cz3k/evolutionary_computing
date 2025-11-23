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

        // DESIGN: "Stochastic Double-Bridge"
        // We split the tour into 4 segments: A, B, C, D.
        // Standard order: A -> B -> C -> D
        // Perturbed order: A -> D -> C -> B
        // This effectively swaps the middle two segments (B and C) and reverses the flow logic.

        // 1. Generate 3 random cut points to define the 4 segments
        // We ensure segments have minimum length to create a genuine structural change.
        // Indices must be: 1 <= pos1 < pos2 < pos3 < size - 1

        // Heuristic simplification: Divide roughly by 4 to ensure spread
        int quarter = size / 4;
        int pos1 = 1 + random.nextInt(quarter);
        int pos2 = pos1 + 1 + random.nextInt(quarter);
        int pos3 = pos2 + 1 + random.nextInt(quarter);

        // Fallback for very small instances or bad random rolls (just sort random cuts)
        if (pos3 >= size - 1) {
            System.out.println("FALLBACK");
            pos1 = 1 + random.nextInt(size - 4);
            pos2 = pos1 + 1 + random.nextInt(size - pos1 - 2);
            pos3 = pos2 + 1 + random.nextInt(size - pos2 - 1);
        }

        // 2. Define the segments
        // Segment A: Start to pos1
        List<Integer> segA = currentNodes.subList(0, pos1);
        // Segment B: pos1 to pos2
        List<Integer> segB = currentNodes.subList(pos1, pos2);
        // Segment C: pos2 to pos3
        List<Integer> segC = currentNodes.subList(pos2, pos3);
        // Segment D: pos3 to End
        List<Integer> segD = currentNodes.subList(pos3, size);

        // 3. Reassemble in "Double-Bridge" order (A -> D -> C -> B)
        List<Integer> newIds = new ArrayList<>();
        newIds.addAll(segA);
        newIds.addAll(segD); // Swapped
        newIds.addAll(segC); // Original order preserved in segment
        newIds.addAll(segB); // Swapped

        // 4. Calculate new objective
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
