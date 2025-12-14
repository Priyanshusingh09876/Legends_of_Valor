package mh.items.spell;

import java.util.EnumMap;
import java.util.Map;

import mh.items.Spell.SpellType;

/**
 * // FEATURE FROM LEONARDO: Centralized registry for spell effect strategies.
 */
public final class SpellEffectStrategies {
    private static final Map<SpellType, SpellEffectStrategy> REGISTRY = new EnumMap<>(SpellType.class);

    static {
        REGISTRY.put(SpellType.ICE, new IceSpellEffectStrategy());
        REGISTRY.put(SpellType.FIRE, new FireSpellEffectStrategy());
        REGISTRY.put(SpellType.LIGHTNING, new LightningSpellEffectStrategy());
    }

    private SpellEffectStrategies() {}

    public static SpellEffectStrategy forType(SpellType type) {
        return REGISTRY.getOrDefault(type, (caster, target) -> {});
    }
}
