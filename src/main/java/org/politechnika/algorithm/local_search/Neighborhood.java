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
        List<Integer> nodes = solution.getNodeIdsReadOnly();
        int size = nodes.size();
        HashSet<Integer> usedNodes = new HashSet<>(nodes);
        
        // ReplaceNode moves
        for (int i = 0; i < size; i++) {
            for (Node node2 : instance.getNodes()) {
                if (!usedNodes.contains(node2.getId())) {
                    routeMoves.add(new ReplaceNode(i, node2.getId()));
                }
            }
        }
        
        // Intra-route moves
        if (Objects.equals(intraRoute, "edge")) {
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size - 1; j++) {
                    routeMoves.add(new SwapEdges(i, j));
                }
            }
        } else if (Objects.equals(intraRoute, "node")) {
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size - 1; j++) {
                    routeMoves.add(new SwapNodes(i, j));
                }
            }
        } else {
            throw new RuntimeException("Unknown intraRoute: " + intraRoute);
        }

        Random rnd = new Random(42);
        Collections.shuffle(routeMoves, rnd);
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
