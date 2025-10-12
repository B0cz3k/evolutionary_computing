package org.politechnika.algorithm;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomSolution implements Algorithm {

    private final Random random;

    public RandomSolution(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        int totalNodes = instance.getTotalNodes();
        int nodesToSelect = instance.getNodesToSelect();

        List<Integer> allNodes = new ArrayList<>();
        for (int i = 0; i < totalNodes; i++) {
            allNodes.add(i);
        }

        Collections.shuffle(allNodes, random);
        List<Integer> selectedNodes = new ArrayList<>(allNodes.subList(0, nodesToSelect));

        double objectiveValue = ObjectiveFunction.calculate(instance, selectedNodes);

        return new Solution(selectedNodes, objectiveValue, getName(), startNode);
    }

    @Override
    public String getName() {
        return "Random Solution";
    }
}
