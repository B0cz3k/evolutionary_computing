package org.politechnika.algorithm;

import org.politechnika.model.Instance;
import org.politechnika.model.Solution;


public interface Algorithm {

    Solution solve(Instance instance, int startNode);

    String getName();
}
