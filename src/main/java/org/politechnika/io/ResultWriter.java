package org.politechnika.io;

import org.politechnika.model.Solution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class ResultWriter {

    public static void printResultsSummary(String instanceName, String algorithmName,
                                          List<Solution> solutions) {
        if (solutions.isEmpty()) {
            System.out.println("No solutions found.");
            return;
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        long sumTime = 0;

        Solution bestSolution = null;

        for (Solution solution : solutions) {
            double value = solution.getObjectiveValue();
            sum += value;
            if (value < min) {
                min = value;
                bestSolution = solution;
            }
            if (value > max) {
                max = value;
            }

            long time = solution.getExecutionTimeMs();
            sumTime += time;
            if (time < minTime) {
                minTime = time;
            }
            if (time > maxTime) {
                maxTime = time;
            }
        }

        double avg = sum / solutions.size();
        double avgTime = (double) sumTime / solutions.size();

        assert bestSolution != null;
        System.out.println("Instance: " + instanceName);
        System.out.println("Algorithm: " + algorithmName);
        System.out.println("Solutions count: " + solutions.size());
        System.out.printf("Min: %.2f%n", min);
        System.out.printf("Max: %.2f%n", max);
        System.out.printf("Avg: %.2f%n", avg);
        System.out.printf("Min Time: %d ms%n", minTime);
        System.out.printf("Max Time: %d ms%n", maxTime);
        System.out.printf("Avg Time: %.2f ms%n", avgTime);
        System.out.println("Best solution nodes: " + bestSolution.getNodeIds());
    }

    public static void saveAllSolutions(String instanceName, String algorithmName, 
                                       List<Solution> solutions, String outputDir) throws IOException {
        String safeAlgName = algorithmName.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = String.format("%s/%s-%s.csv",
                                       outputDir, safeAlgName, instanceName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("SolutionID;StartNode;ObjectiveValue;ExecutionTimeMs;NodeIndices");

            for (int i = 0; i < solutions.size(); i++) {
                Solution solution = solutions.get(i);
                String nodesList = solution.getNodeIds().toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(", ", " ");
                
                writer.printf("%d;%d;%.2f;%d;%s%n",
                    i + 1,
                    solution.getStartNode(),
                    solution.getObjectiveValue(),
                    solution.getExecutionTimeMs(),
                    nodesList);
            }
        }
        
        System.out.println("  Saved all solutions to: " + fileName);
    }

    public static void saveBestSolution(String instanceName, String algorithmName,
                                       Solution solution, String outputDir) throws IOException {
        String safeAlgName = algorithmName.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = String.format("%s/best_solution-%s-%s.csv",
                                       outputDir, safeAlgName, instanceName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("ObjectiveValue;StartNode;ExecutionTimeMs;NodeIndices");

            String nodesList = solution.getNodeIds().toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", " ");
            
            writer.printf("%.2f;%d;%d;%s%n",
                solution.getObjectiveValue(),
                solution.getStartNode(),
                solution.getExecutionTimeMs(),
                nodesList);
        }
        
        System.out.println("  Saved best solution to: " + fileName);
    }

    public static void saveStatistics(String instanceName, String algorithmName,
                                     List<Solution> solutions, String outputDir) throws IOException {
        if (solutions.isEmpty()) {
            return;
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;

        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        long sumTime = 0;

        for (Solution solution : solutions) {
            double value = solution.getObjectiveValue();
            sum += value;
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }

            long time = solution.getExecutionTimeMs();
            sumTime += time;
            if (time < minTime) {
                minTime = time;
            }
            if (time > maxTime) {
                maxTime = time;
            }
        }

        double avg = sum / solutions.size();
        double avgTime = (double) sumTime / solutions.size();

        String safeAlgName = algorithmName.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = String.format("%s/stats-%s-%s.csv",
                                       outputDir, safeAlgName, instanceName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Metric;Value");
            writer.printf("Min;%.2f%n", min);
            writer.printf("Max;%.2f%n", max);
            writer.printf("Avg;%.2f%n", avg);
            writer.printf("MinTime;%d%n", minTime);
            writer.printf("MaxTime;%d%n", maxTime);
            writer.printf("AvgTime;%.2f%n", avgTime);
            writer.printf("SolutionCount;%d%n", solutions.size());
        }
        
        System.out.println("  Saved statistics to: " + fileName);
    }

    public static void saveResults(String instanceName, String algorithmName,
                                   List<Solution> solutions, String outputDir) throws IOException {
        if (solutions.isEmpty()) {
            return;
        }

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        saveAllSolutions(instanceName, algorithmName, solutions, outputDir);
        
        Solution bestSolution = findBestSolution(solutions);
        if (bestSolution != null) {
            saveBestSolution(instanceName, algorithmName, bestSolution, outputDir);
        }
        
        saveStatistics(instanceName, algorithmName, solutions, outputDir);
    }

    private static Solution findBestSolution(List<Solution> solutions) {
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
