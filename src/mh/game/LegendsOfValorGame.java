package mh.game;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import mh.data.DataLoader;
import mh.data.GameData;
import mh.model.Hero;
import mh.model.Monster;
import mh.world.Position;
import mh.world.ValorWorld;

public class LegendsOfValorGame extends RPGGame {
    private final Scanner scanner;
    private final Random random;
    private ValorWorld world;
    private List<Hero> heroes;
    private List<Monster> monsters;
    private GameData gameData;
    private int roundNumber;
    private int difficulty;

    public LegendsOfValorGame() {
        this(new Scanner(System.in), new Random());
    }

    public LegendsOfValorGame(Scanner scanner, Random random) {
        this.scanner = scanner;
        this.random = random;
        this.heroes = new ArrayList<>();
        this.monsters = new ArrayList<>();
        this.roundNumber = 0;
    }

    @Override
    protected void initGame() {
        System.out.println("=== Legends of Valor ===");

        // Load game data
        try {
            gameData = new DataLoader(Paths.get(".")).load();
        } catch (IOException e) {
            System.out.println("Failed to load data: " + e.getMessage());
            stopGame();
            return;
        }

        // Select difficulty
        difficulty = selectDifficulty();

        // Create world
        world = new ValorWorld();

        // Select 3 heroes
        selectHeroes();

        // Place heroes in their lanes
        placeHeroesInLanes();

        // Spawn initial monsters
        spawnMonsters();

        System.out.println("\nGame started! Reach the monsters' Nexus (row 0) to win!");
        System.out.println("Prevent monsters from reaching your Nexus (row 7)!\n");
    }

    @Override
    protected void runTurn() {
        roundNumber++;
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ROUND " + roundNumber);
        System.out.println("=".repeat(50));

        // Display map
        world.printMap();

        // Heroes' turn (YOUR IMPLEMENTATION HERE)
        heroesTurn();

        // Check win condition
        if (checkHeroesWin()) {
            System.out.println("\n🎉 VICTORY! A hero reached the monsters' Nexus!");
            stopGame();
            return;
        }

        // Monsters' turn (teammate will implement)
        monstersTurn();

        // Check lose condition
        if (checkMonstersWin()) {
            System.out.println("\n💀 DEFEAT! A monster reached your Nexus!");
            stopGame();
            return;
        }

        // Spawn new monsters if needed
        if (roundNumber % difficulty == 0) {
            System.out.println("\n⚠️  New monsters spawning!");
            spawnMonsters();
        }

        // End of round regeneration
        endOfRoundRegeneration();
    }

    // ========================================================================
    // YOUR MOVEMENT IMPLEMENTATION
    // ========================================================================

    private void heroesTurn() {
        for (int i = 0; i < heroes.size(); i++) {
            Hero hero = heroes.get(i);

            if (hero.isFainted()) {
                System.out.println(hero.getName() + " is fainted, skipping turn.");
                continue;
            }

            boolean actionTaken = false;
            while (!actionTaken) {
                System.out.println("\n" + "-".repeat(50));
                displayHeroStatus(hero);
                displayActions();

                String input = scanner.nextLine().trim().toUpperCase();

                switch (input) {
                    case "W": case "A": case "S": case "D":
                        actionTaken = handleMove(hero, input.charAt(0));
                        break;
                    case "T":
                        actionTaken = handleTeleport(hero);
                        break;
                    case "R":
                        actionTaken = handleRecall(hero);
                        break;
                    case "Q":
                        // Teammate will implement attack
                        System.out.println("Attack not yet implemented.");
                        break;
                    case "E":
                        // Teammate will implement spell
                        System.out.println("Spell not yet implemented.");
                        break;
                    case "P":
                        // Teammate will implement potion
                        System.out.println("Potion not yet implemented.");
                        break;
                    case "I":
                        showInfo();
                        break;
                    case "M":
                        // Teammate will implement market
                        System.out.println("Market not yet implemented.");
                        break;
                    default:
                        System.out.println("Invalid action. Try again.");
                }
            }

            // Show updated map after each hero acts
            world.printMap();
        }
    }

    private void displayActions() {
        System.out.println("\n📋 Actions:");
        System.out.println("  W/A/S/D - Move (North/West/South/East)");
        System.out.println("  T - Teleport to another lane");
        System.out.println("  R - Recall to your Nexus");
        System.out.println("  Q - Attack");
        System.out.println("  E - Cast Spell");
        System.out.println("  P - Use Potion");
        System.out.println("  M - Market (at Nexus)");
        System.out.println("  I - Show Info");
        System.out.print("\nChoose action: ");
    }

    private void displayHeroStatus(Hero hero) {
        Position pos = world.getPosition(hero);
        System.out.println("\n🦸 " + hero.getName() + "'s Turn");
        System.out.println("   Position: (" + pos.getRow() + ", " + pos.getCol() + ")");
        System.out.println("   HP: " + (int)hero.getHp() + "/" + (int)hero.getMaxHp());
        System.out.println("   MP: " + (int)hero.getMana() + "/" + (int)hero.getMaxMana());
    }

