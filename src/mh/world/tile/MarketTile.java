package mh.world.tile;

/**
 * Legacy tile from the pre-{@link Space} map model.
 * Kept for compatibility with earlier code/tests; prefer {@link MarketSpace}.
 */
@Deprecated
public class MarketTile extends Tile {
    public MarketTile() {
        super('M', true);
    }

    @Override
    public boolean isMarket() {
        return true;
    }
}
