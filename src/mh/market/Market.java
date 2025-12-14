package mh.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import mh.items.Armor;
import mh.items.Item;
import mh.items.Potion;
import mh.items.Spell;
import mh.items.Weapon;
import mh.model.Hero;

public class Market {
    private final List<Item> stock = new ArrayList<>();
    private final Random random = new Random();

    public Market(List<Weapon> weapons, List<Armor> armors, List<Potion> potions, List<Spell> spells) {
        pickRandom(weapons, 6);
        pickRandom(armors, 4);
        pickRandom(potions, 5);
        pickRandom(spells, 5);
    }

    private void pickRandom(List<? extends Item> list, int amount) {
        List<? extends Item> copy = new ArrayList<>(list);
        for (int i = 0; i < amount && !copy.isEmpty(); i++) {
            int idx = random.nextInt(copy.size());
            stock.add(copy.remove(idx));
        }
    }

    public List<Item> getStock() {
        return stock;
    }

    public String listStock() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stock.size(); i++) {
            sb.append(String.format("%d) %s\n", i + 1, stock.get(i).getDescription()));
        }
        return sb.toString();
    }

    public boolean buy(Hero hero, int index) {
        if (index < 0 || index >= stock.size()) return false;
        Item item = stock.get(index);
        // FEATURE FROM PRIYANSHU: Market buy rules enforce hero level and affordability.
        if (!hero.meetsLevel(item)) {
            System.out.println("Your level is too low to buy this item.");
            return false;
        }
        if (!hero.canAfford(item)) {
            System.out.println("Not enough gold.");
            return false;
        }
        hero.purchase(item);
        stock.remove(index);
        return true;
    }

    public List<Item> sellable(Hero hero) {
        return hero.getInventory().getAllItems();
    }

    public boolean sell(Hero hero, Item item) {
        if (!sellable(hero).contains(item)) return false;
        // FEATURE FROM PRIYANSHU: Selling returns the item to the market at half price.
        hero.sell(item);
        stock.add(item);
        return true;
    }

    public String describeSellable(Hero hero) {
        return sellable(hero).stream()
                .map(Item::getDescription)
                .collect(Collectors.joining("\n"));
    }
}
