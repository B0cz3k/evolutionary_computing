package org.politechnika.io;

import org.politechnika.model.Solution;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


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
        }

        double avg = sum / solutions.size();

        assert bestSolution != null;
        System.out.println("Instance: " + instanceName);
        System.out.println("Algorithm: " + algorithmName);
        System.out.println("Solutions count: " + solutions.size());
        System.out.printf("Min: %.2f%n", min);
        System.out.printf("Max: %.2f%n", max);
        System.out.printf("Avg: %.2f%n", avg);
        System.out.println("Best solution nodes: " + bestSolution.getNodeIds());
    }

    public static void saveAllSolutions(String instanceName, String algorithmName, 
                                       List<Solution> solutions, String outputDir) throws IOException {
        String safeAlgName = algorithmName.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = String.format("%s/%s-%s.csv",
                                       outputDir, safeAlgName, instanceName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("SolutionID;StartNode;ObjectiveValue;NodeIndices");
            
            for (int i = 0; i < solutions.size(); i++) {
                Solution solution = solutions.get(i);
                String nodesList = solution.getNodeIds().toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(", ", " ");
                
                writer.printf("%d;%d;%.2f;%s%n",
                    i + 1,
                    solution.getStartNode(),
                    solution.getObjectiveValue(),
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
            writer.println("ObjectiveValue;StartNode;NodeIndices");
            
            String nodesList = solution.getNodeIds().toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", " ");
            
            writer.printf("%.2f;%d;%s%n",
                solution.getObjectiveValue(),
                solution.getStartNode(),
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

        for (Solution solution : solutions) {
            double value = solution.getObjectiveValue();
            sum += value;
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        double avg = sum / solutions.size();

        String safeAlgName = algorithmName.replaceAll("[^a-zA-Z0-9]", "_");
        String fileName = String.format("%s/stats-%s-%s.csv",
                                       outputDir, safeAlgName, instanceName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Metric;Value");
            writer.printf("Min;%.2f%n", min);
            writer.printf("Max;%.2f%n", max);
            writer.printf("Avg;%.2f%n", avg);
            writer.printf("SolutionCount;%d%n", solutions.size());
        }
        
        System.out.println("  Saved statistics to: " + fileName);
    }

    public static void saveResults(String instanceName, String algorithmName,
                                   List<Solution> solutions, String outputDir) throws IOException {
        if (solutions.isEmpty()) {
            return;
        }

        java.io.File dir = new java.io.File(outputDir);
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
