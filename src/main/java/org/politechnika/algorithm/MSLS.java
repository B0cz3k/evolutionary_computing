package org.politechnika.algorithm;

import org.politechnika.algorithm.local_search.LocalSearchLM;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.algorithm.local_search.LocalSearch;

import java.util.Random;

public class MSLS implements Algorithm {

    private final int iterations;
    private final long seed;

    public MSLS(int iterations, long seed) {
        this.iterations = iterations;
        this.seed = seed;
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        Solution bestSolution = null;

        Random masterRandom = new Random(seed);

        for (int i = 0; i < iterations; i++) {
            long iterationSeed = masterRandom.nextLong();

            RandomSolution randomSolution = new RandomSolution(iterationSeed);

            LocalSearch localSearch = new LocalSearchLM(randomSolution);
            Solution solution = localSearch.solve(instance, startNode);

            if (bestSolution == null || solution.getObjectiveValue() < bestSolution.getObjectiveValue()) {
                bestSolution = solution;
            }
        }

        assert bestSolution != null;
        return new Solution(
                bestSolution.getNodeIds(),
                bestSolution.getObjectiveValue(),
                getName(),
                startNode
        );
    }

    @Override
    public String getName() {
        return String.format("MSLS (iterations=%d)", iterations);
    }
}