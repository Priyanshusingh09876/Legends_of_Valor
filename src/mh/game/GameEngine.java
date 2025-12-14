package mh.game;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import mh.battle.strategy.BattleStrategy;
import mh.battle.strategy.SimpleBattleStrategy;
import mh.data.DataLoader;
import mh.data.GameData;
import mh.items.Armor;
import mh.items.Item;
import mh.items.Weapon;
import mh.market.Market;
import mh.model.Hero;
import mh.model.Monster;
import mh.util.ColorUtil;
import mh.world.Position;
import mh.world.WorldMap;
import mh.world.tile.CommonSpace;
import mh.world.tile.MarketSpace;
import mh.world.tile.Space;

public class GameEngine extends RPGGame {
    private final Scanner scanner;
    private final Random random;
    // FEATURE FROM LEONARDO: GameEngine orchestrates systems through a BattleStrategy.
    private final BattleStrategy battleStrategy;
    private GameData data;
    private Party party;
    private WorldMap worldMap;
    private int safeStepsRemaining = 2;
    private final Map<String, Market> marketCache = new HashMap<>();

    public GameEngine() {
        this(new Scanner(System.in), new Random());
    }

    public GameEngine(Scanner scanner, Random random) {
        this.scanner = scanner;
        this.random = random;
        this.battleStrategy = new SimpleBattleStrategy(scanner, random);
    }

    @Override
    protected void initGame() {
        System.out.println("Welcome to Legends: Monsters and Heroes!");
        try {
            data = new DataLoader(Paths.get(".")).load();
        } catch (IOException e) {
            System.out.println("Failed to load game data: " + e.getMessage());
            stopGame();
            return;
        }
        setupParty();
        worldMap = new WorldMap(8);
    }

    @Override
    protected void runTurn() {
        System.out.println(worldMap.display());
        System.out.println("Commands: W/A/S/D to move, M to enter market, I to view party, P for inventory, Q to quit");
        String input = scanner.nextLine().trim().toUpperCase();
        switch (input) {
            case "W":
            case "A":
            case "S":
            case "D":
                if (!worldMap.move(input.charAt(0))) {
                    System.out.println("Cannot move there.");
                } else {
                    handleTile();
                }
                break;
            case "M":
                if (onMarketTile()) {
                    enterMarket();
                } else {
                    System.out.println("You need to be on a market tile.");
                }
                break;
            case "I":
                showPartyInfo();
                break;
            case "P":
                manageInventory();
                break;
            case "Q":
                stopGame();
                break;
            default:
                System.out.println("Unknown command");
        }
        if (party != null && party.allFainted()) {
            System.out.println("All heroes have fallen. Game over.");
            stopGame();
        }
    }

    @Override
    protected boolean isGameOver() {
        return party != null && party.allFainted();
    }

    @Override
    protected void shutdown() {
        System.out.println("Thanks for playing!");
    }

    private void setupParty() {
        party = new Party();
        int count = promptInt("How many heroes will join your party? (1-3)", 1, 3);
        for (int i = 0; i < count; i++) {
            System.out.printf("Select hero %d:%n", i + 1);
            Hero hero = chooseHero();
            party.addHero(hero);
        }
        System.out.println("Your party:");
        System.out.println(party.describe());
    }

    private Hero chooseHero() {
        int selection = promptInt("Choose class: [1] Warrior [2] Sorcerer [3] Paladin", 1, 3);
        switch (selection) {
            case 1:
                return pickFromList(data.getWarriors());
            case 2:
                return pickFromList(data.getSorcerers());
            case 3:
            default:
                return pickFromList(data.getPaladins());
        }
    }

    private Hero pickFromList(List<Hero> heroes) {
        for (int i = 0; i < heroes.size(); i++) {
            Hero hero = heroes.get(i);
            System.out.printf("%d) %s - %s (Favored: %s)\n", i + 1, hero.getName(), hero.fullInfo(), hero.favoredStats());
        }
        int idx = promptInt("Pick hero", 1, heroes.size()) - 1;
        return heroes.get(idx);
    }

