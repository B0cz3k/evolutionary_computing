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
        int size = solution.getNodeIds().size();
        int inNode = solution.getNodeIds().get(inSolutionNodeIndex);
        Integer prev = (inSolutionNodeIndex == 0) ? solution.getNodeIds().getLast()
                : solution.getNodeIds().get(inSolutionNodeIndex - 1);
        Integer next = (inSolutionNodeIndex == size - 1) ? solution.getNodeIds().getFirst()
                : solution.getNodeIds().get(inSolutionNodeIndex + 1);

        return -instance.getDistance(prev,inNode) -
                        instance.getDistance(inNode,next) -
                        instance.getNode(inNode).getCost() +
                        instance.getNode(outSolutionNode).getCost() +
                        instance.getDistance(prev,outSolutionNode) +
                        instance.getDistance(outSolutionNode,next);
    }

    @Override
    public Solution applyMove(Solution solution, Instance instance) {
        List<Integer> newIds = new ArrayList<>(solution.getNodeIds());
        newIds.set(inSolutionNodeIndex, outSolutionNode);

//        if (solution.getObjectiveValue() + delta(solution, instance) != ObjectiveFunction.calculate(instance,newIds)){
//            throw new IllegalStateException(String.format("ReplaceNode: Objective value does not match %f != %f",solution.getObjectiveValue() - delta(solution, instance) , ObjectiveFunction.calculate(instance,newIds)));
//        }
        return new Solution(newIds, solution.getObjectiveValue() + delta(solution, instance),solution.getAlgorithmName(),newIds.getFirst());
    }

}
