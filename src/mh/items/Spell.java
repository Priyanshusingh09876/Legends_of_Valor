package mh.items;

import mh.items.spell.SpellEffectStrategies;
import mh.items.spell.SpellEffectStrategy;
import mh.model.Hero;
import mh.model.Monster;

public class Spell extends Item {
    public enum SpellType {
        ICE, FIRE, LIGHTNING
    }

    private final int baseDamage;
    private final int manaCost;
    private final SpellType spellType;
    private final SpellEffectStrategy effectStrategy;

    public Spell(String name, int price, int levelRequirement, int baseDamage, int manaCost, SpellType spellType) {
        super(name, price, levelRequirement, 1);
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
        this.spellType = spellType;
        // FEATURE FROM LEONARDO: Inject spell effect strategy per spell type.
        this.effectStrategy = SpellEffectStrategies.forType(spellType);
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public int getManaCost() {
        return manaCost;
    }

    public SpellType getSpellType() {
        return spellType;
    }

    public void applyEffect(Hero caster, Monster target) {
        effectStrategy.apply(caster, target);
    }

    @Override
    public String getDescription() {
        return String.format("%s (Damage: %d, Mana: %d, Type: %s, Level: %d, Price: %d)", getName(), baseDamage, manaCost, spellType, getLevelRequirement(), getPrice());
    }
}
