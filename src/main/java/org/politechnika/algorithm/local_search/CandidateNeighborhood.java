package org.politechnika.algorithm.local_search;

import org.politechnika.algorithm.local_search.route_moves.ReplaceNode;
import org.politechnika.algorithm.local_search.route_moves.RouteMove;
import org.politechnika.algorithm.local_search.route_moves.SwapEdges;
import org.politechnika.algorithm.local_search.route_moves.SwapNodes;
import org.politechnika.model.Instance;
import org.politechnika.model.Solution;
import org.politechnika.util.CandidateEdges;

import java.util.*;


public class CandidateNeighborhood implements Iterator<RouteMove> {

    private int generatedMemberCount;
    private final ArrayList<RouteMove> routeMoves;

    public CandidateNeighborhood(Solution solution, Instance instance, String intraRoute, CandidateEdges candidateEdges) {
        this.routeMoves = new ArrayList<>();
        HashSet<Integer> usedNodes = new HashSet<>(solution.getNodeIds());
        List<Integer> nodeIds = solution.getNodeIds();

        // Generate inter-route moves (ReplaceNode) using candidate edges
        for (int i = 0; i < nodeIds.size(); i++) {
            Integer nodeId = nodeIds.get(i);
            
            // Get previous and next nodes in the tour
            int prevIdx = (i == 0) ? nodeIds.size() - 1 : i - 1;
            int nextIdx = (i == nodeIds.size() - 1) ? 0 : i + 1;
            Integer prevNode = nodeIds.get(prevIdx);
            Integer nextNode = nodeIds.get(nextIdx);

            Set<Integer> candidatesToTry = new HashSet<>();
            
            // Add candidates of the current node being replaced
            candidatesToTry.addAll(candidateEdges.getCandidateNeighbors(nodeId));
            
            // Add candidates of the previous node (creates edge prev->candidate)
            candidatesToTry.addAll(candidateEdges.getCandidateNeighbors(prevNode));
            
            // Add candidates of the next node (creates edge candidate->next)
            candidatesToTry.addAll(candidateEdges.getCandidateNeighbors(nextNode));

            for (Integer candidateNode : candidatesToTry) {
                if (!usedNodes.contains(candidateNode)) {
                    routeMoves.add(new ReplaceNode(i, candidateNode));
                }
            }
        }

        if (Objects.equals(intraRoute, "edge")) {
            generateCandidateEdgeSwaps(nodeIds, candidateEdges);
        } else if (Objects.equals(intraRoute, "node")) {
            generateCandidateNodeSwaps(nodeIds, candidateEdges);
        } else {
            throw new RuntimeException("Unknown intraRoute: " + intraRoute);
        }
    }

    private void generateCandidateEdgeSwaps(List<Integer> nodeIds, CandidateEdges candidateEdges) {
        // Create position map for O(1) lookup
        Map<Integer, Integer> nodeToPosition = new HashMap<>();
        for (int i = 0; i < nodeIds.size(); i++) {
            nodeToPosition.put(nodeIds.get(i), i);
        }
        
        // For each position i, find positions j where swapping introduces a candidate edge
        // After edge swap between i and j: new edges are (prev_i, nodeJ) and (nodeI, next_j)
        for (int i = 0; i < nodeIds.size(); i++) {
            Integer nodeI = nodeIds.get(i);
            int prevI = (i == 0) ? nodeIds.size() - 1 : i - 1;
            Integer prevNodeI = nodeIds.get(prevI);
            
            // Find positions of candidate neighbors that could create edge (prev_i -> nodeJ)
            for (Integer candidateNode : candidateEdges.getCandidateNeighbors(prevNodeI)) {
                Integer pos = nodeToPosition.get(candidateNode);
                if (pos != null && pos > i && pos < nodeIds.size() - 1) {
                    routeMoves.add(new SwapEdges(i, pos));
                }
            }
            
            // Find positions where (nodeI -> next_j) would be a candidate edge
            // Check all candidate neighbors of nodeI
            for (Integer candidateNode : candidateEdges.getCandidateNeighbors(nodeI)) {
                // We need candidateNode to be at position next_j
                // So find where candidateNode is, and check if it's a next position
                Integer pos = nodeToPosition.get(candidateNode);
                if (pos != null && pos > 0) {
                    int j = pos - 1;  // j is the position where next_j = pos
                    if (j > i && j < nodeIds.size() - 1) {
                        routeMoves.add(new SwapEdges(i, j));
                    }
                }
            }
        }
    }

