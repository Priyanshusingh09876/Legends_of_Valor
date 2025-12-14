package mh.model;

import java.util.EnumMap;
import java.util.Map;

import mh.model.Hero.HeroClass;

/**
 * Registry of hero-class strategies.
 */
public final class HeroClassPolicies {
    private static final HeroClassPolicy NO_OP = new HeroClassPolicy() {
        @Override
        public void applyInitialBoosts(Hero hero) {}

        @Override
        public void applyLevelUpBoosts(Hero hero) {}

        @Override
        public String favoredStatsLabel() {
            return "";
        }
    };

    private static final Map<HeroClass, HeroClassPolicy> REGISTRY = new EnumMap<>(HeroClass.class);

    static {
        REGISTRY.put(HeroClass.WARRIOR, new WarriorPolicy());
        REGISTRY.put(HeroClass.SORCERER, new SorcererPolicy());
        REGISTRY.put(HeroClass.PALADIN, new PaladinPolicy());
    }

    private HeroClassPolicies() {}

    public static HeroClassPolicy forClass(HeroClass heroClass) {
        return REGISTRY.getOrDefault(heroClass, NO_OP);
    }

    private static final class WarriorPolicy implements HeroClassPolicy {
        @Override
        public void applyInitialBoosts(Hero hero) {
            hero.applyStatMultipliers(1.05, 1.0, 1.05);
        }

        @Override
        public void applyLevelUpBoosts(Hero hero) {
            hero.applyStatMultipliers(1.05, 1.0, 1.05);
        }

        @Override
        public String favoredStatsLabel() {
            return "Strength & Agility";
        }
    }

    private static final class SorcererPolicy implements HeroClassPolicy {
        @Override
        public void applyInitialBoosts(Hero hero) {
            hero.applyStatMultipliers(1.0, 1.05, 1.05);
        }

        @Override
        public void applyLevelUpBoosts(Hero hero) {
            hero.applyStatMultipliers(1.0, 1.05, 1.05);
        }

        @Override
        public String favoredStatsLabel() {
            return "Dexterity & Agility";
        }
    }

    private static final class PaladinPolicy implements HeroClassPolicy {
        @Override
        public void applyInitialBoosts(Hero hero) {
            hero.applyStatMultipliers(1.05, 1.05, 1.0);
        }

        @Override
        public void applyLevelUpBoosts(Hero hero) {
            hero.applyStatMultipliers(1.05, 1.05, 1.0);
        }

        @Override
        public String favoredStatsLabel() {
            return "Strength & Dexterity";
        }
    }
}
