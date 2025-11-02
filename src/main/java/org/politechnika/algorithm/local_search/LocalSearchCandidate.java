package org.politechnika.algorithm.local_search;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.algorithm.local_search.route_moves.RouteMove;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.CandidateEdges;


public class LocalSearchCandidate implements Algorithm {
    private final Algorithm seeder;
    private final String intraRoute;
    private final int candidateCount;

    public LocalSearchCandidate(Algorithm seeder, String intraRoute, int candidateCount) {
        this.seeder = seeder;
        this.intraRoute = intraRoute;
        this.candidateCount = candidateCount;
    }

    private Solution bestSteepest(Solution solution, Instance instance, CandidateEdges candidateEdges) {
        CandidateNeighborhood nb = new CandidateNeighborhood(solution, instance, intraRoute, candidateEdges);
        double bestDelta = 0;
        RouteMove bestMove = null;

        while (nb.hasNext()) {
            RouteMove rm = nb.next();
            double delta = rm.delta(solution, instance);
            if (delta < bestDelta) {
                bestDelta = delta;
                bestMove = rm;
            }
        }
        return bestMove == null ? solution : bestMove.applyMove(solution, instance);
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        CandidateEdges candidateEdges = new CandidateEdges(instance, candidateCount);
        
        Solution start = this.seeder.solve(instance, startNode);
        start = new Solution(start.getNodeIds(), start.getObjectiveValue(), this.getName(), start.getStartNode());
        
        boolean converged = false;
        while (!converged) {
            double lastScore = start.getObjectiveValue();
            start = this.bestSteepest(start, instance, candidateEdges);
            converged = lastScore == start.getObjectiveValue();
        }
        return start;
    }

    @Override
    public String getName() {
        return String.format("Local Search Candidate - %s, %s swap, steepest, k=%d", 
                            seeder.getName(), intraRoute, candidateCount);
    }
}
