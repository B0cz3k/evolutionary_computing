package org.politechnika.util;

public record Tuple<T>(T x, T y) {

    public Tuple<T> reversed() {
        return new Tuple<T>(this.y,this.x);
    }
}
