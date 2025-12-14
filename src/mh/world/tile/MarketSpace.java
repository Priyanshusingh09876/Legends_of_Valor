package mh.world.tile;

public class MarketSpace extends Space {
    public MarketSpace() {
        super('M', true);
    }

    @Override
    public boolean isMarket() {
        return true;
    }
}
