package mh.data;

import java.util.ArrayList;
import java.util.List;

import mh.items.Armor;
import mh.items.Potion;
import mh.items.Spell;
import mh.items.Weapon;
import mh.model.Hero;
import mh.model.Monster;

public class GameData {
    private final List<Hero> warriors = new ArrayList<>();
    private final List<Hero> sorcerers = new ArrayList<>();
    private final List<Hero> paladins = new ArrayList<>();
    private final List<Monster> dragons = new ArrayList<>();
    private final List<Monster> exoskeletons = new ArrayList<>();
    private final List<Monster> spirits = new ArrayList<>();
    private final List<Weapon> weapons = new ArrayList<>();
    private final List<Armor> armors = new ArrayList<>();
    private final List<Potion> potions = new ArrayList<>();
    private final List<Spell> spells = new ArrayList<>();

    // Simple in-memory DTO for all parsed game records; lists are mutable for loader convenience.
    public List<Hero> getWarriors() { return warriors; }
    public List<Hero> getSorcerers() { return sorcerers; }
    public List<Hero> getPaladins() { return paladins; }
    public List<Monster> getDragons() { return dragons; }
    public List<Monster> getExoskeletons() { return exoskeletons; }
    public List<Monster> getSpirits() { return spirits; }
    public List<Weapon> getWeapons() { return weapons; }
    public List<Armor> getArmors() { return armors; }
    public List<Potion> getPotions() { return potions; }
    public List<Spell> getSpells() { return spells; }

    public void addWarrior(Hero hero) { warriors.add(hero); }
    public void addSorcerer(Hero hero) { sorcerers.add(hero); }
    public void addPaladin(Hero hero) { paladins.add(hero); }
    public void addDragon(Monster monster) { dragons.add(monster); }
    public void addExoskeleton(Monster monster) { exoskeletons.add(monster); }
    public void addSpirit(Monster monster) { spirits.add(monster); }
    public void addWeapon(Weapon weapon) { weapons.add(weapon); }
    public void addArmor(Armor armor) { armors.add(armor); }
    public void addPotion(Potion potion) { potions.add(potion); }
    public void addSpell(Spell spell) { spells.add(spell); }
}
