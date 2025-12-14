package mh.items;

import mh.model.Hero;

public class Potion extends Item {
    public enum Attribute {
        HP, MP, STRENGTH, DEXTERITY, AGILITY
    }

    private final Attribute attribute;
    private final int effectAmount;

    public Potion(String name, int price, int levelRequirement, Attribute attribute, int effectAmount) {
        super(name, price, levelRequirement, 1);
        this.attribute = attribute;
        this.effectAmount = effectAmount;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public int getEffectAmount() {
        return effectAmount;
    }

    public void apply(Hero hero) {
        switch (attribute) {
            case HP:
                double healed = hero.applyHealthPotion(effectAmount);
                System.out.printf("%s recovers %.0f HP (current: %.0f/%.0f)\n", hero.getName(), healed, hero.getHp(), hero.getMaxHp());
                break;
            case MP:
                double beforeMp = hero.getMana();
                hero.restoreMana(effectAmount);
                double restored = hero.getMana() - beforeMp;
                System.out.printf("%s recovers %.0f MP (current: %.0f/%.0f)\n", hero.getName(), restored, hero.getMana(), hero.getMaxMana());
                break;
            case STRENGTH:
                hero.increaseStrength(effectAmount);
                System.out.printf("%s gains %d Strength.\n", hero.getName(), effectAmount);
                break;
            case DEXTERITY:
                hero.increaseDexterity(effectAmount);
                System.out.printf("%s gains %d Dexterity.\n", hero.getName(), effectAmount);
                break;
            case AGILITY:
                hero.increaseAgility(effectAmount);
                System.out.printf("%s gains %d Agility.\n", hero.getName(), effectAmount);
                break;
            default:
                break;
        }
    }

    public void use(Hero hero) {
        apply(hero);
    }

    @Override
    public String getDescription() {
        return String.format("%s (Effect: +%d %s, Level: %d, Price: %d)", getName(), effectAmount, attribute, getLevelRequirement(), getPrice());
    }
}
