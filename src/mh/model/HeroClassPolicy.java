package mh.model;

/**
 * Strategy for class-specific stat bonuses and labels.
 */
public interface HeroClassPolicy {
    void applyInitialBoosts(Hero hero);
    void applyLevelUpBoosts(Hero hero);
    String favoredStatsLabel();
}
