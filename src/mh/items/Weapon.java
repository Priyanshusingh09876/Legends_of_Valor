package mh.items;

public class Weapon extends Item {
    private static final int DEFAULT_DURABILITY = 100;

    private final int damage;
    private final int handsRequired;

    public Weapon(String name, int price, int levelRequirement, int damage, int handsRequired) {
        // FEATURE FROM PRIYANSHU: Weapons include durability tracking.
        super(name, price, levelRequirement, DEFAULT_DURABILITY);
        // BALANCING FIX: normalize extreme weapon values so heroes cannot deal 200+ damage.
        this.damage = Math.min(40, Math.max(5, damage));
        this.handsRequired = handsRequired;
    }

    public int getDamage() {
        return damage;
    }

    public int getHandsRequired() {
        return handsRequired;
    }

    @Override
    public String getDescription() {
        return String.format("%s (DMG: %d, Durability: %.0f%%, Hands: %d, Level: %d, Price: %d)",
                getName(), damage, durabilityPercent(), handsRequired, getLevelRequirement(), getPrice());
    }
}
