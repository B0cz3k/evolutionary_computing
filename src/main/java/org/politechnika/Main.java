package org.politechnika;

public class Main {
    public static void main(String[] args) {

        boolean showVisualization = true;
        boolean saveVisualizations = true;
        boolean saveResults = true;
        String visualizationDir = "visualizations";
        String resultsDir = "results";

        try {
            if (saveVisualizations) {
                java.io.File dir = new java.io.File(visualizationDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                    System.out.println("Created output directory: " + visualizationDir);
                }
            }
            if (saveResults) {
                java.io.File dir = new java.io.File(resultsDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                    System.out.println("Created output directory: " + resultsDir);
                }
            }

            String[] instanceFiles = {"TSPA.csv", "TSPB.csv"};
            java.util.Map<String, org.politechnika.model.Instance> instances = new java.util.HashMap<>();
            java.util.Map<String, java.util.Map<String, java.util.List<org.politechnika.model.Solution>>> allResults = new java.util.HashMap<>();
            
            for (String fileName : instanceFiles) {
                org.politechnika.model.Instance instance = org.politechnika.io.InstanceReader.readInstance(fileName);
                instances.put(instance.getName(), instance);

                java.util.Map<String, java.util.List<org.politechnika.model.Solution>> results = 
                    org.politechnika.experiment.ExperimentRunner.runExperiments(instance);
                allResults.put(instance.getName(), results);

                org.politechnika.experiment.ExperimentRunner.printSummary(instance.getName(), results);

                System.out.println("BEST SOLUTIONS FOR EACH ALGORITHM");
                
                for (java.util.Map.Entry<String, java.util.List<org.politechnika.model.Solution>> entry : results.entrySet()) {
                    java.util.List<org.politechnika.model.Solution> solutions = entry.getValue();
                    org.politechnika.model.Solution best = org.politechnika.experiment.ExperimentRunner.getBestSolution(solutions);
                    
                    if (best != null) {
                        System.out.println("\n" + best.getAlgorithmName() + ":");
                        System.out.println("  Objective: " + best.getObjectiveValue());
                        System.out.println("  Start node: " + best.getStartNode());
                        System.out.println("  Solution: " + best.getNodeIds());

                        if (saveResults) {
                            org.politechnika.io.ResultWriter.saveResults(
                                instance.getName(), 
                                best.getAlgorithmName(), 
                                solutions, 
                                resultsDir
                            );
                        }

                        if (showVisualization) {
                            org.politechnika.visualization.SolutionVisualizer.show(instance, best);
                        }

                        if (saveVisualizations) {
                            String safeAlgName = best.getAlgorithmName().replaceAll("[^a-zA-Z0-9]", "_");
                            String outputFile = visualizationDir + "/" + instance.getName() + "_" + safeAlgName + ".png";
                            org.politechnika.visualization.SolutionVisualizer.saveToFile(instance, best, outputFile);
                        }
                    }
                }
            }
            if (saveVisualizations) {
                System.out.println("Visualizations saved to: " + visualizationDir + "/");
            }
            if (saveResults) {
                System.out.println("Results saved to: " + resultsDir + "/");
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