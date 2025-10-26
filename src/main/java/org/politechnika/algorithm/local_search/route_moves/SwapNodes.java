package org.politechnika.algorithm.local_search.route_moves;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;

import java.util.ArrayList;
import java.util.List;

public class SwapNodes implements RouteMove {
    private final int nodeIndex1;
    private final int nodeIndex2;
    public SwapNodes(Integer nodeIndex1, Integer nodeIndex2) {
        this.nodeIndex1 = Math.min(nodeIndex1, nodeIndex2);
        this.nodeIndex2 = Math.max(nodeIndex1, nodeIndex2);
    }
    @Override
    public double delta(Solution solution, Instance instance) {
        int n = solution.getNodeIds().size();
        if (nodeIndex1 == nodeIndex2) return 0.0;

        int i = Math.min(nodeIndex1, nodeIndex2);
        int j = Math.max(nodeIndex1, nodeIndex2);

        List<Integer> nodes = solution.getNodeIds();
        int prevI = (i == 0) ? n - 1 : i - 1;
        int nextI = (i + 1) % n;
        int prevJ = (j == 0) ? n - 1 : j - 1;
        int nextJ = (j + 1) % n;

        int ni = nodes.get(i);
        int nj = nodes.get(j);

        // adjacent case
        if (nextI == j) {
            int prevNode = nodes.get(prevI);
            int nextNode = nodes.get(nextJ);

            double oldDist = instance.getDistance(prevNode, ni) + instance.getDistance(ni, nj) + instance.getDistance(nj, nextNode);
            double newDist = instance.getDistance(prevNode, nj) + instance.getDistance(nj, ni) + instance.getDistance(ni, nextNode);
            return newDist - oldDist;
        }

        // non-adjacent
        int prevNodeI = nodes.get(prevI);
        int nextNodeI = nodes.get(nextI);
        int prevNodeJ = nodes.get(prevJ);
        int nextNodeJ = nodes.get(nextJ);

        double oldDist =
                instance.getDistance(prevNodeI, ni) +
                        instance.getDistance(ni, nextNodeI) +
                        instance.getDistance(prevNodeJ, nj) +
                        instance.getDistance(nj, nextNodeJ);

        double newDist =
                instance.getDistance(prevNodeI, nj) +
                        instance.getDistance(nj, nextNodeI) +
                        instance.getDistance(prevNodeJ, ni) +
                        instance.getDistance(ni, nextNodeJ);

        return newDist - oldDist;
    }

    @Override
    public Solution applyMove(Solution solution, Instance instance) {
        ArrayList<Integer> newIds = new ArrayList<>(solution.getNodeIds());
        Integer tmp = newIds.get(nodeIndex1);
        newIds.set(nodeIndex1, newIds.get(nodeIndex2));
        newIds.set(nodeIndex2, tmp);

//        if (solution.getObjectiveValue() + delta(solution, instance) != ObjectiveFunction.calculate(instance,newIds)){
//            throw new IllegalStateException(String.format("SwapNodes: Objective value does not match %f != %f",solution.getObjectiveValue() - delta(solution, instance) , ObjectiveFunction.calculate(instance,newIds)));
//        }
        return new Solution(newIds, solution.getObjectiveValue() + delta(solution, instance),solution.getAlgorithmName(),newIds.getFirst());
    }
}
