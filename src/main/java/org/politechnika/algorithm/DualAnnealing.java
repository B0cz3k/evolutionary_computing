package org.politechnika.algorithm;

import org.politechnika.algorithm.local_search.LocalSearchLM;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.*;

public class DualAnnealing implements Algorithm {
    
    private final long timeLimitMs;
    private final long seed;
    private final double initialTemp;
    private final double restartTempRatio;
    private final int maxIterWithoutImprovement;
    private final boolean useLocalSearch;
    
    private int totalIterations = 0;
    private int restarts = 0;
    private int acceptedMoves = 0;

    public DualAnnealing(long timeLimitMs, long seed, double initialTemp, 
                         double restartTempRatio, int maxIterWithoutImprovement,
                         boolean useLocalSearch) {
        this.timeLimitMs = timeLimitMs;
        this.seed = seed;
        this.initialTemp = initialTemp;
        this.restartTempRatio = restartTempRatio;
        this.maxIterWithoutImprovement = maxIterWithoutImprovement;
        this.useLocalSearch = useLocalSearch;
    }

    public DualAnnealing(long timeLimitMs, long seed) {
        this(timeLimitMs, seed, 10000.0, 0.0001, 1000, true);
    }

    public DualAnnealing(long timeLimitMs, long seed, boolean useLocalSearch) {
        this(timeLimitMs, seed, 10000.0, 0.0001, 1000, useLocalSearch);
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        Random random = new Random(seed);
        long startTime = System.currentTimeMillis();
        totalIterations = 0;
        restarts = 0;
        acceptedMoves = 0;

        Solution currentSolution = generateRandomSolution(instance, random);

        if (useLocalSearch) {
            currentSolution = applyLocalSearch(currentSolution, instance);
        }
        
        Solution bestSolution = currentSolution;
        Solution localBest = currentSolution;

        double temperature = initialTemp;
        double minTemp = initialTemp * restartTempRatio;
        int iterWithoutImprovement = 0;

        while (System.currentTimeMillis() - startTime < timeLimitMs) {
            totalIterations++;

            Solution neighbor = generateNeighbor(currentSolution, instance, random, temperature);

            double delta = neighbor.getObjectiveValue() - currentSolution.getObjectiveValue();

            if (delta < 0 || random.nextDouble() < Math.exp(-delta / temperature)) {
                currentSolution = neighbor;
                acceptedMoves++;

                if (currentSolution.getObjectiveValue() < localBest.getObjectiveValue()) {
                    localBest = currentSolution;
                    iterWithoutImprovement = 0;

                    if (localBest.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                        bestSolution = localBest;

                        if (useLocalSearch) {
                            Solution refined = applyLocalSearch(bestSolution, instance);
                            if (refined.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                                bestSolution = refined;
                                localBest = refined;
                                currentSolution = refined;
                            }
                        }
                    }
                } else {
                    iterWithoutImprovement++;
                }
            } else {
                iterWithoutImprovement++;
            }

            temperature = cool(temperature, totalIterations);

            boolean shouldRestart = false;

            if (temperature < minTemp) {
                shouldRestart = true;
            }

            if (iterWithoutImprovement > maxIterWithoutImprovement) {
                shouldRestart = true;
            }

            // Perform restart
            if (shouldRestart) {
                restarts++;
                temperature = initialTemp;
                iterWithoutImprovement = 0;
                
                // Restart from a new random solution or perturbed best
                if (random.nextDouble() < 0.5) {
                    // Restart from random solution
                    currentSolution = generateRandomSolution(instance, random);
                } else {
                    // Restart from perturbed best solution
                    currentSolution = perturbSolution(bestSolution, instance, random);
                }
                
                if (useLocalSearch) {
                    currentSolution = applyLocalSearch(currentSolution, instance);
                }
                
                localBest = currentSolution;
                
                if (currentSolution.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                    bestSolution = currentSolution;
                }
            }
        }

        // Final local search on best solution
        if (useLocalSearch) {
            Solution finalRefined = applyLocalSearch(bestSolution, instance);
            if (finalRefined.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                bestSolution = finalRefined;
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return new Solution(
            bestSolution.getNodeIds(),
            bestSolution.getObjectiveValue(),
            getName(),
            startNode,
            executionTime
        );
    }

    private double cool(double temperature, int iteration) {
        // Exponential cooling: T(k) = T0 * alpha^k
        // Use a slow cooling rate
        double alpha = 0.99995;
        return temperature * alpha;
    }

    private Solution generateRandomSolution(Instance instance, Random random) {
        int nodesToSelect = instance.getNodesToSelect();
        int totalNodes = instance.getTotalNodes();
        
        List<Integer> allNodes = new ArrayList<>();
        for (int i = 0; i < totalNodes; i++) {
            allNodes.add(i);
        }
        Collections.shuffle(allNodes, random);
        
        List<Integer> selectedNodes = new ArrayList<>(allNodes.subList(0, nodesToSelect));
        double objective = ObjectiveFunction.calculate(instance, selectedNodes);
        
        return new Solution(selectedNodes, objective, "DualAnnealing", selectedNodes.get(0));
    }

    private Solution generateNeighbor(Solution current, Instance instance, Random random, double temperature) {
        List<Integer> nodes = current.getNodeIds();
        int size = nodes.size();
        
        // Choose move type based on temperature (more diverse moves at high temp)
        double moveSelector = random.nextDouble();
        double tempRatio = temperature / initialTemp;
        
        List<Integer> newNodes;
        
        if (moveSelector < 0.4) {
            // Move 1: Swap two nodes in the tour (2-opt style)
            newNodes = new ArrayList<>(nodes);
            int i = random.nextInt(size);
            int j = random.nextInt(size);
            while (j == i) j = random.nextInt(size);
            Collections.swap(newNodes, i, j);
            
        } else if (moveSelector < 0.7) {
            // Move 2: Reverse a segment (2-opt)
            newNodes = new ArrayList<>(nodes);
            int i = random.nextInt(size);
            int j = random.nextInt(size);
            if (i > j) { int tmp = i; i = j; j = tmp; }
            Collections.reverse(newNodes.subList(i, j + 1));
            
        } else if (moveSelector < 0.85) {
            // Move 3: Replace a node with an outside node
            newNodes = new ArrayList<>(nodes);
            Set<Integer> usedNodes = new HashSet<>(nodes);
            
            List<Integer> outsideNodes = new ArrayList<>();
            for (int n = 0; n < instance.getTotalNodes(); n++) {
                if (!usedNodes.contains(n)) {
                    outsideNodes.add(n);
                }
            }
            
            if (!outsideNodes.isEmpty()) {
                int replaceIdx = random.nextInt(size);
                int newNode = outsideNodes.get(random.nextInt(outsideNodes.size()));
                newNodes.set(replaceIdx, newNode);
            }
            
        } else {
            // Move 4: Or-opt - move a segment to a different position
            newNodes = new ArrayList<>(nodes);
            int segmentLength = 1 + random.nextInt(Math.min(3, size / 4 + 1));
            int segmentStart = random.nextInt(size - segmentLength + 1);
            
            List<Integer> segment = new ArrayList<>();
            for (int k = 0; k < segmentLength; k++) {
                segment.add(newNodes.get(segmentStart));
                newNodes.remove(segmentStart);
            }
            
            int insertPos = random.nextInt(newNodes.size() + 1);
            newNodes.addAll(insertPos, segment);
        }
        
        double objective = ObjectiveFunction.calculate(instance, newNodes);
        return new Solution(newNodes, objective, "DualAnnealing", newNodes.get(0));
    }

    private Solution perturbSolution(Solution solution, Instance instance, Random random) {
        Solution perturbed = solution;

        // Apply 3-7 random moves
        int numMoves = 3 + random.nextInt(5);
        for (int i = 0; i < numMoves; i++) {
            perturbed = generateNeighbor(perturbed, instance, random, initialTemp);
        }
        
        return perturbed;
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
        
        LocalSearchLM localSearch = new LocalSearchLM(wrapper);
        return localSearch.solve(instance, solution.getStartNode());
    }

    @Override
    public String getName() {
        String lsStatus = useLocalSearch ? "withLS" : "noLS";
        return String.format("DualAnnealing_%s (time=%dms, iters=%d, restarts=%d, accepted=%d)",
                             lsStatus, timeLimitMs, totalIterations, restarts, acceptedMoves);
    }

    public int getTotalIterations() {
        return totalIterations;
    }

    public int getRestarts() {
        return restarts;
    }

    public int getAcceptedMoves() {
        return acceptedMoves;
    }
}
