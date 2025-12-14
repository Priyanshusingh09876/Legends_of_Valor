package mh.items;

public abstract class Item {
    private final String name;
    private final int price;
    private final int levelRequirement;
    private final int maxDurability;
    private int usesRemaining;

    // FEATURE FROM PRIYANSHU: Base durability tracking for every item.
    protected Item(String name, int price, int levelRequirement, int usesRemaining) {
        this.name = name;
        this.price = price;
        this.levelRequirement = levelRequirement;
        this.usesRemaining = usesRemaining;
        this.maxDurability = usesRemaining == Integer.MAX_VALUE ? Integer.MAX_VALUE : usesRemaining;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public boolean isUsable() {
        return usesRemaining != 0;
    }

    public int getUsesRemaining() {
        return usesRemaining;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public double durabilityPercent() {
        if (maxDurability == Integer.MAX_VALUE) {
            return 100;
        }
        if (maxDurability == 0) {
            return 0;
        }
        return (usesRemaining / (double) maxDurability) * 100.0;
    }

    public void consumeUse() {
        if (usesRemaining == Integer.MAX_VALUE) {
            return;
        }
        if (usesRemaining > 0) {
            usesRemaining--;
        }
    }

    public abstract String getDescription();
}
