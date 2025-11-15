package org.politechnika.algorithm.local_search.route_moves;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwapEdges implements RouteMove {
    private final int nodeIndex1;
    private final int nodeIndex2;
    public SwapEdges(Integer nodeIndex1, Integer nodeIndex2) {
        this.nodeIndex1 = Math.min(nodeIndex1, nodeIndex2);
        this.nodeIndex2 = Math.max(nodeIndex1, nodeIndex2);
    }

    public Tuple<Integer> firstEdge(Solution solution) {
        List<Integer> nodes = solution.getNodeIds();
        return new Tuple<>(nodes.get(nodeIndex1 == 0? solution.getNodeIds().size() - 1 :nodeIndex1 - 1), nodes.get(nodeIndex1));
    }

    public Tuple<Integer> secondEdge(Solution solution) {
        List<Integer> nodes = solution.getNodeIds();
        return new Tuple<>(nodes.get(nodeIndex2), nodes.get(nodeIndex2 == solution.getNodeIds().size() -1 ? 0 : nodeIndex2 + 1));
    }

    @Override
    public double delta(Solution solution, Instance instance) {
        int prevNodeIndex = nodeIndex1 == 0? solution.getNodeIds().size() - 1 :nodeIndex1 - 1;
        int nextNodeIndex = nodeIndex2 == solution.getNodeIds().size() -1 ? 0 : nodeIndex2 + 1;

        Integer prevNode = solution.getNodeIds().get(prevNodeIndex);
        Integer nextNode = solution.getNodeIds().get(nextNodeIndex);
        Integer node1 = solution.getNodeIds().get(nodeIndex1);
        Integer node2 = solution.getNodeIds().get(nodeIndex2);


        return -instance.getDistance(prevNode,node1) -
                instance.getDistance(nextNode,node2) +
                instance.getDistance(prevNode,node2) +
                instance.getDistance(nextNode,node1);
    }


    @Override
    public Solution applyMove(Solution solution, Instance instance) {
        List<Integer> original = solution.getNodeIds();

        List<Integer> newIds = new ArrayList<>(original);
        Collections.reverse(newIds.subList(nodeIndex1, nodeIndex2 + 1));


//        if (solution.getObjectiveValue() + delta(solution, instance) != ObjectiveFunction.calculate(instance,newIds)){
//            throw new IllegalStateException(String.format("SwapEdges: Objective value does not match %f != %f",solution.getObjectiveValue() - delta(solution, instance) , ObjectiveFunction.calculate(instance,newIds)));
//        }

        return new Solution(newIds, solution.getObjectiveValue() + delta(solution, instance),solution.getAlgorithmName(),newIds.getFirst());
    }
}
