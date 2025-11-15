package org.politechnika.util;


public record RouteMoveWrapper(Tuple<Tuple<Integer>> edges, Double delta, String type, Integer externalNode){}
