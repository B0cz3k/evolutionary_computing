package org.politechnika.algorithm.local_search;

import org.politechnika.algorithm.Algorithm;
import org.politechnika.algorithm.local_search.route_moves.RouteMove;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;

public class LocalSearch implements Algorithm {
    private Algorithm seeder;

    public LocalSearch(Algorithm seeder) {
        this.seeder = seeder;
    }

    @Override
    public Solution solve(Instance instance, int startNode) {
        Solution start = this.seeder.solve(instance, startNode);
        start = new Solution(start.getNodeIds(),start.getObjectiveValue(),this.getName(),start.getStartNode());
        boolean converged = false;
        while (!converged) {
            Neighborhood nb = new Neighborhood(start,instance);
            while (nb.hasNext()) {
                RouteMove rm = nb.next();
                double delta = rm.delta(start, instance);
                if (delta < 0){
                    //System.out.printf("Found better solution %f -> %f%n",start.getObjectiveValue(),start.getObjectiveValue()+delta );
                    start = rm.applyMove(start, instance);
                    break;
                }
                converged = true;
            }
        }
        return start;
    }

    @Override
    public String getName() {
        return String.format("Local Search - %s", seeder.getName());
    }
}
