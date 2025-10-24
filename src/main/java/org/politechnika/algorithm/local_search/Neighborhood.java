package org.politechnika.algorithm.local_search;

import org.politechnika.algorithm.local_search.route_moves.NodeSwap;
import org.politechnika.algorithm.local_search.route_moves.RouteMove;
import org.politechnika.algorithm.local_search.route_moves.SwapEdges;
import org.politechnika.model.Instance;
import org.politechnika.model.Node;
import org.politechnika.model.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class Neighborhood implements Iterator<RouteMove> {

    private int generatedMemberCount;
    private final ArrayList<RouteMove> routeMoves;

    public Neighborhood(Solution solution, Instance instance) {
        this.routeMoves = new ArrayList<>();
        HashSet<Integer> usedNodes = new HashSet<>(solution.getNodeIds());
        for(int i = 0; i < solution.getNodeIds().size(); i++){
            Integer nodeId = solution.getNodeIds().get(i);
            if(!usedNodes.contains(nodeId)){
                continue;
            }
            for (Node node2 :instance.getNodes()){
                if(usedNodes.contains(node2.getId())){
                    continue;
                }
                routeMoves.add(new NodeSwap(i,node2.getId()));
            }
        }
        for(int i = 0; i < solution.getNodeIds().size(); i++){
            //TODO: if i == 0 && j == size-1 then SwapEdges delta calculation fails
            for (int j = i+1; j < solution.getNodeIds().size()-1; j++){
                routeMoves.add(new SwapEdges(i,j));
            }
        }
        Collections.shuffle(routeMoves);
    }

    @Override
    public boolean hasNext() {
        return generatedMemberCount < routeMoves.size();
    }

    @Override
    public RouteMove next() {
        return  routeMoves.get(generatedMemberCount++);
    }
}
