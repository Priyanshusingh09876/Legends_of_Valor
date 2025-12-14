package mh.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mh.items.Armor;
import mh.items.Item;
import mh.items.Potion;
import mh.items.Spell;
import mh.items.Weapon;
import mh.util.ColorUtil;

public class Hero extends Creature {
    public enum HeroClass { WARRIOR, SORCERER, PALADIN }

    private static final double HERO_CRIT_CHANCE = 0.10;

    private double mana;
    private double maxMana;
    private int gold;
    private int experience;
    private final HeroClass heroClass;
    private final HeroClassPolicy classPolicy;
    private final Inventory inventory;
    private Weapon equippedWeapon;
    private Armor equippedArmor;
    private final List<Spell> spells;
    private int baseMaxHp;
    private int battleMaxHp;
    private boolean inBattle;

    public Hero(String name, int mana, double strength, double agility, double dexterity, int gold, int experience, HeroClass heroClass) {
        super(name, 1, 100,
                normalizeAttribute(strength),
                normalizeAttribute(dexterity),
                normalizeAttribute(agility),
                0,
                0);
        this.gold = gold;
        this.experience = experience;
        this.heroClass = heroClass;
        this.classPolicy = HeroClassPolicies.forClass(heroClass);
        this.inventory = new Inventory();
        this.mana = normalizeMana(mana);
        this.maxMana = this.mana;
        this.spells = new ArrayList<>();
        this.baseMaxHp = 100 * level;
        this.battleMaxHp = baseMaxHp;
        setBattleMaxHp(baseMaxHp);
        this.hp = battleMaxHp;
        this.inBattle = false;
        classPolicy.applyInitialBoosts(this);
    }

