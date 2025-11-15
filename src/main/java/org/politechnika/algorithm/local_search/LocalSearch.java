package org.politechnika.algorithm.local_search;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.algorithm.local_search.route_moves.RouteMove;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;

import javax.swing.plaf.synth.SynthTextAreaUI;

public class LocalSearch implements Algorithm {
    Algorithm seeder;
    private final String intraRoute;
    private final String strategy;
    public LocalSearch(Algorithm seeder, String intraRoute, String strategy) {
        this.seeder = seeder;
        this.intraRoute = intraRoute;
        this.strategy = strategy;
    }

    private Solution bestGreedy(Solution solution, Instance instance) {
        Neighborhood nb = new Neighborhood(solution,instance,intraRoute);
        while (nb.hasNext()) {
            RouteMove rm = nb.next();
            double delta = rm.delta(solution, instance);
            if (delta < 0) {
                //System.out.printf("Found better solution %f -> %f%n",start.getObjectiveValue(),start.getObjectiveValue()+delta );
                return rm.applyMove(solution, instance);
            }
        }
        return solution;
    }

    protected Solution bestSteepest(Solution solution, Instance instance) {
        Neighborhood nb = new Neighborhood(solution,instance,intraRoute);
        double bestDelta = 0;
        RouteMove bestMove = null;

        while (nb.hasNext()) {
            RouteMove rm = nb.next();
            double delta = rm.delta(solution, instance);
            if (delta < bestDelta) {
                //System.out.printf("Found better solution %f -> %f%n",start.getObjectiveValue(),start.getObjectiveValue()+delta );
                bestDelta = delta;
                bestMove = rm;
            }
        }
        return bestMove == null? solution:bestMove.applyMove(solution, instance);
    }
    @Override
    public Solution solve(Instance instance, int startNode) {
        Solution start = this.seeder.solve(instance, startNode);
        start = new Solution(start.getNodeIds(),start.getObjectiveValue(),this.getName(),start.getStartNode());
        boolean converged = false;
        while (!converged) {
            double lastScore = start.getObjectiveValue();
            if (strategy.equals("greedy")) {
                start = this.bestGreedy(start, instance);
            }
            else if (strategy.equals("steepest")) {
                start = this.bestSteepest(start, instance);
            }
            else {
                throw new RuntimeException("Unknown strategy: " + strategy);
            }
            //System.out.println(start.getObjectiveValue());
            converged = lastScore == start.getObjectiveValue();
        }
        return start;
    }

    @Override
    public String getName() {
        return String.format("Local Search - %s, %s swap, %s", seeder.getName(),intraRoute,strategy);
    }
}
