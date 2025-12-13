package org.politechnika.algorithm.local_search;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.algorithm.local_search.route_moves.ReplaceNode;
import org.politechnika.algorithm.local_search.route_moves.RouteMove;
import org.politechnika.algorithm.local_search.route_moves.SwapEdges;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.RouteMoveWrapper;
import org.politechnika.util.Tuple;

import java.util.*;

public class LocalSearchLM extends LocalSearch{
    private final PriorityQueue<RouteMoveWrapper> queue;
    public LocalSearchLM(Algorithm seeder) {
        super(seeder, "edge", "steepest");
        queue = new PriorityQueue<>(Comparator.comparingDouble(RouteMoveWrapper::delta));
    }

    @Override
    protected Solution bestSteepest(Solution solution, Instance instance) {
        HashMap<Tuple<Integer>, Integer> edges = new HashMap<>();
        List<Integer> nodes = solution.getNodeIdsReadOnly();
        int size = nodes.size();
        for (int i = 0; i < size - 1; i++) {
            edges.put(new Tuple<>(nodes.get(i), nodes.get(i + 1)), i + 1);
        }
        
        // Create a set for fast contains check
        Set<Integer> nodeSet = new HashSet<>(nodes);

        ArrayList<RouteMoveWrapper> toAdd = new ArrayList<>();
        while (!queue.isEmpty()) {
            RouteMoveWrapper wrapper = queue.poll();

            Tuple<Integer> edge1 = wrapper.edges().x();
            Tuple<Integer> edge2 = wrapper.edges().y();

            boolean has1 = edges.containsKey(edge1);
            boolean has2 = edges.containsKey(edge2);
            boolean has1rev = edges.containsKey(edge1.reversed());
            boolean has2rev = edges.containsKey(edge2.reversed());

            if ((has1 && has2 && edges.get(edge1) < edges.get(edge2)) || (has1rev && has2rev && edges.get(edge1.reversed()) > edges.get(edge2.reversed()))) {
                if (wrapper.type().equals("edge")) {
                    SwapEdges move = has1 ? new SwapEdges(edges.get(edge1), edges.get(edge2) - 1) : new SwapEdges(edges.get(edge2.reversed()), edges.get(edge1.reversed()) - 1);
                    queue.addAll(toAdd);
                    return move.applyMove(solution, instance);
                } else if (wrapper.type().equals("node")) {
                    if (nodeSet.contains(wrapper.externalNode())) continue;
                    ReplaceNode move = has1 ? new ReplaceNode(edges.get(edge1), wrapper.externalNode()) : new ReplaceNode(edges.get(edge2.reversed()), wrapper.externalNode());
                    queue.addAll(toAdd);
                    return move.applyMove(solution, instance);
                }
            }
            if ((has1 && has2rev) || (has1rev && has2)) {
                toAdd.add(wrapper);
            }
        }
        queue.addAll(toAdd);

        Neighborhood nb = new Neighborhood(solution, instance, "edge");
        double bestDelta = 0;
        RouteMove bestMove = null;

        while (nb.hasNext()) {
            RouteMove rm = nb.next();

            double delta = rm.delta(solution, instance);

            if (delta < bestDelta) {
                bestDelta = delta;
                bestMove = rm;
            }
            if (delta < 0) {
                if (rm instanceof SwapEdges swapEdges) {
                    queue.add(new RouteMoveWrapper(new Tuple<>(swapEdges.firstEdge(solution), swapEdges.secondEdge(solution)), delta, "edge", null));
                } else if (rm instanceof ReplaceNode replaceNode) {
                    int inNode = replaceNode.getInSolutionNodeIndex();
                    int prevIdx = inNode == 0 ? size - 1 : inNode - 1;
                    int nextIdx = inNode == size - 1 ? 0 : inNode + 1;
                    Tuple<Integer> e1 = new Tuple<>(nodes.get(prevIdx), nodes.get(inNode));
                    Tuple<Integer> e2 = new Tuple<>(nodes.get(inNode), nodes.get(nextIdx));
                    queue.add(new RouteMoveWrapper(new Tuple<>(e1, e2), delta, "node", replaceNode.getOutSolutionNode()));
                }
            }
        }
        return bestMove == null ? solution : bestMove.applyMove(solution, instance);
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        Solution start = this.seeder.solve(instance, startNode);
        start = new Solution(start.getNodeIds(),start.getObjectiveValue(),this.getName(),start.getStartNode());
        boolean converged = false;
        //int steps = 0;
        while (!converged) {
            //steps++;
            double lastScore = start.getObjectiveValue();
            start = this.bestSteepest(start, instance);
            converged = lastScore == start.getObjectiveValue();
        }
        //System.out.printf("Steps: %d %f\n", steps, start.getObjectiveValue());
        return start;
    }

    @Override
    public String getName() {
        return String.format("Local Search LM - %s", seeder.getName());
    }

}
