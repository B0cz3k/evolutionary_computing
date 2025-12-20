package org.politechnika.algorithm;

import org.politechnika.algorithm.greedy_heuristics.NearestNeighborAnyPosition;
import org.politechnika.algorithm.local_search.LocalSearchLM;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.Tuple;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HybridEvolutionary2 extends HybridEvolutionary{
    private final int tournamentSize;
    public HybridEvolutionary2(long timeLimitMs, long seed, int populationSize,int tournamentSize, boolean useLocalSearchAfterRecombination) throws Exception {
        super(timeLimitMs, seed, populationSize, useLocalSearchAfterRecombination);
        if (tournamentSize > 2*populationSize){
            throw new Exception("Tournament size should be smaller than the number of available solutions");
        }
        this.tournamentSize = tournamentSize;
    }
    @Override
    List<Solution> initializePopulation(Instance instance, Random random) {
        List<Solution> population = new ArrayList<>();

        int attempts = 0;
        int maxAttempts = populationSize * 10;

        while (population.size() < populationSize && attempts < maxAttempts) {
            attempts++;

            LocalSearchLM localSearch = new LocalSearchLM(new NearestNeighborAnyPosition());
            Solution solution = localSearch.solve(instance, random.nextInt(instance.getTotalNodes()));

            if (isUnique(solution, population)) {
                population.add(solution);
            }
        }

        return population;
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        Random random = new Random(seed);
        long startTime = System.currentTimeMillis();
        generations = 0;

        List<Solution> population = initializePopulation(instance, random);

        Solution bestSolution = getBest(population);

        while (System.currentTimeMillis() - startTime < timeLimitMs) {
            generations++;

            while (population.size() < 2*populationSize) {
                Solution offspring = getOffspring(population, instance, random);

                if (isUnique(offspring, population)) {
                    population.add(offspring);
                }
            }
            Solution currentBest = getBest(population);
            if (currentBest.getObjectiveValue() < bestSolution.getObjectiveValue()){
                bestSolution = currentBest;
            }
            population = pickNextPopulation(population, random);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return new Solution(
                bestSolution.getNodeIds(),
                bestSolution.getObjectiveValue(),
                getName(),
                startNode,
                executionTime, generations
        );
    }

    private Solution getOffspring(List<Solution> population, Instance instance, Random random){
        Solution parent1 = population.get(random.nextInt(population.size()));
        Solution parent2 = population.get(random.nextInt(population.size()));

        int attempts = 0;
        while (parent1 == parent2 && attempts < 5) {
            parent2 = population.get(random.nextInt(population.size()));
            attempts++;
        }

        Solution offspring = recombine(parent1, parent2, instance, random);

        if (random.nextDouble() < 0.2){
            offspring = perturbSolution(offspring,instance,random);
        }

        if (useLocalSearchAfterRecombination) {
            offspring = applyLocalSearch(offspring, instance);
        }

        return  offspring;
    }

    private List<Solution> pickNextPopulation(List<Solution> population, Random random){
        List<Solution> pop = new ArrayList<>();
        for(int i = 0; i < populationSize; i++){
            Collections.shuffle(population,random);
            Solution best = population.getFirst();
            for(int j = 0; j < tournamentSize;j++){
                if(population.get(j).getObjectiveValue() < best.getObjectiveValue()){
                    best = population.get(j);
                }
            }
            pop.add(best);
        }
        return pop;
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
                "HybridEvo2",
                solution.getStartNode()
        );
    }

    @Override
    public String getName() {
        String lsStatus = useLocalSearchAfterRecombination ? "withLS" : "noLS";
        return String.format("ImprovedHybridEvo_%s (time=%dms, pop=%d, gens=%d)",
                lsStatus, timeLimitMs, populationSize, generations);
    }
}
