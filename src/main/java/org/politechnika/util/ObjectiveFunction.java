package org.politechnika.util;

import org.politechnika.model.Instance;

import java.util.List;


public class ObjectiveFunction {

    public static double calculate(Instance instance, List<Integer> nodeIds) {
        if (nodeIds.isEmpty()) {
            return 0;
        }

        double totalDistance = 0;
        double totalCost = 0;

        for (int nodeId : nodeIds) {
            totalCost += instance.getNode(nodeId).getCost();
        }

        for (int i = 0; i < nodeIds.size(); i++) {
            int currentNode = nodeIds.get(i);
            int nextNode = nodeIds.get((i + 1) % nodeIds.size());
            totalDistance += instance.getDistance(currentNode, nextNode);
        }

        return totalDistance + totalCost;
    }

    public static double calculateInsertionCost(Instance instance, List<Integer> currentPath, 
                                                 int newNodeId, int position) {
        double cost = instance.getNode(newNodeId).getCost();

        if (currentPath.isEmpty()) {
            return cost;
        }

        if (position == 0) {
            int nextNode = currentPath.getFirst();
            return cost + instance.getDistance(newNodeId, nextNode);
        } else if (position == currentPath.size()) {
            int prevNode = currentPath.getLast();
            return cost + instance.getDistance(prevNode, newNodeId);
        } else {
            int prevNode = currentPath.get(position - 1);
            int nextNode = currentPath.get(position);

            double removedDistance = instance.getDistance(prevNode, nextNode);
            double addedDistance = instance.getDistance(prevNode, newNodeId) + 
                                  instance.getDistance(newNodeId, nextNode);
            
            return cost + addedDistance - removedDistance;
        }
    }
}
