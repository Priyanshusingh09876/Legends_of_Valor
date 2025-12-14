package mh.world.tile;

import java.util.Random;

/**
 * // FEATURE FROM PRIYANSHU: Common spaces host random encounter chances.
 */
public class CommonSpace extends Space {
    private static final double ENCOUNTER_CHANCE = 0.3;

    public CommonSpace() {
        super(' ', true);
    }

    public boolean shouldTriggerBattle(Random random) {
        return random.nextDouble() < ENCOUNTER_CHANCE;
    }
}
