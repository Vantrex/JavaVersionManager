package de.vantrex.jdkswitcher.util;

public class Tuple<L, R> {

    private final L left;
    private final R right;


    public Tuple(L left, R right) {
        this.right = right;
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public L getLeft() {
        return left;
    }
}
