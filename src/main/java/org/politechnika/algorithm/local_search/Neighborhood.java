package org.politechnika.algorithm.local_search;

import org.politechnika.algorithm.local_search.route_moves.ReplaceNode;
import org.politechnika.algorithm.local_search.route_moves.RouteMove;
import org.politechnika.algorithm.local_search.route_moves.SwapEdges;
import org.politechnika.algorithm.local_search.route_moves.SwapNodes;
import org.politechnika.model.Instance;
import org.politechnika.model.Node;
import org.politechnika.model.Solution;

import java.util.*;

public class Neighborhood implements Iterator<RouteMove> {

    private int generatedMemberCount;
    private final ArrayList<RouteMove> routeMoves;

    public Neighborhood(Solution solution, Instance instance, String intraRoute) {
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
                routeMoves.add(new ReplaceNode(i,node2.getId()));
            }
        }
        if (Objects.equals(intraRoute, "edge")) {
            for (int i = 0; i < solution.getNodeIds().size(); i++) {
                //TODO: if i == 0 && j == size-1 then SwapEdges delta calculation fails
                for (int j = i + 1; j < solution.getNodeIds().size() - 1; j++) {
                    routeMoves.add(new SwapEdges(i, j));
                }
            }
        } else if (Objects.equals(intraRoute, "node")) {
            for (int i = 0; i < solution.getNodeIds().size(); i++) {
                //TODO: if i == 0 && j == size-1 then SwapEdges delta calculation fails
                for (int j = i + 1; j < solution.getNodeIds().size()-1; j++) {
                    routeMoves.add(new SwapNodes(i, j));
                }
            }
        } else {
            throw new RuntimeException("Unknown intraRoute: " + intraRoute);
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
