package org.politechnika.algorithm;

import org.politechnika.algorithm.greedy_heuristics.NearestNeighborAnyPosition;
import org.politechnika.algorithm.local_search.LocalSearchLM;
import org.politechnika.algorithm.local_search.route_moves.ReplaceNode;
import org.politechnika.algorithm.local_search.route_moves.SwapEdges;
import org.politechnika.algorithm.local_search.route_moves.SwapNodes;
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

        Solution currentSolution = new NearestNeighborAnyPosition().solve(instance,random.nextInt(instance.getTotalNodes()));

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
            boolean acceptWorse = random.nextDouble() < Math.exp(-delta / temperature);

            if (delta < 0 || acceptWorse) {
                currentSolution = neighbor;
                acceptedMoves++;

                if (currentSolution.getObjectiveValue() < localBest.getObjectiveValue()) {
                    localBest = currentSolution;
                    iterWithoutImprovement = 0;

                    if (localBest.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                        bestSolution = localBest;

                        if (useLocalSearch) {
                            bestSolution = applyLocalSearch(bestSolution, instance);
                            localBest = bestSolution;
                            currentSolution = bestSolution;
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
            if (shouldRestart || random.nextDouble() < 0.5) {
                restarts++;
                temperature = initialTemp;
                iterWithoutImprovement = 0;

                if (random.nextDouble() < 0.5) {
                    currentSolution = perturbSolution(bestSolution, instance, random);
                }else{
                    currentSolution = perturbSolution(localBest,instance,random);
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
            executionTime, totalIterations
        );
    }

    private double cool(double temperature, int iteration) {
        // Exponential cooling: T(k) = T0 * alpha^k
        // Use a slow cooling rate
        double alpha = 0.99995;
        return temperature * alpha;
    }

    private Solution generateRandomSolution(Instance instance, Random random) {
        Solution randomSol = new RandomSolution(random.nextLong()).solve(instance,0);
        return new Solution(randomSol.getNodeIds(), randomSol.getObjectiveValue(), "DualAnnealing", randomSol.getStartNode());
    }

    private Solution generateNeighbor(Solution current, Instance instance, Random random, double temperature) {
        List<Integer> nodes = current.getNodeIds();
        int size = nodes.size();
        
        // Choose move type based on temperature (more diverse moves at high temp)
        double moveSelector = random.nextDouble();
        double tempRatio = temperature / initialTemp;
        
        List<Integer> newNodes;

        if (moveSelector < 0.2) {
            // Move 1: Swap two nodes in the tour (2-opt style)
            newNodes = new SwapNodes(random.nextInt(size),random.nextInt(size)).applyMove(current,instance).getNodeIds();
        } else if (moveSelector < 0.6) {
            // Move 2: Reverse a segment (2-opt)
            newNodes = new SwapEdges(random.nextInt(size),random.nextInt(size-1)).applyMove(current,instance).getNodeIds();
        } else if (moveSelector < 0.8) {
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
        return new Solution(newNodes, objective, "DualAnnealing", newNodes.getFirst());
    }

    private Solution perturbSolution(Solution solution, Instance instance, Random random) {
        List<Integer> currentNodes = new ArrayList<>(solution.getNodeIds());
        int size = currentNodes.size();

        int quarter = size / 4;
        int pos1 = 1 + random.nextInt(quarter);
        int pos2 = pos1 + 1 + random.nextInt(quarter);
        int pos3 = pos2 + 1 + random.nextInt(quarter);

        List<Integer> segA = currentNodes.subList(0, pos1);
        List<Integer> segB = currentNodes.subList(pos1, pos2);
        List<Integer> segC = currentNodes.subList(pos2, pos3);
        List<Integer> segD = currentNodes.subList(pos3, size);

        // A -> D -> C -> B
        List<Integer> newIds = new ArrayList<>();
        newIds.addAll(segA);
        newIds.addAll(segD); // Swapped
        newIds.addAll(segC); // Original order preserved in segment
        newIds.addAll(segB); // Swapped

        double objectiveValue = org.politechnika.util.ObjectiveFunction.calculate(instance, newIds);

        return new Solution(
                newIds,
                objectiveValue,
                "DualAnnealing",
                solution.getStartNode()
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