    private void handleTile() {
        Space tile = worldMap.getCurrentTile();
        System.out.printf("You stepped onto a %s tile.%n", tile.describe());
        if (tile instanceof CommonSpace) {
            if (safeStepsRemaining > 0) {
                safeStepsRemaining--;
                System.out.println("The area seems calm. Use this time to prepare.");
                return;
            }
            CommonSpace commonTile = (CommonSpace) tile;
            // FEATURE FROM PRIYANSHU: Common tiles have a random encounter chance.
            if (commonTile.shouldTriggerBattle(random)) {
                List<Monster> monsters = generateMonsters();
                boolean victory = battleStrategy.executeBattle(party.getHeroes(), monsters);
                if (!victory) {
                    System.out.println("All heroes need rest after the defeat.");
                }
            } else {
                System.out.println("No monsters nearby... for now.");
            }
        } else if (tile instanceof MarketSpace) {
            System.out.println("You hear nearby market chatter.");
        }
    }

    private Market getOrCreateMarket() {
        Position pos = worldMap.getPartyPosition();
        String key = pos.getRow() + ":" + pos.getCol();
        Market market = marketCache.get(key);
        if (market == null) {
            // Reuse the same stock for this tile so it stays consistent across visits
            market = new Market(data.getWeapons(), data.getArmors(), data.getPotions(), data.getSpells());
            marketCache.put(key, market);
        }
        return market;
    }

