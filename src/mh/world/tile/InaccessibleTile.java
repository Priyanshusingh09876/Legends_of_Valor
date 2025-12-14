package mh.world.tile;

/**
 * Legacy tile from the pre-{@link Space} map model.
 * Kept for compatibility with earlier code/tests; prefer {@link InaccessibleSpace}.
 */
@Deprecated
public class InaccessibleTile extends Tile {
    public InaccessibleTile() {
        super('X', false);
    }
}