    public double getMana() {
        return mana;
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

    public double getMaxMana() {
        return maxMana;
    }

    @Override
    public double getMaxHp() {
        return battleMaxHp;
    }

    public int getBaseMaxHp() {
        return baseMaxHp;
    }

    public int getBattleMaxHp() {
        return battleMaxHp;
    }

    public void setBattleMaxHp(int value) {
        this.battleMaxHp = value;
        // Keep the inherited cap aligned with the battle-specific ceiling
        setMaxHp(value);
    }

    private void setMaxMana(double value) {
        maxMana = value;
        if (mana > maxMana) {
            mana = maxMana;
        }
    }

    public int getGold() {
        return gold;
    }

    public int getExperience() {
        return experience;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public Armor getEquippedArmor() {
        return equippedArmor;
    }

    public List<Spell> getSpells() {
        return new ArrayList<>(spells);
    }

    public void addSpell(Spell spell) {
        spells.add(spell);
        inventory.addItem(spell);
    }

    public void removeSpell(Spell spell) {
        spells.remove(spell);
        inventory.removeItem(spell);
    }

    public void equipWeapon(Weapon weapon) {
        if (weapon == null) return;
        if (!canEquipItem(weapon)) {
            return;
        }
        swapWeapon(weapon);
    }

    /**
     * Equip a weapon by inventory index (0-based).
     * Only inventory weapons are shown to players; the equipped one is never duplicated.
     */
    public boolean equipWeapon(int inventoryIndex) {
        List<Weapon> weapons = inventory.getWeapons();
        if (inventoryIndex < 0 || inventoryIndex >= weapons.size()) {
            return false;
        }
        Weapon chosen = weapons.get(inventoryIndex);
        if (!canEquipItem(chosen)) {
            return false;
        }
        swapWeapon(chosen);
        return true;
    }

    private void swapWeapon(Weapon chosen) {
        if (equippedWeapon != null && equippedWeapon.isUsable() && !inventory.getAllItems().contains(equippedWeapon)) {
            // Return previously equipped weapon to the bag exactly once
            inventory.addItem(equippedWeapon);
        }
        equippedWeapon = chosen;
        inventory.removeItem(chosen);
    }

    public void equipArmor(Armor armor) {
        if (armor == null) return;
        if (!canEquipItem(armor)) {
            return;
        }
        swapArmor(armor);
    }

    public boolean equipArmor(int inventoryIndex) {
        List<Armor> armors = inventory.getArmors();
        if (inventoryIndex < 0 || inventoryIndex >= armors.size()) {
            return false;
        }
        Armor chosen = armors.get(inventoryIndex);
        if (!canEquipItem(chosen)) {
            return false;
        }
        swapArmor(chosen);
        return true;
    }

    private void swapArmor(Armor chosen) {
        if (equippedArmor != null && equippedArmor.isUsable() && !inventory.getAllItems().contains(equippedArmor)) {
            // Return previously equipped armor to the bag exactly once
            inventory.addItem(equippedArmor);
        }
        equippedArmor = chosen;
        inventory.removeItem(chosen);
    }

    private boolean canEquipItem(Item item) {
        if (!meetsLevel(item)) {
            System.out.println("Your level is too low to equip this item.");
            return false;
        }
        if (!item.isUsable()) {
            System.out.println("This item has no durability left.");
            return false;
        }
        return true;
    }

    public boolean canAfford(Item item) {
        return gold >= item.getPrice();
    }

    public boolean meetsLevel(Item item) {
        return level >= item.getLevelRequirement();
    }

    public void purchase(Item item) {
        gold -= item.getPrice();
        if (item instanceof Spell) {
            addSpell((Spell) item);
        } else {
            inventory.addItem(item);
        }
    }

    public void sell(Item item) {
        gold += item.getPrice() / 2;
        inventory.removeItem(item);
        if (item instanceof Spell) {
            spells.remove(item);
        }
    }

    public void restoreMana(double amount) {
        if (amount <= 0) {
            return;
        }
        mana = Math.min(maxMana, mana + amount);
    }

    public void useMana(double amount) {
        mana = Math.max(0, mana - amount);
    }

    public void increaseStrength(double amount) {
        strength += amount;
    }

    public void increaseDexterity(double amount) {
        dexterity += amount;
    }

    public void increaseAgility(double amount) {
        agility += amount;
    }

    /**
     * Use a potion by 0-based index in the hero's potion list.
     * Returns true if a potion was consumed.
     */
    public boolean usePotion(int inventoryIndex) {
        List<Potion> potions = inventory.getPotions();
        if (inventoryIndex < 0 || inventoryIndex >= potions.size()) {
            return false;
        }
        Potion potion = potions.get(inventoryIndex);
        potion.use(this);
        inventory.removeItem(potion);
        return true;
    }

    public double applyHealthPotion(int amount) {
        if (amount <= 0) {
            return 0;
        }
        double before = hp;
        if (inBattle && hp >= battleMaxHp) {
            // Over-heal within a battle temporarily raises the cap for that fight only
            setBattleMaxHp(battleMaxHp + amount);
            hp += amount;
        } else {
            hp = Math.min(hp + amount, battleMaxHp);
        }
        if (hp > 0) {
            setFainted(false);
        }
        return hp - before;
    }

    public double attackDamage() {
        double weaponDamage = equippedWeapon != null ? equippedWeapon.getDamage() : 0;
        // BALANCING FIX: blend strength/weapon so early heroes land 15-60 raw damage.
        double rawDamage = (strength * 0.3) + (weaponDamage * 0.7);
        return Math.max(5, rawDamage);
    }

    @Override
    public AttackResult attack(Creature target, Random random) {
        if (target == null) {
            return AttackResult.dodged();
        }
        if (random.nextDouble() < target.getDodgeChance()) {
            return AttackResult.dodged();
        }
        double damage = attackDamage();
        boolean critical = random.nextDouble() < HERO_CRIT_CHANCE;
        if (critical) {
            damage *= 2;
        }
        double targetDefense = target instanceof Monster ? ((Monster) target).getDefense() : 0;
        double mitigated = applyDamageReduction(damage, targetDefense);
        double applied = target.takeDamage(mitigated);
        tickWeaponDurability();
        return AttackResult.hit(applied, critical);
    }

    public double spellDamage(Spell spell) {
        return spell.getBaseDamage() + (dexterity / 10000.0) * spell.getBaseDamage();
    }

    @Override
    public double getDodgeChance() {
        // Keep dodge chance meaningful but below guaranteed avoidance
        return Math.min(0.35, agility * 0.0005);
    }

    public double dodgeChance() {
        return getDodgeChance();
    }

    public void gainRewards(int exp, int goldEarned) {
        experience += exp;
        gold += goldEarned;
        checkLevelUp();
    }

    public void reviveHalf() {
        if (isFainted()) {
            hp = Math.min(baseMaxHp, baseMaxHp * 0.5);
            setBattleMaxHp(baseMaxHp);
            mana = Math.min(maxMana, maxMana * 0.5);
            setFainted(false);
        }
    }

    // FEATURE FROM PRIYANSHU: HP/MP regeneration each round.
    public void regenerateAfterRound() {
        // BALANCING FIX: heroes no longer auto-heal or restore mana mid-battle
        // so that damage taken by monsters remains meaningful.
    }

    private void checkLevelUp() {
        int needed = level * 10;
        while (experience >= needed) {
            experience -= needed;
            level++;
            double hpBonus = 100;
            baseMaxHp += hpBonus;
            setBattleMaxHp(baseMaxHp);
            hp = battleMaxHp;
            // BALANCE FIX: reduce mana and stat growth to keep level-ups reasonable.
            mana *= 1.04;
            setMaxMana(Math.max(maxMana, mana));
            strength *= 1.03;
            dexterity *= 1.03;
            agility *= 1.03;
            // FEATURE FROM PRIYANSHU + BALANCING FIX: Favored stats still get extra boosts but at 1.05.
            classPolicy.applyLevelUpBoosts(this);
            System.out.printf("%s leveled up to level %d! Stats increased.%n", name, level);
            needed = level * 10;
        }
    }

    public String fullInfo() {
        String weaponName = equippedWeapon != null ? equippedWeapon.getName() : "None";
        String armorName = equippedArmor != null ? equippedArmor.getName() : "None";
        return String.format("%s (Lvl %d) HP: %.0f MP: %.0f STR: %.0f DEX: %.0f AGI: %.0f Gold: %d XP: %d Weapon: %s Armor: %s", name, level, hp, mana, strength, dexterity, agility, gold, experience, weaponName, armorName);
    }

    @Override
    public String shortStatus() {
        String weaponName = equippedWeapon != null ? equippedWeapon.getName() : "None";
        String armorName = equippedArmor != null ? equippedArmor.getName() : "None";
        int currentHp = (int) Math.round(hp);
        int maxHpValue = (int) Math.round(getMaxHp());
        String hpInfo = String.format("%sHP:%s %d/%d  %s",
                ColorUtil.RED, ColorUtil.RESET, currentHp, maxHpValue,
                ColorUtil.hpBar(currentHp, maxHpValue));
        return String.format("%s (Lvl %d) %s MP: %.0f Weapon: %s Armor: %s",
                coloredName(), level, hpInfo, mana, weaponName, armorName);
    }

    public String favoredStats() {
        return classPolicy.favoredStatsLabel();
    }

    void applyStatMultipliers(double strengthFactor, double dexterityFactor, double agilityFactor) {
        strength *= strengthFactor;
        dexterity *= dexterityFactor;
        agility *= agilityFactor;
    }

    public void prepareForBattle() {
        inBattle = true;
        setBattleMaxHp(baseMaxHp);
        if (hp > baseMaxHp) {
            hp = baseMaxHp;
        }
    }

    public void resetAfterBattleCaps() {
        inBattle = false;
        if (hp > baseMaxHp) {
            hp = baseMaxHp;
        }
        setBattleMaxHp(baseMaxHp);
    }

    // FEATURE FROM LEO: Durability system for weapons.
    public void tickWeaponDurability() {
        if (equippedWeapon == null) {
            return;
        }
        equippedWeapon.consumeUse();
        if (!equippedWeapon.isUsable()) {
            System.out.printf("%s's %s broke!%n", name, equippedWeapon.getName());
            equippedWeapon = null;
        }
    }

    // FEATURE FROM LEO: Durability system for armor.
    public void tickArmorDurability() {
        if (equippedArmor == null) {
            return;
        }
        equippedArmor.consumeUse();
        if (!equippedArmor.isUsable()) {
            System.out.printf("%s's %s crumbled away!%n", name, equippedArmor.getName());
            equippedArmor = null;
        }
    }

    private static double normalizeAttribute(double value) {
        // BALANCE FIX: normalized hero stats to avoid STR/DEX/AGI in the 800+ range at level 2.
        double scaled = value / 8.0;
        return Math.min(100, Math.max(40, scaled));
    }

    private static double normalizeMana(double value) {
        // BALANCE FIX: clamp MP to a reasonable 100-250 range for level 1 heroes.
        double scaled = value / 4.0;
        return Math.min(250, Math.max(100, scaled));
    }

    private String coloredName() {
        String color;
        switch (heroClass) {
            case WARRIOR:
                color = ColorUtil.RED;
                break;
            case SORCERER:
                color = ColorUtil.BLUE;
                break;
            case PALADIN:
                color = ColorUtil.CYAN;
                break;
            default:
                color = ColorUtil.WHITE;
        }
        return color + name + ColorUtil.RESET;
    }
}
