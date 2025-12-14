package mh.world;

/**
 * @deprecated Use {@link TileView} for read-only terrain queries.
 */
@Deprecated
public interface World extends TileView {

    @Deprecated
    default int getRows() {
        return rows();
    }

    @Deprecated
    default int getCols() {
        return cols();
    }

    @Deprecated
    default boolean isPassable(Position position) {
        return isWalkable(position);
    }

    @Deprecated
    default TileType getTile(Position position) {
        return tileTypeAt(position);
    }

    @Deprecated
    default void printMap() {
        System.out.println("printMap() is deprecated; use MapRenderer.render(...)");
    }
}
