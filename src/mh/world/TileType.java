package mh.world;

/**
 * Enumeration of the world tile categories shared by different game modes.
 */
public enum TileType {
    MARKET('M', true),
    NEXUS('N', true),
    INACCESSIBLE('I', false),
    OBSTACLE('O', false),
    PLAIN('P', true),
    BUSH('B', true),
    CAVE('C', true),
    KOULOU('K', true);

    private final char symbol;
    private final boolean passable;

    TileType(char symbol, boolean passable) {
        this.symbol = symbol;
        this.passable = passable;
    }

    /**
     * @return whether units can traverse this tile.
     */
    public boolean isPassable() {
        return passable;
    }

    /**
     * @return terminal symbol used during map rendering.
     */
    public char getSymbol() {
        return symbol;
    }
}
