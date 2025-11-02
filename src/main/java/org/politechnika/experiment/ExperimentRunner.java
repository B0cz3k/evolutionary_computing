package org.politechnika.experiment;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.algorithm.RandomSolution;
import org.politechnika.algorithm.greedy_heuristics.GreedyCycle;
import org.politechnika.algorithm.greedy_heuristics.NearestNeighborAnyPosition;
import org.politechnika.algorithm.greedy_heuristics.NearestNeighborEnd;
import org.politechnika.algorithm.greedy_regret.RegretK2GreedyCycle;
import org.politechnika.algorithm.greedy_regret.RegretK2NNAny;
import org.politechnika.algorithm.local_search.LocalSearch;
import org.politechnika.algorithm.local_search.LocalSearchCandidate;
import org.politechnika.io.ResultWriter;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExperimentRunner {

    private static final int SOLUTIONS_PER_ALGORITHM = 200;
    private static final long RANDOM_SEED = 0;

    public static Map<String, List<Solution>> runExperiments(Instance instance) {
        Map<String, List<Solution>> results = new HashMap<>();

        System.out.println("\nRunning experiments for instance: " + instance.getName());
        System.out.println("Total nodes: " + instance.getTotalNodes());
        System.out.println("Nodes to select: " + instance.getNodesToSelect());


        System.out.println("\n=== Candidate Local Search ===");
        results.putAll(runCandidateLocalSearch(instance));

        return results;
    }

    private static Map<String, List<Solution>> runGreedy(Instance instance) {
        Map<String, List<Solution>> solutions = new HashMap<>();
        solutions.put("NN_End", runAlgorithm(instance, new NearestNeighborEnd()));
        solutions.put("NN_AnyPos", runAlgorithm(instance, new NearestNeighborAnyPosition()));
        solutions.put("GreedyCycle", runAlgorithm(instance, new GreedyCycle()));
        return solutions;
    }

    private static Map<String, List<Solution>> runGreedyRegret(Instance instance) {
        Map<String, List<Solution>> solutions = new HashMap<>();
        solutions.put("Greedy2Regret_NN_AnyPos", runAlgorithm(instance,new RegretK2NNAny(0.0,1.0)));
        solutions.put("Greedy2Regret_NN_AnyPos_Weighed", runAlgorithm(instance,new RegretK2NNAny(0.9,0.1)));
        solutions.put("Greedy2Regret_Cycle", runAlgorithm(instance,new RegretK2GreedyCycle(0.0,1.0)));
        solutions.put("Greedy2Regret_NN_Cycle", runAlgorithm(instance,new RegretK2GreedyCycle(0.9,0.1)));
        return solutions;
    }

    private static Map<String, List<Solution>> runLocalSearch(Instance instance) {
        Map<String, List<Solution>> solutions = new HashMap<>();
        solutions.put("LocalSearch-NN-edge-greedy", runAlgorithm(instance,new LocalSearch(new NearestNeighborAnyPosition(),"edge","greedy")));
        solutions.put("LocalSearch-NN-edge-steepest", runAlgorithm(instance,new LocalSearch(new NearestNeighborAnyPosition(),"edge","steepest")));
        solutions.put("LocalSearch-NN-node-greedy", runAlgorithm(instance,new LocalSearch(new NearestNeighborAnyPosition(),"node","greedy")));
        solutions.put("LocalSearch-NN-node-steepest", runAlgorithm(instance,new LocalSearch(new NearestNeighborAnyPosition(),"node","steepest")));
        solutions.put("LocalSearch-Random-edge-greedy", runAlgorithm(instance,new LocalSearch(new RandomSolution(42),"edge","greedy")));
        solutions.put("LocalSearch-Random-edge-steepest", runAlgorithm(instance,new LocalSearch(new RandomSolution(42),"edge","steepest")));
        solutions.put("LocalSearch-Random-node-greedy", runAlgorithm(instance,new LocalSearch(new RandomSolution(42),"node","greedy")));
        solutions.put("LocalSearch-Random-node-steepest", runAlgorithm(instance,new LocalSearch(new RandomSolution(42),"node","steepest")));
        return solutions;
    }

    private static Map<String, List<Solution>> runCandidateLocalSearch(Instance instance) {
        Map<String, List<Solution>> solutions = new HashMap<>();

        int[] candidateCounts = {5, 10, 15, 20};
        
        for (int k : candidateCounts) {
            solutions.put(String.format("LocalSearch-Candidate-Random-edge-k%d", k), 
                         runAlgorithm(instance, new LocalSearchCandidate(new RandomSolution(42), "edge", k)));
        }
        
        return solutions;
    }

    private static List<Solution> runRandomSolutions(Instance instance) {
        List<Solution> solutions = new ArrayList<>();

        for (int i = 0; i < SOLUTIONS_PER_ALGORITHM; i++) {
            RandomSolution algorithm = new RandomSolution(RANDOM_SEED + i);
            Solution solution = algorithm.solve(instance, 0);
            solutions.add(solution);
        }

        return solutions;
    }

    private static List<Solution> runAlgorithm(Instance instance, Algorithm algorithm) {
        System.out.printf("Running %s %d times.%n", algorithm.getName(), SOLUTIONS_PER_ALGORITHM);
        List<Solution> solutions = new ArrayList<>();
        int totalNodes = instance.getTotalNodes();

        for (int i = 0; i < SOLUTIONS_PER_ALGORITHM; i++) {
            int startNode = i % totalNodes;
            long startTime = System.nanoTime();
            Solution solution = algorithm.solve(instance, startNode);
            long endTime = System.nanoTime();
            long executionTimeMs = (endTime - startTime) / 1_000_000;

            Solution timedSolution = new Solution(
                solution.getNodeIds(),
                solution.getObjectiveValue(),
                solution.getAlgorithmName(),
                solution.getStartNode(),
                executionTimeMs
            );
            solutions.add(timedSolution);
        }

        return solutions;
    }

    public static void printSummary(String instanceName, Map<String, List<Solution>> results) {
        System.out.println("\nSUMMARY FOR INSTANCE: " + instanceName);

        for (Map.Entry<String, List<Solution>> entry : results.entrySet()) {
            List<Solution> solutions = entry.getValue();

            if (solutions.isEmpty()) {
                continue;
            }

            String algorithmName = solutions.getFirst().getAlgorithmName();
            ResultWriter.printResultsSummary(instanceName, algorithmName, solutions);
        }
    }

    public static Solution getBestSolution(List<Solution> solutions) {
        if (solutions.isEmpty()) {
            return null;
        }

        Solution best = solutions.getFirst();
        for (Solution solution : solutions) {
            if (solution.getObjectiveValue() < best.getObjectiveValue()) {
                best = solution;
            }
        }

        return best;
    }
}
