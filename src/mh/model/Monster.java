package mh.model;

import java.util.Random;

import mh.items.Armor;
import mh.util.ColorUtil;

public class Monster extends Creature {
    private static final double MONSTER_CRIT_CHANCE = 0.05;

    private double baseDamage;
    private final MonsterType monsterType;
    private final double templateDamage;
    private final double templateDefense;
    private final double templateDodge;

    public Monster(String name, int level, double baseDamage, double defense, double dodgePercent, MonsterType monsterType) {
        // BALANCING FIX: increase monster HP scaling for better survivability.
        super(name, level, level * 120, 0, 0, 0, 0, 0);
        // BALANCING FIX: normalize raw stats so messy data files cannot create absurd values.
        this.baseDamage = normalizeStat(baseDamage);
        this.defense = normalizeStat(defense);
        // Input dodge is provided as a percent in the data files
        this.dodgeChance = clampDodge(dodgePercent / 100.0);
        this.monsterType = monsterType;
        // BALANCING FIX: soften level-1 monsters regardless of file data.
        if (level <= 1) {
            this.baseDamage *= 0.2;
            this.defense *= 0.2;
        }
        applyFavored();
        // Store normalized templates so scaling never reuses inflated numbers.
        this.templateDamage = this.baseDamage;
        this.templateDefense = this.defense;
        this.templateDodge = this.dodgeChance;
    }

    private double clampDodge(double value) {
        return Math.min(0.5, Math.max(0, value));
    }

    private void applyFavored() {
        // Assignment guidance: dragons hit harder, exoskeletons tank more, spirits evade more.
        switch (monsterType) {
            case DRAGON:
                baseDamage *= 1.05;
                break;
            case EXOSKELETON:
                defense *= 1.05;
                break;
            case SPIRIT:
                dodgeChance = clampDodge(dodgeChance + 0.03);
                break;
            default:
                break;
        }
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public double getDefense() {
        return defense;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    public void reduceBaseDamage(double percent) {
        baseDamage *= (1 - percent);
    }

    public void reduceDefense(double percent) {
        defense *= (1 - percent);
    }

    public void reduceDodge(double percent) {
        dodgeChance *= (1 - percent);
    }

    @Override
    public AttackResult attack(Creature target, Random random) {
        if (target == null) {
            return AttackResult.dodged();
        }
        if (random.nextDouble() < target.getDodgeChance()) {
            return AttackResult.dodged();
        }
        double armorMitigation = 0;
        if (target instanceof Hero) {
            Armor armor = ((Hero) target).getEquippedArmor();
            armorMitigation = armor != null ? armor.getDamageReduction() : 0;
        }
        double damage = baseDamage + (level * 5);
        boolean critical = random.nextDouble() < MONSTER_CRIT_CHANCE;
        if (critical) {
            damage *= 2;
        }
        double applied = target.takeDamage(applyDamageReduction(damage, armorMitigation));
        if (applied > 0 && target instanceof Hero) {
            ((Hero) target).tickArmorDurability();
        }
        return AttackResult.hit(applied, critical);
    }

    // FEATURE FROM PRIYANSHU: Monsters scale to the highest hero level.
    public Monster scaledCopyForLevel(int targetLevel) {
        if (targetLevel <= 0) {
            targetLevel = 1;
        }
        double levelDelta = targetLevel - level;
        double scaling = Math.pow(1.05, levelDelta);
        // BALANCING FIX: scale off normalized templates so stats remain within sane bounds.
        double scaledDamage = Math.max(10, templateDamage * 0.2 * scaling);
        double scaledDefense = Math.max(5, templateDefense * 0.2 * scaling);
        double scaledDodge = clampDodge(templateDodge + (0.01 * levelDelta));
        return new Monster(name, targetLevel, scaledDamage, scaledDefense, scaledDodge * 100, monsterType);
    }

    private double normalizeStat(double value) {
        // FEATURE: Balanced normalization to prevent OP monsters even when raw data is extreme.
        return Math.min(70, Math.max(5, value));
    }

    @Override
    public String shortStatus() {
        int currentHp = (int) Math.round(hp);
        int maxHpValue = (int) Math.round(getMaxHp());
        String hpInfo = String.format("%sHP:%s %d/%d  %s",
                ColorUtil.RED, ColorUtil.RESET, currentHp, maxHpValue,
                ColorUtil.hpBar(currentHp, maxHpValue));
        return String.format("%s [%s] (Lvl %d) %s DMG: %.0f DEF: %.0f Dodge: %.2f",
                coloredName(), friendlyTypeColored(), level, hpInfo, baseDamage, defense, dodgeChance);
    }

    private String friendlyTypeColored() {
        switch (monsterType) {
            case DRAGON:
                return ColorUtil.YELLOW + "Dragon" + ColorUtil.RESET;
            case EXOSKELETON:
                return ColorUtil.GREEN + "Exoskeleton" + ColorUtil.RESET;
            case SPIRIT:
                return ColorUtil.MAGENTA + "Spirit" + ColorUtil.RESET;
            default:
                return monsterType.name();
        }
    }

    private String coloredName() {
        String color;
        switch (monsterType) {
            case DRAGON:
                color = ColorUtil.YELLOW;
                break;
            case EXOSKELETON:
                color = ColorUtil.GREEN;
                break;
            case SPIRIT:
                color = ColorUtil.MAGENTA;
                break;
            default:
                color = ColorUtil.WHITE;
        }
        return color + name + ColorUtil.RESET;
    }
}
