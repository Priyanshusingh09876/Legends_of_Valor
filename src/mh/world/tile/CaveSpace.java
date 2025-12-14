package mh.world.tile;

import java.util.HashMap;
import java.util.Map;

import mh.model.Hero;

public class CaveSpace extends Space {
    private static final double BUFF = 0.1;
    private final Map<Hero, Double> applied = new HashMap<>();

    public CaveSpace() {
        super('C', true);
    }

    @Override
    public void onEnter(Hero hero) {
        if (hero == null) return;
        double delta = hero.getAgility() * BUFF;
        hero.increaseAgility(delta);
        applied.put(hero, delta);
    }

    @Override
    public void onExit(Hero hero) {
        if (hero == null) return;
        Double delta = applied.remove(hero);
        if (delta != null) {
            hero.increaseAgility(-delta);
        }
    }
}
