package mh.model;

import java.util.Random;

/**
 * Base creature abstraction shared by heroes and monsters.
 */
public abstract class Creature implements Attacker {
    protected final String name;
    protected int level;
    protected double hp;
    protected double maxHp;
    protected double strength;
    protected double dexterity;
    protected double agility;
    protected double defense;
    protected double dodgeChance;
    private boolean fainted;

    protected Creature(String name, int level, double hp, double strength, double dexterity, double agility, double defense, double dodgeChance) {
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.maxHp = hp;
        this.strength = strength;
        this.dexterity = dexterity;
        this.agility = agility;
        this.defense = defense;
        this.dodgeChance = dodgeChance;
        this.fainted = hp <= 0;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public double getHp() {
        return hp;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public double getStrength() {
        return strength;
    }

    public double getDexterity() {
        return dexterity;
    }

    public double getAgility() {
        return agility;
    }

    public double getDefense() {
        return defense;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public boolean isFainted() {
        return fainted;
    }

    public boolean isAlive() {
        return !fainted;
    }

    public double takeDamage(double dmg) {
        double applied = Math.max(0, dmg);
        double actual = Math.min(applied, hp);
        hp = Math.max(0, hp - actual);
        if (hp > maxHp) {
            hp = maxHp;
        }
        fainted = hp <= 0;
        return actual;
    }

    public void heal(double amount) {
        if (amount <= 0) {
            return;
        }
        hp = Math.min(hp + amount, maxHp);
        if (hp > 0) {
            fainted = false;
        }
    }

    protected void setMaxHp(double maxHp) {
        this.maxHp = maxHp;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    protected void setFainted(boolean fainted) {
        this.fainted = fainted;
    }

    protected double applyDamageReduction(double damage, double armor) {
        if (damage <= 0) {
            return 0;
        }
        if (armor < 0) {
            armor = 0;
        }
        double reduction = 100.0 / (100.0 + armor);
        return Math.max(1, damage * reduction);
    }

    @Override
    public abstract AttackResult attack(Creature target, Random random);

    public abstract String shortStatus();
}
