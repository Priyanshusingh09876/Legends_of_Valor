package mh.world.tile;

import java.util.Random;

/**
 * // FEATURE FROM PRIYANSHU: Common tiles host random encounter chances.
 */
public class CommonTile extends Tile {
    private static final double ENCOUNTER_CHANCE = 0.3;

    public CommonTile() {
        super(' ', true);
    }

    public boolean shouldTriggerBattle(Random random) {
        return random.nextDouble() < ENCOUNTER_CHANCE;
    }
}
