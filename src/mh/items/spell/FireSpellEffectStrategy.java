package mh.items.spell;

import mh.model.Hero;
import mh.model.Monster;

/**
 * // FEATURE FROM LEO: Fire spells burn away monster defenses.
 */
public class FireSpellEffectStrategy implements SpellEffectStrategy {
    @Override
    public void apply(Hero caster, Monster target) {
        target.reduceDefense(0.1);
    }
}
