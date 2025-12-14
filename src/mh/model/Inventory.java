package mh.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import mh.items.Armor;
import mh.items.Item;
import mh.items.Potion;
import mh.items.Spell;
import mh.items.Weapon;

public class Inventory {
    private final List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    public List<Weapon> getWeapons() {
        return items.stream().filter(i -> i instanceof Weapon).map(i -> (Weapon) i).collect(Collectors.toList());
    }

    public List<Armor> getArmors() {
        return items.stream().filter(i -> i instanceof Armor).map(i -> (Armor) i).collect(Collectors.toList());
    }

    public List<Potion> getPotions() {
        return items.stream().filter(i -> i instanceof Potion).map(i -> (Potion) i).collect(Collectors.toList());
    }

    public List<Spell> getSpells() {
        return items.stream().filter(i -> i instanceof Spell).map(i -> (Spell) i).collect(Collectors.toList());
    }

    public Optional<Item> findByName(String name) {
        return items.stream().filter(i -> i.getName().equalsIgnoreCase(name)).findFirst();
    }
}
