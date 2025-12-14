package mh.world.tile;

import mh.model.Creature;

/**
 * Nexus spaces act as spawn points and markets for heroes.
 */
public class NexusSpace extends Space {
    private final boolean heroNexus;

    public NexusSpace(boolean heroNexus) {
        super('N', true);
        this.heroNexus = heroNexus;
    }

    @Override
    public boolean isWalkableFor(Creature creature) {
        return true;
    }

    @Override
    public boolean isMarket() {
        return heroNexus;
    }

    public boolean isHeroNexus() {
        return heroNexus;
    }
}
