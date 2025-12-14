package mh.items.spell;

import mh.model.Hero;
import mh.model.Monster;

/**
 * // FEATURE FROM LEO: Ice spells reduce monster damage output.
 */
public class IceSpellEffectStrategy implements SpellEffectStrategy {
    @Override
    public void apply(Hero caster, Monster target) {
        target.reduceBaseDamage(0.1);
    }
}
