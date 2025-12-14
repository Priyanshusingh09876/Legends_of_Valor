package mh.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import mh.items.Armor;
import mh.items.Potion;
import mh.items.Potion.Attribute;
import mh.items.Spell;
import mh.items.Spell.SpellType;
import mh.items.Weapon;
import mh.model.Hero;
import mh.model.Hero.HeroClass;
import mh.model.Monster;
import mh.model.MonsterType;

public class DataLoader {
    private final Path basePath;

    public DataLoader(Path basePath) {
        this.basePath = basePath;
    }

    /**
     * Loads all hero/monster/item records from the configured text files into a {@link GameData} container.
     */
    public GameData load() throws IOException {
        GameData data = new GameData();
        loadHeroes(data, "Warriors.txt", HeroClass.WARRIOR);
        loadHeroes(data, "Sorcerers.txt", HeroClass.SORCERER);
        loadHeroes(data, "Paladins.txt", HeroClass.PALADIN);
        loadMonsters(data, "Dragons.txt", MonsterType.DRAGON);
        loadMonsters(data, "Exoskeletons.txt", MonsterType.EXOSKELETON);
        loadMonsters(data, "Spirits.txt", MonsterType.SPIRIT);
        loadWeapons(data, "Weaponry.txt");
        loadArmors(data, "Armory.txt");
        loadPotions(data, "Potions.txt");
        loadSpells(data, "FireSpells.txt", SpellType.FIRE);
        loadSpells(data, "IceSpells.txt", SpellType.ICE);
        loadSpells(data, "LightningSpells.txt", SpellType.LIGHTNING);
        return data;
    }

    /**
     * File parsing helpers below assume whitespace-separated columns and skip the first header line.
     */
    private void loadHeroes(GameData data, String fileName, HeroClass heroClass) throws IOException {
        List<String> lines = readDataLines(fileName);
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 6) continue;
            String name = parts[0];
            int mana = Integer.parseInt(parts[1]);
            int strength = Integer.parseInt(parts[2]);
            int agility = Integer.parseInt(parts[3]);
            int dexterity = Integer.parseInt(parts[4]);
            int money = Integer.parseInt(parts[5]);
            int exp = parts.length > 6 ? Integer.parseInt(parts[6]) : 0;
            Hero hero = new Hero(name, mana, strength, agility, dexterity, money, exp, heroClass);
            switch (heroClass) {
                case WARRIOR:
                    data.addWarrior(hero);
                    break;
                case SORCERER:
                    data.addSorcerer(hero);
                    break;
                case PALADIN:
                    data.addPaladin(hero);
                    break;
                default:
                    break;
            }
        }
    }

    private void loadMonsters(GameData data, String fileName, MonsterType type) throws IOException {
        List<String> lines = readDataLines(fileName);
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;
            String name = parts[0];
            int level = Integer.parseInt(parts[1]);
            double damage = Double.parseDouble(parts[2]);
            double defense = Double.parseDouble(parts[3]);
            double dodge = Double.parseDouble(parts[4]);
            Monster monster = new Monster(name, level, damage, defense, dodge, type);
            switch (type) {
                case DRAGON:
                    data.addDragon(monster);
                    break;
                case EXOSKELETON:
                    data.addExoskeleton(monster);
                    break;
                case SPIRIT:
                    data.addSpirit(monster);
                    break;
                default:
                    break;
            }
        }
    }

    private void loadWeapons(GameData data, String fileName) throws IOException {
        List<String> lines = readDataLines(fileName);
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;
            String name = parts[0];
            int cost = Integer.parseInt(parts[1]);
            int level = Integer.parseInt(parts[2]);
            int damage = Integer.parseInt(parts[3]);
            int hands = Integer.parseInt(parts[4]);
            data.addWeapon(new Weapon(name, cost, level, damage, hands));
        }
    }

    private void loadArmors(GameData data, String fileName) throws IOException {
        List<String> lines = readDataLines(fileName);
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 4) continue;
            String name = parts[0];
            int cost = Integer.parseInt(parts[1]);
            int level = Integer.parseInt(parts[2]);
            int reduction = Integer.parseInt(parts[3]);
            data.addArmor(new Armor(name, cost, level, reduction));
        }
    }

    private void loadPotions(GameData data, String fileName) throws IOException {
        List<String> lines = readDataLines(fileName);
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;
            String name = parts[0];
            int cost = Integer.parseInt(parts[1]);
            int level = Integer.parseInt(parts[2]);
            int amount = Integer.parseInt(parts[3]);
            String attrText = parts[4].toUpperCase(Locale.US);
            Attribute attribute;
            switch (attrText) {
                case "HEALTH":
                    attribute = Attribute.HP;
                    break;
                case "MANA":
                    attribute = Attribute.MP;
                    break;
                case "STRENGTH":
                    attribute = Attribute.STRENGTH;
                    break;
                case "DEXTERITY":
                    attribute = Attribute.DEXTERITY;
                    break;
                case "AGILITY":
                    attribute = Attribute.AGILITY;
                    break;
                default:
                    attribute = Attribute.HP;
            }
            data.addPotion(new Potion(name, cost, level, attribute, amount));
        }
    }

    private void loadSpells(GameData data, String fileName, SpellType type) throws IOException {
        List<String> lines = readDataLines(fileName);
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 5) continue;
            String name = parts[0];
            int cost = Integer.parseInt(parts[1]);
            int level = Integer.parseInt(parts[2]);
            int damage = Integer.parseInt(parts[3]);
            int mana = Integer.parseInt(parts[4]);
            data.addSpell(new Spell(name, cost, level, damage, mana, type));
        }
    }

    private List<String> readDataLines(String fileName) throws IOException {
        Path filePath = basePath.resolve(fileName);
        return Files.lines(filePath)
                .skip(1) // remove header
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }
}
