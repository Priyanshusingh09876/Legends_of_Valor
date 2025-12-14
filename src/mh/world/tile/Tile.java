package mh.world.tile;

/**
 * // FEATURE FROM LEONARDO: Tile hierarchy improves map modularity and cohesion.
 */
public abstract class Tile {
    private final char symbol;
    private final boolean accessible;

    protected Tile(char symbol, boolean accessible) {
        this.symbol = symbol;
        this.accessible = accessible;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public boolean isMarket() {
        return false;
    }

    public String describe() {
        return getClass().getSimpleName().replace("Tile", "");
    }
}
