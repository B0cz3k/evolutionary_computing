package org.politechnika.algorithm.local_search.route_moves;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.ObjectiveFunction;
import org.politechnika.util.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ReplaceNode implements RouteMove {
    private final Integer inSolutionNodeIndex;
    private final Integer outSolutionNode;
    public ReplaceNode(Integer inSolutionNodeIndex, Integer outSolutionNode) {
        this.inSolutionNodeIndex = inSolutionNodeIndex;
        this.outSolutionNode = outSolutionNode;
    }

    public Integer getInSolutionNodeIndex() {
        return inSolutionNodeIndex;
    }

    public Integer getOutSolutionNode() {
        return outSolutionNode;
    }

    public Tuple<Integer> getReplacedNode(Solution solution){
        return new Tuple<>(solution.getNodeIds().get(inSolutionNodeIndex), outSolutionNode);
    }

    @Override
    public double delta(Solution solution, Instance instance) {
        int size = solution.size();
        int inNode = solution.getNodeAt(inSolutionNodeIndex);
        int prev = (inSolutionNodeIndex == 0) ? solution.getNodeAt(size - 1)
                : solution.getNodeAt(inSolutionNodeIndex - 1);
        int next = (inSolutionNodeIndex == size - 1) ? solution.getNodeAt(0)
                : solution.getNodeAt(inSolutionNodeIndex + 1);

        return -instance.getDistance(prev, inNode) -
                instance.getDistance(inNode, next) -
                instance.getNode(inNode).getCost() +
                instance.getNode(outSolutionNode).getCost() +
                instance.getDistance(prev, outSolutionNode) +
                instance.getDistance(outSolutionNode, next);
    }

    @Override
    public Solution applyMove(Solution solution, Instance instance) {
        // Use getNodeIds() since we need to modify the list
        List<Integer> newIds = solution.getNodeIds();
        newIds.set(inSolutionNodeIndex, outSolutionNode);

        double newObjective = solution.getObjectiveValue() + delta(solution, instance);
        return new Solution(newIds, newObjective, solution.getAlgorithmName(), newIds.getFirst());
    }

}
