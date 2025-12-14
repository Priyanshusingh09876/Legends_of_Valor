package mh.battle.strategy;

import java.util.List;

import mh.model.Hero;
import mh.model.Monster;

/**
 * // FEATURE FROM LEONARDO: BattleStrategy interface centralizes battle orchestration.
 */
public interface BattleStrategy {
    boolean executeBattle(List<Hero> heroes, List<Monster> monsters);
}
