package mh.items.spell;

import mh.model.Hero;
import mh.model.Monster;

/**
 * // FEATURE FROM LEONARDO: Strategy interface for elemental spell effects.
 */
public interface SpellEffectStrategy {
    void apply(Hero caster, Monster target);
}
