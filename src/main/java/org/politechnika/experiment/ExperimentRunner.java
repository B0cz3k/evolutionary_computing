package org.politechnika.experiment;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.algorithm.ILS;
import org.politechnika.algorithm.LNS;
import org.politechnika.algorithm.MSLS;
import org.politechnika.algorithm.RandomSolution;
import org.politechnika.algorithm.greedy_heuristics.*;
import org.politechnika.algorithm.greedy_regret.*;
import org.politechnika.algorithm.local_search.*;
import org.politechnika.io.ResultWriter;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExperimentRunner {

    private static final int SOLUTIONS_PER_ALGORITHM = 200; // For greedy/simple LS
    private static final long RANDOM_SEED = 0;


    public static Map<String, List<Solution>> runILSandMSLS(Instance instance) {
        Map<String, List<Solution>> results = new HashMap<>();

        System.out.println("\nRunning experiments for instance: " + instance.getName());
        System.out.println("Total nodes: " + instance.getTotalNodes());
        System.out.println("Nodes to select: " + instance.getNodesToSelect());

        int mslsRuns = 20;
        int mslsIterations = 200;

        List<Solution> mslsSolutions = runAlgorithm(instance, mslsRuns,
                i -> new MSLS(mslsIterations, i)
        );
        results.put("MSLS", mslsSolutions);

        long averageTimeMsls = calculateAverageTime(mslsSolutions);
        System.out.printf("\nAverage MSLS execution time: %d ms%n", averageTimeMsls);

        int ilsRuns = 20;

        List<Solution> ilsSolutions = runAlgorithm(instance, ilsRuns,
                i -> new ILS(averageTimeMsls, i, 1)
        );
        results.put("ILS", ilsSolutions);

        return results;
    }

    /**
     * Run LNS experiments with and without local search.
     * Uses the average MSLS time as the time limit.
     */
    public static Map<String, List<Solution>> runLNS(Instance instance) {
        Map<String, List<Solution>> results = new HashMap<>();

        System.out.println("\nRunning LNS experiments for instance: " + instance.getName());
        System.out.println("Total nodes: " + instance.getTotalNodes());
        System.out.println("Nodes to select: " + instance.getNodesToSelect());

        // First run MSLS to get average time
        int mslsRuns = 20;
        int mslsIterations = 200;

        List<Solution> mslsSolutions = runAlgorithm(instance, mslsRuns,
                i -> new MSLS(mslsIterations, i)
        );
        results.put("MSLS", mslsSolutions);

        long averageTimeMsls = calculateAverageTime(mslsSolutions);
        System.out.printf("\nAverage MSLS execution time: %d ms (using as time limit for LNS)%n", averageTimeMsls);

        int lnsRuns = 20;

        // LNS with local search (steepest + edge swap)
        // Using 50/50 weights for cost and edge (0.5, 0.5)
        System.out.println("\n=== Running LNS WITH Local Search ===");
        List<Solution> lnsWithLSSolutions = runAlgorithm(instance, lnsRuns,
                i -> new LNS(averageTimeMsls, i, true, 0.3, 0.5, 0.5)
        );
        results.put("LNS_with_LS", lnsWithLSSolutions);

        // LNS without local search
        System.out.println("\n=== Running LNS WITHOUT Local Search ===");
        List<Solution> lnsWithoutLSSolutions = runAlgorithm(instance, lnsRuns,
                i -> new LNS(averageTimeMsls, i, false, 0.3, 0.5, 0.5)
        );
        results.put("LNS_without_LS", lnsWithoutLSSolutions);

        return results;
    }

    private static List<Solution> runAlgorithm(Instance instance, int runs, Function<Integer, Algorithm> algorithmFactory) {
        List<Solution> solutions = new ArrayList<>();
        int totalNodes = instance.getTotalNodes();

        String algoName = algorithmFactory.apply(0).getName().split("\\(")[0];
        System.out.printf("Running %s %d times.%n", algoName, runs);

        for (int i = 0; i < runs; i++) {
            int startNode = i % totalNodes;

            Algorithm algorithm = algorithmFactory.apply(i);

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

            System.out.printf("  Run %d/%d: Objective = %.2f, Time = %d ms%n",
                    i + 1, runs, solution.getObjectiveValue(), executionTimeMs);
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


    private static long calculateAverageTime(List<Solution> solutions) {
        if (solutions.isEmpty()) return 0;
        long totalTime = 0;
        for (Solution solution : solutions) {
            totalTime += solution.getExecutionTimeMs();
        }
        return totalTime / solutions.size();
    }

    public static void printSummary(String instanceName, Map<String, List<Solution>> results) {
        System.out.println("\nSUMMARY FOR INSTANCE: " + instanceName);
        for (Map.Entry<String, List<Solution>> entry : results.entrySet()) {
            List<Solution> solutions = entry.getValue();
            if (solutions.isEmpty()) continue;
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


    private static List<Solution> runRandomSolutions(Instance instance) {
        List<Solution> solutions = new ArrayList<>();

        for (int i = 0; i < SOLUTIONS_PER_ALGORITHM; i++) {
            RandomSolution algorithm = new RandomSolution(RANDOM_SEED + i);
            Solution solution = algorithm.solve(instance, 0);
            solutions.add(solution);
        }

        return solutions;
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
        solutions.put("LocalSearch-Random-edge-steepest", runAlgorithm(instance,new LocalSearch(new RandomSolution(42),"edge","steepest")));
        return solutions;
    }

    private static Map<String, List<Solution>> runCandidateLocalSearch(Instance instance) {
        Map<String, List<Solution>> solutions = new HashMap<>();

        int[] candidateCounts = { 15};

        for (int k : candidateCounts) {
            solutions.put(String.format("LocalSearch-Candidate-Random-edge-k%d", k),
                    runAlgorithm(instance, new LocalSearchCandidate(new RandomSolution(42), "edge", k)));
            solutions.put(String.format("LocalSearch-Candidate-Random-node-k%d", k),
                    runAlgorithm(instance, new LocalSearchCandidate(new RandomSolution(42), "node", k)));
        }

        return solutions;
    }

    private static Map<String, List<Solution>> runLocalSearchLM(Instance instance) {
        Map<String, List<Solution>> solutions = new HashMap<>();
        solutions.put("LocalSearchLM-random-edge-steepest", runAlgorithm(instance,new LocalSearchLM(new RandomSolution(42))));
        return solutions;
    }

}