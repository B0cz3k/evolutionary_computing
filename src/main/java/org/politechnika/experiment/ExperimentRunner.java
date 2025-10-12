package org.politechnika.experiment;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.algorithm.RandomSolution;
import org.politechnika.algorithm.greedy_heuristics.GreedyCycle;
import org.politechnika.algorithm.greedy_heuristics.NearestNeighborAnyPosition;
import org.politechnika.algorithm.greedy_heuristics.NearestNeighborEnd;
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

        results.put("Random", runRandomSolutions(instance));
        results.put("NN_End", runGreedyAlgorithm(instance, new NearestNeighborEnd()));
        results.put("NN_AnyPos", runGreedyAlgorithm(instance, new NearestNeighborAnyPosition()));
        results.put("GreedyCycle", runGreedyAlgorithm(instance, new GreedyCycle()));

        return results;
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

    private static List<Solution> runGreedyAlgorithm(Instance instance, Algorithm algorithm) {
        List<Solution> solutions = new ArrayList<>();
        int totalNodes = instance.getTotalNodes();

        for (int i = 0; i < SOLUTIONS_PER_ALGORITHM; i++) {
            int startNode = i % totalNodes;
            Solution solution = algorithm.solve(instance, startNode);
            solutions.add(solution);
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
