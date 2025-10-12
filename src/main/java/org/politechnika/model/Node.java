package org.politechnika.model;


public class Node {
    private final int id;
    private final double x;
    private final double y;
    private final int cost;

    public Node(int id, double x, double y, int cost) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("Node{id=%d, x=%.2f, y=%.2f, cost=%d}", id, x, y, cost);
    }
}