    private void generateCandidateNodeSwaps(List<Integer> nodeIds, CandidateEdges candidateEdges) {
        // Create position map for O(1) lookup
        Map<Integer, Integer> nodeToPosition = new HashMap<>();
        for (int i = 0; i < nodeIds.size(); i++) {
            nodeToPosition.put(nodeIds.get(i), i);
        }
        
        // For each position i, find positions j where swapping introduces a candidate edge
        // After swapping nodes at i and j:
        // - New edges around i: prevNodeI-nodeJ and nodeJ-nextNodeI
        // - New edges around j: prevNodeJ-nodeI and nodeI-nextNodeJ
        
        Set<String> addedPairs = new HashSet<>();  // To avoid duplicates
        
        for (int i = 0; i < nodeIds.size(); i++) {
            Integer nodeI = nodeIds.get(i);
            
            int prevI = (i == 0) ? nodeIds.size() - 1 : i - 1;
            int nextI = (i == nodeIds.size() - 1) ? 0 : i + 1;
            Integer prevNodeI = nodeIds.get(prevI);
            Integer nextNodeI = nodeIds.get(nextI);
            
            // Check candidate neighbors of prevNodeI (creates edge prevNodeI -> nodeJ)
            for (Integer nodeJ : candidateEdges.getCandidateNeighbors(prevNodeI)) {
                Integer j = nodeToPosition.get(nodeJ);
                if (j != null && j > i && j < nodeIds.size() - 1) {
                    String key = i + "-" + j;
                    if (!addedPairs.contains(key)) {
                        routeMoves.add(new SwapNodes(i, j));
                        addedPairs.add(key);
                    }
                }
            }
            
            // Check candidate neighbors of nextNodeI (creates edge nodeJ -> nextNodeI)
            for (Integer nodeJ : candidateEdges.getCandidateNeighbors(nextNodeI)) {
                Integer j = nodeToPosition.get(nodeJ);
                if (j != null && j > i && j < nodeIds.size() - 1) {
                    String key = i + "-" + j;
                    if (!addedPairs.contains(key)) {
                        routeMoves.add(new SwapNodes(i, j));
                        addedPairs.add(key);
                    }
                }
            }
            
            // Check where nodeI could go (creates edges around position j)
            for (Integer candidateOfI : candidateEdges.getCandidateNeighbors(nodeI)) {
                // If nodeI moves to position j, it needs to create candidate edge with neighbors of j
                Integer j = nodeToPosition.get(candidateOfI);
                if (j != null && j > i && j < nodeIds.size() - 1) {
                    int prevJ = (j == 0) ? nodeIds.size() - 1 : j - 1;
                    int nextJ = (j == nodeIds.size() - 1) ? 0 : j + 1;
                    Integer prevNodeJ = nodeIds.get(prevJ);
                    Integer nextNodeJ = nodeIds.get(nextJ);
                    
                    // Check if nodeI is candidate of prevNodeJ or nextNodeJ
                    if (candidateEdges.isCandidate(prevNodeJ, nodeI) || 
                        candidateEdges.isCandidate(nodeI, nextNodeJ)) {
                        String key = i + "-" + j;
                        if (!addedPairs.contains(key)) {
                            routeMoves.add(new SwapNodes(i, j));
                            addedPairs.add(key);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return generatedMemberCount < routeMoves.size();
    }

    @Override
    public RouteMove next() {
        return routeMoves.get(generatedMemberCount++);
    }
    
    public int size() {
        return routeMoves.size();
    }
}