    // ========================================================================
    // MOVEMENT HANDLER (YOUR PRIMARY IMPLEMENTATION)
    // ========================================================================

    private boolean handleMove(Hero hero, char direction) {
        Position current = world.getPosition(hero);
        Position target = getTargetPosition(current, direction);

        if (target == null) {
            System.out.println("❌ Cannot move there (out of bounds).");
            return false;
        }

        if (!world.canMove(hero, current, target)) {
            System.out.println("❌ Cannot move there (blocked/invalid).");
            return false;
        }

        if (world.move(hero, target)) {
            System.out.println("✅ " + hero.getName() + " moved " + directionName(direction) + ".");
            return true;
        }

        return false;
    }

    private Position getTargetPosition(Position current, char direction) {
        int row = current.getRow();
        int col = current.getCol();

        switch (direction) {
            case 'W': row--; break; // North
            case 'S': row++; break; // South
            case 'A': col--; break; // West
            case 'D': col++; break; // East
            default: return null;
        }

        try {
            return new Position(row, col);
        } catch (IllegalArgumentException e) {
            return null; // Out of bounds
        }
    }

    private String directionName(char dir) {
        switch (dir) {
            case 'W': return "North";
            case 'S': return "South";
            case 'A': return "West";
            case 'D': return "East";
            default: return "Unknown";
        }
    }

    // ========================================================================
    // TELEPORT HANDLER (YOUR PRIMARY IMPLEMENTATION)
    // ========================================================================

    private boolean handleTeleport(Hero hero) {
        // Step 1: Find heroes in other lanes
        List<Hero> otherLaneHeroes = getHeroesInOtherLanes(hero);

        if (otherLaneHeroes.isEmpty()) {
            System.out.println("❌ No heroes in other lanes to teleport to.");
            return false;
        }

        // Step 2: Let player choose target hero
        System.out.println("\n📍 Select hero to teleport near:");
        for (int i = 0; i < otherLaneHeroes.size(); i++) {
            Hero target = otherLaneHeroes.get(i);
            Position pos = world.getPosition(target);
            System.out.printf("  %d) %s at (%d, %d) - Lane %d\n",
                    i + 1, target.getName(), pos.getRow(), pos.getCol(), getLane(pos.getCol()) + 1);
        }
        System.out.println("  0) Cancel");

        int choice = promptInt("Choose hero: ", 0, otherLaneHeroes.size());
        if (choice == 0) {
            System.out.println("Teleport cancelled.");
            return false;
        }

        Hero targetHero = otherLaneHeroes.get(choice - 1);
        Position targetPos = world.getPosition(targetHero);

        // Step 3: Get valid destinations
        List<Position> validDests = getValidTeleportDestinations(hero, targetHero, targetPos);

        if (validDests.isEmpty()) {
            System.out.println("❌ No valid teleport destinations near " + targetHero.getName() + ".");
            return false;
        }

        // Step 4: Let player choose destination
        System.out.println("\n📍 Select destination:");
        for (int i = 0; i < validDests.size(); i++) {
            Position dest = validDests.get(i);
            System.out.printf("  %d) (%d, %d) - %s of %s\n",
                    i + 1, dest.getRow(), dest.getCol(),
                    getDirection(targetPos, dest), targetHero.getName());
        }

        choice = promptInt("Choose destination: ", 1, validDests.size());
        Position dest = validDests.get(choice - 1);

        // Step 5: Execute teleport
        if (world.relocateHero(hero, dest)) {
            System.out.println("✅ " + hero.getName() + " teleported to (" +
                    dest.getRow() + ", " + dest.getCol() + ")!");
            return true;
        } else {
            System.out.println("❌ Teleport failed.");
            return false;
        }
    }

    private List<Hero> getHeroesInOtherLanes(Hero hero) {
        Position heroPos = world.getPosition(hero);
        int heroLane = getLane(heroPos.getCol());

        List<Hero> result = new ArrayList<>();
        for (Hero h : heroes) {
            if (h == hero || h.isFainted()) continue;

            Position hPos = world.getPosition(h);
            int hLane = getLane(hPos.getCol());

            if (hLane != heroLane && hLane != -1) {
                result.add(h);
            }
        }
        return result;
    }

    private List<Position> getValidTeleportDestinations(Hero hero, Hero target, Position targetPos) {
        List<Position> dests = new ArrayList<>();

        for (Position neighbor : targetPos.getCardinalNeighbors()) {
            if (world.canTeleport(hero, target, neighbor)) {
                dests.add(neighbor);
            }
        }

        return dests;
    }

    private int getLane(int col) {
        if (col == 0 || col == 1) return 0;
        if (col == 3 || col == 4) return 1;
        if (col == 6 || col == 7) return 2;
        return -1;
    }

    private String getDirection(Position from, Position to) {
        if (to.getRow() < from.getRow()) return "North";
        if (to.getRow() > from.getRow()) return "South";
        if (to.getCol() < from.getCol()) return "West";
        if (to.getCol() > from.getCol()) return "East";
        return "Same";
    }

    // ========================================================================
    // RECALL HANDLER (YOUR IMPLEMENTATION)
    // ========================================================================

