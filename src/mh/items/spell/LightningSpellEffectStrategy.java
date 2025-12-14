package mh.items.spell;

import mh.model.Hero;
import mh.model.Monster;

/**
 * // FEATURE FROM LEO: Lightning spells cripple monster dodge chance.
 */
public class LightningSpellEffectStrategy implements SpellEffectStrategy {
    @Override
    public void apply(Hero caster, Monster target) {
        target.reduceDodge(0.1);
    }
}