    private List<Monster> generateMonsters() {
        int count = party.getHeroes().size();
        int heroLevel = party.highestLevel();
        List<Monster> pool = new ArrayList<>();
        pool.addAll(data.getDragons());
        pool.addAll(data.getExoskeletons());
        pool.addAll(data.getSpirits());
        List<Monster> monsters = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Monster template = pool.get(random.nextInt(pool.size()));
            // FEATURE FROM PRIYANSHU: Monsters scale to match the strongest hero.
            monsters.add(template.scaledCopyForLevel(heroLevel));
        }
        return monsters;
    }

    private void enterMarket() {
        Market market = getOrCreateMarket();
        boolean shopping = true;
        while (shopping) {
            System.out.println("Select hero for market actions:");
            for (int i = 0; i < party.getHeroes().size(); i++) {
                System.out.printf("%d) %s%n", i + 1, party.getHeroes().get(i).fullInfo());
            }
            System.out.println("0) Leave market");
            int selection = promptInt("Choice", 0, party.getHeroes().size());
            if (selection == 0) {
                shopping = false;
                break;
            }
            Hero hero = party.getHeroes().get(selection - 1);
            System.out.println("Market options: [B]uy [S]ell [Q]uit");
            String action = scanner.nextLine().trim().toUpperCase();
            switch (action) {
                case "B":
                    buyFlow(market, hero);
                    break;
                case "S":
                    sellFlow(market, hero);
                    break;
                default:
                    shopping = false;
                    break;
            }
        }
    }

    private void buyFlow(Market market, Hero hero) {
        System.out.println("Items for sale:");
        System.out.print(market.listStock());
        int choice = promptInt("Select item (0 to cancel)", 0, market.getStock().size());
        if (choice == 0) return;
        boolean success = market.buy(hero, choice - 1);
        if (success) {
            System.out.println("Purchase successful!");
        } else {
            System.out.println("Cannot buy item (insufficient gold or level).");
        }
    }

    private void sellFlow(Market market, Hero hero) {
        List<Item> sellable = market.sellable(hero);
        if (sellable.isEmpty()) {
            System.out.println("No items to sell.");
            return;
        }
        for (int i = 0; i < sellable.size(); i++) {
            System.out.printf("%d) %s (Sell price: %d)\n", i + 1, sellable.get(i).getDescription(), sellable.get(i).getPrice() / 2);
        }
        int choice = promptInt("Select item to sell (0 to cancel)", 0, sellable.size());
        if (choice == 0) return;
        Item item = sellable.get(choice - 1);
        market.sell(hero, item);
        System.out.println("Item sold.");
    }

    private void manageInventory() {
        System.out.println("Which hero?");
        for (int i = 0; i < party.getHeroes().size(); i++) {
            System.out.printf("%d) %s%n", i + 1, party.getHeroes().get(i).fullInfo());
        }
        int idx = promptInt("Choice", 1, party.getHeroes().size()) - 1;
        Hero hero = party.getHeroes().get(idx);
        System.out.println("Inventory options: [E]quip, [P]otion info, [S]pells");
        String action = scanner.nextLine().trim().toUpperCase();
        switch (action) {
            case "E":
                equip(hero);
                break;
            case "P":
                hero.getInventory().getPotions().forEach(p -> System.out.println(p.getDescription()));
                break;
            case "S":
                hero.getInventory().getSpells().forEach(s -> System.out.println(s.getDescription()));
                break;
            default:
                break;
        }
    }

    private void equip(Hero hero) {
        System.out.println("Equip [W]eapon or [A]rmor?");
        String input = readEquipChoice();
        if (input.equals("W")) {
            List<Weapon> weapons = hero.getInventory().getWeapons();
            if (weapons.isEmpty()) {
                System.out.println("No weapons available.");
                return;
            }
            for (int i = 0; i < weapons.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, weapons.get(i).getDescription());
            }
            int choice = promptInt("Select weapon", 1, weapons.size());
            if (hero.equipWeapon(choice - 1)) {
                System.out.printf("%s equipped %s.%n", hero.getName(), weapons.get(choice - 1).getName());
            }
        } else {
            List<Armor> armors = hero.getInventory().getArmors();
            if (armors.isEmpty()) {
                System.out.println("No armor available.");
                return;
            }
            for (int i = 0; i < armors.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, armors.get(i).getDescription());
            }
            int choice = promptInt("Select armor", 1, armors.size());
            if (hero.equipArmor(choice - 1)) {
                System.out.printf("%s equipped %s.%n", hero.getName(), armors.get(choice - 1).getName());
            }
        }
    }

    private String readEquipChoice() {
        // Prevent equipping flow from taking unexpected keys
        while (true) {
            String raw = scanner.nextLine().trim().toUpperCase();
            if (raw.startsWith("W")) {
                return "W";
            }
            if (raw.startsWith("A")) {
                return "A";
            }
            System.out.println("Invalid choice, please try again.");
        }
    }

    private int promptInt(String prompt, int min, int max) {
        int value = min - 1;
        while (value < min || value > max) {
            try {
                System.out.printf("%s: ", prompt);
                value = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                value = min - 1;
            }
            if (value < min || value > max) {
                System.out.println("Invalid choice, please try again.");
            }
        }
        return value;
    }

    private boolean onMarketTile() {
        return worldMap.getCurrentTile() instanceof MarketSpace;
    }

    private void showPartyInfo() {
        final int nameWidth = 22;
        final int hpWidth = 32;
        final int mpWidth = 10;

        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_WHITE + "--- Party Status ---" + ColorUtil.RESET);
        for (Hero hero : party.getHeroes()) {
            String name = ColorUtil.padRight(colorHeroName(hero), nameWidth);
            System.out.printf("H: %s (Lvl %d)%n", name, hero.getLevel());

            int currentHp = (int) Math.round(hero.getHp());
            int maxHp = (int) Math.round(hero.getMaxHp());
            String hpLine = ColorUtil.padRight("HP: " + ColorUtil.formatHP(currentHp, maxHp), hpWidth);
            System.out.println("   " + hpLine);

            String weapon = hero.getEquippedWeapon() != null ? hero.getEquippedWeapon().getName() : "None";
            String armor = hero.getEquippedArmor() != null ? hero.getEquippedArmor().getName() : "None";
            String mpLine = ColorUtil.padRight("MP: " + ColorUtil.formatMP((int) Math.round(hero.getMana())), mpWidth);
            System.out.println("   " + mpLine + " Weapon: " + weapon + "   Armor: " + armor);

            String stats = String.format("   STR: %.0f  DEX: %.0f  AGI: %.0f",
                    hero.getStrength(), hero.getDexterity(), hero.getAgility());
            System.out.println(stats + "\n");
        }
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_WHITE + "--------------------" + ColorUtil.RESET);
    }

    private String colorHeroName(Hero hero) {
        String favored = hero.favoredStats();
        if ("Strength & Agility".equals(favored)) {
            return ColorUtil.RED + hero.getName() + ColorUtil.RESET;
        } else if ("Dexterity & Agility".equals(favored)) {
            return ColorUtil.BLUE + hero.getName() + ColorUtil.RESET;
        } else if ("Strength & Dexterity".equals(favored)) {
            return ColorUtil.CYAN + hero.getName() + ColorUtil.RESET;
        }
        return ColorUtil.WHITE + hero.getName() + ColorUtil.RESET;
    }
}
