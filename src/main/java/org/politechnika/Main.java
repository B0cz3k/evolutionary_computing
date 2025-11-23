package org.politechnika;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.io.InstanceReader;
import org.politechnika.experiment.ExperimentRunner;
import org.politechnika.io.ResultWriter;
import org.politechnika.visualization.SolutionVisualizer;

import java.io.File;
import java.util.Map;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        boolean showVisualization = false;
        boolean saveVisualizations = true;
        boolean saveResults = true;
        String visualizationDir = "visualizations";
        String resultsDir = "results";

        try {
            if (saveVisualizations) {
                java.io.File dir = new File(visualizationDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                    System.out.println("Created output directory: " + visualizationDir);
                }
            }
            if (saveResults) {
                java.io.File dir = new File(resultsDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                    System.out.println("Created output directory: " + resultsDir);
                }
            }

            String[] instanceFiles = {"TSPA.csv", "TSPB.csv"};
            
            for (String fileName : instanceFiles) {
                System.out.println("Processing instance: " + fileName);

                Instance instance = InstanceReader.readInstance(fileName);

                Map<String, List<Solution>> results = ExperimentRunner.runILSandMSLS(instance);
                ExperimentRunner.printSummary(instance.getName(), results);

                System.out.println("Best solutions for each algorithm:");

                for (Map.Entry<String, List<Solution>> entry : results.entrySet()) {
                    List<Solution> solutions = entry.getValue();
                    Solution best = ExperimentRunner.getBestSolution(solutions);
                    
                    if (best != null) {
                        System.out.println("\n" + best.getAlgorithmName() + ":");
                        System.out.println("  Objective: " + best.getObjectiveValue());
                        System.out.println("  Start node: " + best.getStartNode());
                        System.out.println("  Solution: " + best.getNodeIds());

                        if (saveResults) {
                            ResultWriter.saveResults(
                                instance.getName(), 
                                best.getAlgorithmName(), 
                                solutions, 
                                resultsDir
                            );
                        }
                        if (showVisualization) {
                            SolutionVisualizer.show(instance, best);
                        }
                        if (saveVisualizations) {
                            String safeAlgName = best.getAlgorithmName().replaceAll("[^a-zA-Z0-9]", "_");
                            String outputFile = visualizationDir + "/" + instance.getName() + "_" + safeAlgName + ".png";
                            SolutionVisualizer.saveToFile(instance, best, outputFile);
                            System.out.println("  Visualization saved: " + outputFile);
                        }
                    }
                }
            }

            if (saveResults) {
                System.out.println("Results saved to: " + resultsDir + "/");
            }
            if (saveVisualizations) {
                System.out.println("Visualizations saved to: " + visualizationDir + "/");
            }
            if (showVisualization) {
                System.out.println("Close all visualization windows to exit the program.");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}