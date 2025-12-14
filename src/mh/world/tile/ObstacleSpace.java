package mh.world.tile;

import mh.model.Creature;

public class ObstacleSpace extends Space {
    public ObstacleSpace() {
        super('O', false);
    }

    @Override
    public boolean isWalkableFor(Creature creature) {
        return false;
    }
}
