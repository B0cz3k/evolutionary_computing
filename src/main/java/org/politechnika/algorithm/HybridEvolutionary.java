package org.politechnika.algorithm;

import org.politechnika.algorithm.local_search.LocalSearchLM;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.*;

public class HybridEvolutionary implements Algorithm {
    private final long timeLimitMs;
    private final long seed;
    private final int populationSize;
    private final boolean useLocalSearchAfterRecombination;
    
    private int generations = 0;
    private int localSearchCalls = 0;

    public HybridEvolutionary(long timeLimitMs, long seed, int populationSize, boolean useLocalSearchAfterRecombination) {
        this.timeLimitMs = timeLimitMs;
        this.seed = seed;
        this.populationSize = populationSize;
        this.useLocalSearchAfterRecombination = useLocalSearchAfterRecombination;
    }

    public HybridEvolutionary(long timeLimitMs, long seed, int populationSize) {
        this(timeLimitMs, seed, populationSize, true);
    }

    public HybridEvolutionary(long timeLimitMs, long seed) {
        this(timeLimitMs, seed, 20, true);
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        Random random = new Random(seed);
        long startTime = System.currentTimeMillis();
        generations = 0;
        localSearchCalls = 0;

        List<Solution> population = initializePopulation(instance, random);
        
        Solution bestSolution = getBest(population);

        while (System.currentTimeMillis() - startTime < timeLimitMs) {
            generations++;

            Solution parent1 = population.get(random.nextInt(population.size()));
            Solution parent2 = population.get(random.nextInt(population.size()));

            int attempts = 0;
            while (parent1 == parent2 && attempts < 5) {
                parent2 = population.get(random.nextInt(population.size()));
                attempts++;
            }

            Solution offspring = recombine(parent1, parent2, instance, random);

            if (useLocalSearchAfterRecombination) {
                offspring = applyLocalSearch(offspring, instance);
                localSearchCalls++;
            }

            if (isUnique(offspring, population)) {
                int worstIdx = findWorstIndex(population);
                Solution worst = population.get(worstIdx);

                if (offspring.getObjectiveValue() < worst.getObjectiveValue()) {
                    population.set(worstIdx, offspring);
                    
                    if (offspring.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                        bestSolution = offspring;
                    }
                }
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

    private List<Solution> initializePopulation(Instance instance, Random random) {
        List<Solution> population = new ArrayList<>();

        int attempts = 0;
        int maxAttempts = populationSize * 10;

        while (population.size() < populationSize && attempts < maxAttempts) {
            attempts++;

            RandomSolution randomSolution = new RandomSolution(random.nextLong());
            LocalSearchLM localSearch = new LocalSearchLM(randomSolution);
            Solution solution = localSearch.solve(instance, random.nextInt(instance.getTotalNodes()));
            localSearchCalls++;

            if (isUnique(solution, population)) {
                population.add(solution);
            }
        }

        return population;
    }

    private Solution recombine(Solution parent1, Solution parent2, Instance instance, Random random) {
        List<Integer> nodes1 = parent1.getNodeIds();
        List<Integer> nodes2 = parent2.getNodeIds();
        
        Set<Integer> nodesSet1 = new HashSet<>(nodes1);
        Set<Integer> nodesSet2 = new HashSet<>(nodes2);

        Set<Integer> commonNodes = new HashSet<>(nodesSet1);
        commonNodes.retainAll(nodesSet2);

        Set<Edge> edges1 = extractEdges(nodes1);
        Set<Edge> edges2 = extractEdges(nodes2);
        
        Set<Edge> commonEdges = new HashSet<>();
        for (Edge e : edges1) {
            if (edges2.contains(e) || edges2.contains(e.reversed())) {
                commonEdges.add(e);
            }
        }

        List<List<Integer>> subpaths = buildSubpaths(commonNodes, commonEdges);

        Set<Integer> usedNodes = new HashSet<>();
        for (List<Integer> subpath : subpaths) {
            usedNodes.addAll(subpath);
        }

        int targetSize = instance.getNodesToSelect();
        int nodesToAdd = targetSize - usedNodes.size();
        
        List<Integer> availableNodes = new ArrayList<>();
        for (int i = 0; i < instance.getTotalNodes(); i++) {
            if (!usedNodes.contains(i)) {
                availableNodes.add(i);
            }
        }
        Collections.shuffle(availableNodes, random);

        for (int i = 0; i < nodesToAdd && i < availableNodes.size(); i++) {
            List<Integer> singleNodePath = new ArrayList<>();
            singleNodePath.add(availableNodes.get(i));
            subpaths.add(singleNodePath);
        }

        List<Integer> result = connectSubpathsRandomly(subpaths, random);
        
        double objectiveValue = ObjectiveFunction.calculate(instance, result);
        return new Solution(result, objectiveValue, "Offspring", parent1.getStartNode());
    }

    private Set<Edge> extractEdges(List<Integer> nodes) {
        Set<Edge> edges = new HashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            int from = nodes.get(i);
            int to = nodes.get((i + 1) % nodes.size());
            edges.add(new Edge(from, to));
        }
        return edges;
    }

    private List<List<Integer>> buildSubpaths(Set<Integer> commonNodes, Set<Edge> commonEdges) {
        List<List<Integer>> subpaths = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        Map<Integer, List<Integer>> adjacency = new HashMap<>();
        for (Edge e : commonEdges) {
            adjacency.computeIfAbsent(e.from, k -> new ArrayList<>()).add(e.to);
            adjacency.computeIfAbsent(e.to, k -> new ArrayList<>()).add(e.from);
        }

        for (int node : commonNodes) {
            if (visited.contains(node)) continue;
            
            List<Integer> path = new ArrayList<>();
            buildPathDFS(node, adjacency, visited, path, commonNodes);
            
            if (!path.isEmpty()) {
                subpaths.add(path);
            }
        }
        
        return subpaths;
    }

    private void buildPathDFS(int node, Map<Integer, List<Integer>> adjacency, 
                              Set<Integer> visited, List<Integer> path, Set<Integer> commonNodes) {
        if (visited.contains(node) || !commonNodes.contains(node)) return;
        
        visited.add(node);
        path.add(node);
        
        List<Integer> neighbors = adjacency.getOrDefault(node, Collections.emptyList());
        for (int neighbor : neighbors) {
            if (!visited.contains(neighbor) && commonNodes.contains(neighbor)) {
                buildPathDFS(neighbor, adjacency, visited, path, commonNodes);
            }
        }
    }

    private List<Integer> connectSubpathsRandomly(List<List<Integer>> subpaths, Random random) {
        if (subpaths.isEmpty()) {
            return new ArrayList<>();
        }

        Collections.shuffle(subpaths, random);
        
        List<Integer> result = new ArrayList<>();
        for (List<Integer> subpath : subpaths) {
            if (random.nextBoolean()) {
                Collections.reverse(subpath);
            }
            result.addAll(subpath);
        }
        
        return result;
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

    private boolean isUnique(Solution solution, List<Solution> population) {
        for (Solution s : population) {
            if (Math.abs(s.getObjectiveValue() - solution.getObjectiveValue()) < 0.001) {
                return false;
            }
        }
        return true;
    }

    private int findWorstIndex(List<Solution> population) {
        int worstIdx = 0;
        double worstValue = population.getFirst().getObjectiveValue();
        
        for (int i = 1; i < population.size(); i++) {
            if (population.get(i).getObjectiveValue() > worstValue) {
                worstValue = population.get(i).getObjectiveValue();
                worstIdx = i;
            }
        }
        
        return worstIdx;
    }

    private Solution getBest(List<Solution> population) {
        Solution best = population.getFirst();
        for (Solution s : population) {
            if (s.getObjectiveValue() < best.getObjectiveValue()) {
                best = s;
            }
        }
        return best;
    }

    @Override
    public String getName() {
        String lsStatus = useLocalSearchAfterRecombination ? "withLS" : "noLS";
        return String.format("HybridEvo_%s (time=%dms, pop=%d, gens=%d, lsCalls=%d)",
                             lsStatus, timeLimitMs, populationSize, generations, localSearchCalls);
    }

    private static class Edge {
        final int from;
        final int to;

        Edge(int from, int to) {
            this.from = from;
            this.to = to;
        }

        Edge reversed() {
            return new Edge(to, from);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Edge edge)) return false;
            return from == edge.from && to == edge.to;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }
}
