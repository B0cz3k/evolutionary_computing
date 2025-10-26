package org.politechnika.algorithm.local_search.route_moves;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;

public interface RouteMove {
    double delta(Solution solution, Instance instance);
    Solution applyMove(Solution solution, Instance instance);
}
