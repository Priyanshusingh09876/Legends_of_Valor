package mh.world.tile;

import java.util.HashMap;
import java.util.Map;

import mh.model.Hero;

public class KoulouSpace extends Space {
    private static final double BUFF = 0.1;
    private final Map<Hero, Double> applied = new HashMap<>();

    public KoulouSpace() {
        super('K', true);
    }

    @Override
    public void onEnter(Hero hero) {
        if (hero == null) return;
        double delta = hero.getStrength() * BUFF;
        hero.increaseStrength(delta);
        applied.put(hero, delta);
    }

    @Override
    public void onExit(Hero hero) {
        if (hero == null) return;
        Double delta = applied.remove(hero);
        if (delta != null) {
            hero.increaseStrength(-delta);
        }
    }
}
