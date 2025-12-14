package mh.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable representation of a coordinate on an 8x8 game board.
 */
public final class Position {
    private static final int MIN = 0;
    private static final int MAX = 7;

    private final int row;
    private final int col;

    public Position(int row, int col) {
        validate(row, col);
        this.row = row;
        this.col = col;
    }

    private void validate(int row, int col) {
        if (row < MIN || row > MAX || col < MIN || col > MAX) {
            throw new IllegalArgumentException("Position out of bounds: (" + row + "," + col + ")");
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Returns the valid N/W/S/E neighbors within board bounds.
     */
    public List<Position> getCardinalNeighbors() {
        List<Position> neighbors = new ArrayList<>(4);
        if (row - 1 >= MIN) neighbors.add(new Position(row - 1, col));
        if (row + 1 <= MAX) neighbors.add(new Position(row + 1, col));
        if (col - 1 >= MIN) neighbors.add(new Position(row, col - 1));
        if (col + 1 <= MAX) neighbors.add(new Position(row, col + 1));
        return Collections.unmodifiableList(neighbors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "Position{" + "row=" + row + ", col=" + col + '}';
    }
}
