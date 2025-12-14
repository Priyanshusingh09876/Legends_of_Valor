package mh.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mh.model.Hero;

public class Party {
    private final List<Hero> heroes = new ArrayList<>();

    public void addHero(Hero hero) {
        if (heroes.size() < 3) {
            heroes.add(hero);
        }
    }

    public List<Hero> getHeroes() {
        return heroes;
    }

    public boolean allFainted() {
        return heroes.stream().allMatch(Hero::isFainted);
    }

    public int highestLevel() {
        return heroes.stream().mapToInt(Hero::getLevel).max().orElse(1);
    }

    public void reviveAll() {
        heroes.forEach(Hero::reviveHalf);
    }

    public String describe() {
        return heroes.stream().map(Hero::fullInfo).collect(Collectors.joining("\n"));
    }
}