    private boolean handleRecall(Hero hero) {
        Position nexus = world.recallDestination(hero);

        if (nexus == null) {
            System.out.println("❌ Cannot recall (no spawn point found).");
            return false;
        }

        if (world.relocateHero(hero, nexus)) {
            System.out.println("✅ " + hero.getName() + " recalled to Nexus at (" +
                    nexus.getRow() + ", " + nexus.getCol() + ")!");
            return true;
        } else {
            System.out.println("❌ Recall failed.");
            return false;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void selectHeroes() {
        System.out.println("\n📋 Select 3 heroes:");

        List<Hero> available = new ArrayList<>();
        available.addAll(gameData.getWarriors());
        available.addAll(gameData.getSorcerers());
        available.addAll(gameData.getPaladins());

        for (int i = 0; i < 3; i++) {
            System.out.println("\n🦸 Hero " + (i + 1) + " for Lane " + (i + 1) + ":");
            for (int j = 0; j < available.size(); j++) {
                Hero h = available.get(j);
                System.out.printf("  %d) %s - %s\n", j + 1, h.getName(), h.fullInfo());
            }

            int choice = promptInt("Select hero: ", 1, available.size());
            Hero chosen = available.remove(choice - 1);
            heroes.add(chosen);
            System.out.println("✅ " + chosen.getName() + " selected!");
        }
    }

    private void placeHeroesInLanes() {
        int[] laneCols = {0, 3, 6}; // Left side of each lane
        int spawnRow = 7; // Bottom Nexus row

        for (int i = 0; i < heroes.size(); i++) {
            Position spawnPos = new Position(spawnRow, laneCols[i]);
            world.placeHero(heroes.get(i), spawnPos);
            System.out.println("  " + heroes.get(i).getName() + " spawned in Lane " + (i + 1));
        }
    }

    private void spawnMonsters() {
        int highestLevel = heroes.stream()
                .mapToInt(Hero::getLevel)
                .max()
                .orElse(1);

        int[] laneCols = {1, 4, 7}; // Right side of each lane
        int spawnRow = 0; // Top Nexus row

        List<Monster> pool = new ArrayList<>();
        pool.addAll(gameData.getDragons());
        pool.addAll(gameData.getExoskeletons());
        pool.addAll(gameData.getSpirits());

        for (int i = 0; i < laneCols.length; i++) {
            Monster template = pool.get(random.nextInt(pool.size()));
            Monster scaled = template.scaledCopyForLevel(highestLevel);

            Position spawnPos = new Position(spawnRow, laneCols[i]);
            if (world.placeMonster(scaled, spawnPos)) {
                monsters.add(scaled);
                System.out.println("  👾 " + scaled.getName() + " spawned in Lane " + (i + 1));
            }
        }
    }

    private int selectDifficulty() {
        System.out.println("\n⚙️  Select difficulty:");
        System.out.println("  1) Easy (spawn every 8 rounds)");
        System.out.println("  2) Medium (spawn every 6 rounds)");
        System.out.println("  3) Hard (spawn every 4 rounds)");

        int choice = promptInt("Choice: ", 1, 3);

        switch (choice) {
            case 1: return 8;
            case 2: return 6;
            case 3: return 4;
            default: return 8;
        }
    }

    private int promptInt(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.println("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    private void showInfo() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("GAME INFO");
        System.out.println("=".repeat(50));

        System.out.println("\n🦸 Heroes:");
        for (Hero h : heroes) {
            Position pos = world.getPosition(h);
            System.out.printf("  %s at (%d,%d) - HP: %.0f/%.0f, MP: %.0f/%.0f\n",
                    h.getName(), pos.getRow(), pos.getCol(),
                    h.getHp(), h.getMaxHp(), h.getMana(), h.getMaxMana());
        }

        System.out.println("\n👾 Monsters:");
        for (Monster m : monsters) {
            if (!m.isFainted()) {
                Position pos = world.getPosition(m);
                System.out.printf("  %s at (%d,%d) - HP: %.0f/%.0f\n",
                        m.getName(), pos.getRow(), pos.getCol(),
                        m.getHp(), m.getMaxHp());
            }
        }
    }

    private boolean checkHeroesWin() {
        for (Hero hero : heroes) {
            Position pos = world.getPosition(hero);
            if (pos != null && pos.getRow() == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMonstersWin() {
        for (Monster monster : monsters) {
            if (!monster.isFainted()) {
                Position pos = world.getPosition(monster);
                if (pos != null && pos.getRow() == 7) {
                    return true;
                }
            }
        }
        return false;
    }

    private void monstersTurn() {
        // Teammate will implement monster AI
        System.out.println("\n👾 Monsters' turn (not yet implemented)...");
    }

    private void endOfRoundRegeneration() {
        for (Hero hero : heroes) {
            if (!hero.isFainted()) {
                double hpRegen = hero.getMaxHp() * 0.1;
                double mpRegen = hero.getMaxMana() * 0.1;
                hero.heal(hpRegen);
                hero.restoreMana(mpRegen);
            }
        }
    }
}