package mh.items;

public class Armor extends Item {
    private static final int DEFAULT_DURABILITY = 120;

    private final int damageReduction;

    public Armor(String name, int price, int levelRequirement, int damageReduction) {
        // FEATURE FROM PRIYANSHU: Armor durability allows gear to break over time.
        super(name, price, levelRequirement, DEFAULT_DURABILITY);
        this.damageReduction = damageReduction;
    }

    public int getDamageReduction() {
        return damageReduction;
    }

    @Override
    public String getDescription() {
        return String.format("%s (Reduction: %d, Durability: %.0f%%, Level: %d, Price: %d)",
                getName(), damageReduction, durabilityPercent(), getLevelRequirement(), getPrice());
    }
}
