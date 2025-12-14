package mh.world;

/**
 * Read-only view of a tile-based board. Focuses purely on terrain queries
 * without gameplay rules or unit state.
 */
public interface TileView {

    int rows();

    int cols();

    /**
        * Returns the tile category at the given position.
     */
    TileType tileTypeAt(Position position);

    /**
     * Convenience helper to ask if the tile is walkable.
     */
    default boolean isWalkable(Position position) {
        return tileTypeAt(position).isPassable();
    }
}
