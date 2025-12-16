/**
 * Role: Runs the Legends of Valor game mode, wiring world state, combat, menus, and turn handling.
 * Pattern(s): Template Method ;
 * Notes: Game controller
 */
package final_project.game.valor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import final_project.game.CombatResolver;
import final_project.shared.data.DataLoader;
import final_project.shared.data.GameData;
import final_project.game.RPGGame;
import final_project.shared.command.CommandRegistry;
import final_project.items.Item;
import final_project.items.Potion;
import final_project.items.Spell;
import final_project.shared.info.InfoMenu;
import final_project.shared.info.InfoProvider;
import final_project.shared.market.MarketMenu;
import final_project.shared.market.MarketCallbacks;
import final_project.shared.market.MarketAccessPolicy;
import final_project.shared.market.Market;
import final_project.shared.Move.MoveHandler;
import final_project.shared.Move.MoveAdapter;
import final_project.shared.Equip.EquipMenu;
import final_project.game.ActionResult;
import final_project.model.Hero;
import final_project.model.Monster;
import final_project.shared.UI.ColorUtil;
import final_project.shared.util.InputUtil;
import final_project.world.Position;
import final_project.world.ValorWorld;
public class LegendsOfValorGame extends RPGGame {
    private static final int LANE_COUNT = 3;
    private final Random random;

    private ValorWorld world;
    private ValorBoard board;
    private CombatResolver combatResolver;
    private MonsterController monsterController;
    private SpawnManager spawnManager;
    private Market market;
    private MarketMenu marketMenu;
    private InfoMenu infoMenu;
    private EquipMenu equipMenu;
    private CommandRegistry<Hero> heroCommands;
    private BattleUI battleUI;

    private final List<Hero> heroes = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();
    private GameData gameData;
    private int roundNumber;
    private int spawnInterval;
    private boolean needsRender;
    private boolean roundHeaderPending;
    private int currentHeroIndex;
    private boolean turnConsumed;
    private GameEndReason endReason;

    public LegendsOfValorGame() {
        this(new Scanner(System.in), new Random());
    }

    public LegendsOfValorGame(Scanner scanner, Random random) {
        super(scanner);
        this.random = random;
    }

    public void start() {
        play();
    }

    @Override
    protected void initGame() {
        gameOver = false;
        roundNumber = 0;
        needsRender = true;
        roundHeaderPending = true;
        currentHeroIndex = 0;
        System.out.println("=== Legends of Valor ===");
        try {
            gameData = new DataLoader(Paths.get(".")).load();
        } catch (IOException e) {
            System.out.println("Failed to load data: " + e.getMessage());
            gameOver = true;
            return;
        }

        spawnInterval = selectDifficulty();
        world = new ValorWorld(random);
        board = new ValorBoard(world);
        combatResolver = new CombatResolver(board, random);
        monsterController = new MonsterController(board, combatResolver);
        spawnManager = new SpawnManager(board, gameData, random);
        market = new Market(gameData.getWeapons(), gameData.getArmors(), gameData.getPotions(), gameData.getSpells());
        marketMenu = new MarketMenu(market, in, createValorMarketPolicy(), createValorMarketCallbacks());
        setupHeroCommands();
        battleUI = new BattleUI();
        infoMenu = new InfoMenu(in, createInfoProvider(), hero -> {});
        equipMenu = new EquipMenu(in);

        selectHeroes();
        placeHeroesInLanes();
        monsters.addAll(spawnManager.spawnWave(heroes));
        startNewRound();

        System.out.println("Reach the monster Nexus (top row) to win. Defend your Nexus (bottom row) to survive.");
    }

    private void startNewRound() {
        roundNumber++;
        reviveFaintedHeroes();
        roundHeaderPending = true;
        currentHeroIndex = indexOfFirstAliveHero();
        if (currentHeroIndex < 0) {
            endGame(GameEndReason.MONSTER_WIN_NEXUS);
            return;
        }
        needsRender = true;
    }

    @Override
    protected void render() {
        Hero current = currentHero();
        renderGameView(roundHeaderPending, current);
        roundHeaderPending = false;
        needsRender = false;
    }

    @Override
    protected String prompt() {
        Hero current = currentHero();
        if (current == null) {
            return "";
        }
        return "Actions: " + heroCommands.describeActions() + "\n" + current.getName() + " - choose action: ";
    }

    @Override
    protected ActionResult handleInput(String input) {
        turnConsumed = false;
        if (gameOver) {
            return ActionResult.failure(null);
        }
        Hero hero = currentHero();
        if (hero == null) {
            endGame(GameEndReason.MONSTER_WIN_NEXUS);
            return ActionResult.failure(null);
        }
        if (input == null) {
            quitGame();
            return ActionResult.failure("Input closed. Exiting game.");
        }
        String trimmed = input.trim().toUpperCase();
        if (trimmed.isEmpty()) {
            return ActionResult.failure("Please enter a command.");
        }
        if (!heroCommands.hasCommand(trimmed)) {
            return ActionResult.failure("Invalid action. Try again.");
        }
        boolean turnEnded = heroCommands.dispatch(trimmed, hero);
        turnConsumed = turnEnded;
        if (gameOver) {
            return ActionResult.failure(null);
        }
        return ActionResult.success(null);
    }

    @Override
    protected void afterInput(ActionResult r) {
        if (gameOver) {
            return;
        }
        if (turnConsumed) {
            monsters.removeIf(Monster::isFainted);
            advanceAfterHeroTurn();
        }
    }

    @Override
    protected boolean shouldRender() {
        boolean render = needsRender;
        needsRender = false;
        return render;
    }

    @Override
    protected boolean isGameOver() {
        return gameOver;
    }

    @Override
    protected void renderGameOverSummary() {
        printGameOverSummary();
    }

    private void advanceAfterHeroTurn() {
        Hero next = nextAliveHero(currentHeroIndex + 1);
        if (next != null) {
            currentHeroIndex = heroes.indexOf(next);
            needsRender = true;
            return;
        }
        monstersTurn();
        if (gameOver) {
            return;
        }
        spawnIfNeeded();
        regenerateHeroes();
        startNewRound();
    }

    private Hero currentHero() {
        if (currentHeroIndex < 0 || currentHeroIndex >= heroes.size()) {
            return null;
        }
        Hero hero = heroes.get(currentHeroIndex);
        if (hero != null && hero.isFainted()) {
            Hero next = nextAliveHero(currentHeroIndex + 1);
            if (next != null) {
                currentHeroIndex = heroes.indexOf(next);
                return next;
            }
            return null;
        }
        return hero;
    }

    private int indexOfFirstAliveHero() {
        for (int i = 0; i < heroes.size(); i++) {
            if (!heroes.get(i).isFainted()) {
                return i;
            }
        }
        return -1;
    }

    private void setupHeroCommands() {
        heroCommands = new CommandRegistry<>();
        heroCommands.register("W", "Move Up", hero -> MoveHandler.handleMove('W', createValorMoveAdapter(hero)));
        heroCommands.register("A", "Move Left", hero -> MoveHandler.handleMove('A', createValorMoveAdapter(hero)));
        heroCommands.register("S", "Move Down", hero -> MoveHandler.handleMove('S', createValorMoveAdapter(hero)));
        heroCommands.register("D", "Move Right", hero -> MoveHandler.handleMove('D', createValorMoveAdapter(hero)));
        heroCommands.register("Q", "Attack", hero -> {
            ValorAction action = chooseAttackAction(hero);
            return action != null && executeHeroAction(hero, action);
        });
        heroCommands.register("E", "Cast Spell", hero -> {
            ValorAction action = chooseSpellAction(hero);
            return action != null && executeHeroAction(hero, action);
        });
        heroCommands.register("P", "Use Potion", hero -> {
            ValorAction action = choosePotionAction(hero);
            return action != null && executeHeroAction(hero, action);
        });
        heroCommands.register("K", "Equip", this::handleEquipCommand);
        heroCommands.register("M", "Market", hero -> {
            marketMenu.open(hero);
            needsRender = true;
            return false;
        });
        heroCommands.register("T", "Teleport", hero -> {
            ValorAction action = chooseTeleportAction(hero);
            return action != null && executeHeroAction(hero, action);
        });
        heroCommands.register("R", "Recall", hero -> executeHeroAction(hero, ValorAction.recall()));
        heroCommands.register("O", "Remove Obstacle", this::handleRemoveObstacle);
        heroCommands.register("I", "Info", hero -> {
            infoMenu.open(hero);
            return false;
        });
        heroCommands.register("Z", "Quit", hero -> quitGame());
    }

    private MoveAdapter createValorMoveAdapter(final Hero hero) {
        return new MoveAdapter() {
            private Position lastDestination;

            @Override
            public String actorName() {
                return hero != null ? hero.getName() : "Hero";
            }

            @Override
            public boolean move(char wasd) {
                Position current = board.getPosition(hero);
                Position moveTo = targetFromDirection(current, wasd);
                if (moveTo == null) {
                    System.out.println("Cannot move out of bounds.");
                    return false;
                }
                lastDestination = moveTo;
                boolean moved = board.moveHero(hero, moveTo);
                if (!moved) {
                    System.out.println("Move not allowed.");
                    return false;
                }
                needsRender = true;
                return true;
            }

            @Override
            public void afterSuccessfulMove() {
                handleHeroReachingMonsterNexus(lastDestination);
            }
        };
    }

    private boolean executeHeroAction(Hero hero, ValorAction action) {
        ActionType type = action.getType();
        switch (type) {
            case MOVE:
                return handleMove(hero, action.getDestination());
            case ATTACK:
                return logResult(combatResolver.attack(hero, action.getMonsterTarget()));
            case CAST_SPELL:
                return logResult(combatResolver.castSpell(hero, action.getSpell(), action.getMonsterTarget()));
            case USE_POTION:
                return logResult(combatResolver.usePotion(hero, action.getIndex()));
            case EQUIP:
                return logResult(combatResolver.equip(hero, action.getItem()));
            case TELEPORT:
                return handleTeleport(hero, action.getHeroTarget(), action.getDestination());
            case RECALL:
                return handleRecall(hero);
            case REMOVE_OBSTACLE:
                return handleRemoveObstacle(hero, action.getDestination());
            case QUIT:
                return quitGame();
            default:
                return false;
        }
    }

    private boolean handleMove(Hero hero, Position target) {
        boolean moved = board.moveHero(hero, target);
        if (moved) {
            handleHeroReachingMonsterNexus(target);
            needsRender = true;
            return true;
        }
        System.out.println("Move not allowed.");
        return false;
    }

    private boolean handleTeleport(Hero hero, Hero targetHero, Position destination) {
        if (targetHero == null || destination == null) {
            System.out.println("Invalid teleport target.");
            return false;
        }
        if (board.teleport(hero, targetHero, destination)) {
            handleHeroReachingMonsterNexus(destination);
            needsRender = true;
            return true;
        }
        System.out.println("Teleport failed.");
        return false;
    }

    private boolean handleRecall(Hero hero) {
        if (board.recall(hero)) {
            needsRender = true;
            return true;
        }
        System.out.println("Recall failed.");
        return false;
    }

    private ValorAction chooseAttackAction(Hero hero) {
        List<Monster> inRange = combatResolver.attackableMonsters(hero, monsters);
        if (inRange.isEmpty()) {
            System.out.println("No monsters in range to attack.");
            return null;
        }
        battleUI.printTargetsWithHp(inRange, board);
        int choice = promptInt("Attack target: ", 1, inRange.size());
        return ValorAction.attack(inRange.get(choice - 1));
    }

    private ValorAction chooseSpellAction(Hero hero) {
        List<Spell> spells = new ArrayList<>(hero.getSpells());
        if (spells.isEmpty()) {
            System.out.println("No spells available.");
            return null;
        }
        for (int i = 0; i < spells.size(); i++) {
            Spell sp = spells.get(i);
            System.out.printf("%d) %s DMG %.0f Mana %.0f%n", i + 1, sp.getName(), sp.getBaseDamage(), sp.getManaCost());
        }
        int spellChoice = promptInt("Cast which spell: ", 1, spells.size());
        Spell spell = spells.get(spellChoice - 1);
        List<Monster> targets = combatResolver.attackableMonsters(hero, monsters);
        if (targets.isEmpty()) {
            System.out.println("No monsters in range to target.");
            return null;
        }
        battleUI.printTargetsWithHp(targets, board);
        int targetChoice = promptInt("Target: ", 1, targets.size());
        return ValorAction.castSpell(spell, targets.get(targetChoice - 1));
    }

    private ValorAction choosePotionAction(Hero hero) {
        List<Potion> potions = hero.getInventory().getPotions();
        if (potions.isEmpty()) {
            System.out.println("No potions in inventory.");
            return null;
        }
        for (int i = 0; i < potions.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, potions.get(i).getDescription());
        }
        int choice = promptInt("Use which potion: ", 1, potions.size());
        return ValorAction.usePotion(choice - 1);
    }


    private ValorAction chooseTeleportAction(Hero hero) {
        List<Hero> candidates = heroesInOtherLanes(hero);
        if (candidates.isEmpty()) {
            System.out.println("No heroes in other lanes to teleport near.");
            return null;
        }
        for (int i = 0; i < candidates.size(); i++) {
            Position pos = board.getPosition(candidates.get(i));
            System.out.printf("%d) %s at (%d,%d)%n", i + 1, candidates.get(i).getName(), pos.getRow(), pos.getCol());
        }
        int choice = promptInt("Teleport near which hero: ", 1, candidates.size());
        Hero target = candidates.get(choice - 1);
        Position targetPos = board.getPosition(target);
        List<Position> validDestinations = board.validTeleportDestinations(hero, target);
        if (validDestinations.isEmpty()) {
            System.out.println("No valid teleport cells near that hero.");
            return null;
        }
        System.out.println("Choose direction:");
        List<Direction> options = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            Position dest = dir.apply(targetPos);
            if (dest != null && validDestinations.contains(dest)) {
                options.add(dir);
            }
        }
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, options.get(i).getLabel());
        }
        int destChoice = promptInt("Destination: ", 1, options.size());
        Direction chosen = options.get(destChoice - 1);
        Position destination = chosen.apply(targetPos);
        if (destination == null) {
            System.out.println("Teleport failed.");
            return null;
        }
        System.out.printf("Teleported to the %s of %s at (%d,%d).%n",
                chosen.getLabel(), target.getName(), destination.getRow(), destination.getCol());
        return ValorAction.teleport(target, destination);
    }

    private boolean handleRemoveObstacle(Hero hero) {
        Position heroPos = board.getPosition(hero);
        List<Position> options = heroPos.getCardinalNeighbors();
        List<Position> obstacles = new ArrayList<>();
        for (Position pos : options) {
            if (board.isObstacle(pos)) {
                obstacles.add(pos);
            }
        }
        if (obstacles.isEmpty()) {
            System.out.println("No adjacent obstacles to remove.");
            return false;
        }
        for (int i = 0; i < obstacles.size(); i++) {
            Position pos = obstacles.get(i);
            System.out.printf("%d) Obstacle at (%d,%d)%n", i + 1, pos.getRow(), pos.getCol());
        }
        int choice = promptInt("Remove which obstacle (0 to cancel): ", 0, obstacles.size());
        if (choice == 0) {
            return false;
        }
        Position target = obstacles.get(choice - 1);
        needsRender = true;
        return handleRemoveObstacle(hero, target);
    }

    private boolean handleRemoveObstacle(Hero hero, Position target) {
        if (target == null) {
            System.out.println("Invalid target.");
            return false;
        }
        boolean removed = board.removeObstacle(hero, target);
        if (removed) {
            System.out.printf("Removed obstacle at (%d,%d).%n", target.getRow(), target.getCol());
            needsRender = true;
            return true;
        }
        System.out.println("Cannot remove obstacle there (must be adjacent obstacle).");
        return false;
    }

    private boolean handleEquipCommand(Hero hero) {
        Item item = equipMenu.chooseItemToEquip(hero);
        if (item == null) {
            return false;
        }
        ActionResult result = combatResolver.equip(hero, item);
        if (result != null && result.getMessage() != null) {
            System.out.println(result.getMessage());
        }
        needsRender = true;
        return result != null && result.isSuccess();
    }

    private List<Hero> heroesInOtherLanes(Hero hero) {
        Position heroPos = board.getPosition(hero);
        int lane = board.laneId(heroPos);
        List<Hero> others = new ArrayList<>();
        for (Hero h : heroes) {
            if (h == hero || h.isFainted()) continue;
            Position pos = board.getPosition(h);
            if (pos != null && board.laneId(pos) != lane) {
                others.add(h);
            }
        }
        return others;
    }

    private void monstersTurn() {
        List<Monster> order = new ArrayList<>(monsters);
        for (Monster monster : order) {
            if (monster.isFainted()) {
                continue;
            }
            ActionResult result = monsterController.takeTurn(monster, heroes);
            if (result != null) {
                battleUI.printMonsterTurnStatus(monster, heroes, board, result.getMessage());
            }
            Position pos = board.getPosition(monster);
            if (pos != null && board.isHeroNexus(pos)) {
                endGame(GameEndReason.MONSTER_WIN_NEXUS);
                return;
            }
        }
        needsRender = true;
    }

    private void reviveFaintedHeroes() {
        for (Hero hero : heroes) {
            if (!hero.isFainted()) {
                continue;
            }
            Position spawn = board.recallDestination(hero);
            hero.reviveHalf();
            if (spawn != null) {
                board.placeHero(hero, spawn);
            }
        }
    }

    private void regenerateHeroes() {
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                continue;
            }
            hero.heal(hero.getMaxHp() * 0.1);
            hero.restoreMana(hero.getMaxMana() * 0.1);
        }
    }

    private void spawnIfNeeded() {
        if (roundNumber % spawnInterval != 0) {
            return;
        }
        List<Monster> spawned = spawnManager.spawnWave(heroes);
        monsters.addAll(spawned);
        if (!spawned.isEmpty()) {
            System.out.println("New monsters have spawned.");
            needsRender = true;
        }
    }

    private void selectHeroes() {
        System.out.println("Select " + LANE_COUNT + " heroes (one per lane).");
        List<Hero> available = new ArrayList<>();
        available.addAll(gameData.getWarriors());
        available.addAll(gameData.getSorcerers());
        available.addAll(gameData.getPaladins());
        for (int i = 0; i < LANE_COUNT; i++) {
            for (int j = 0; j < available.size(); j++) {
                Hero h = available.get(j);
                System.out.printf("%d) %s Level %d HP %.0f MP %.0f%n", j + 1, h.getName(), h.getLevel(), h.getMaxHp(), h.getMaxMana());
            }
            int choice = promptInt("Hero for lane " + (i + 1) + ": ", 1, available.size());
            heroes.add(available.remove(choice - 1));
        }
    }

    private void placeHeroesInLanes() {
        int[] laneCols = {0, 3, 6};
        int spawnRow = 7;
        for (int i = 0; i < heroes.size(); i++) {
            Position spawnPos = new Position(spawnRow, laneCols[i]);
            board.placeHero(heroes.get(i), spawnPos);
        }
    }

    private int selectDifficulty() {
        System.out.println("1) Easy (spawn every 8 rounds)");
        System.out.println("2) Medium (spawn every 6 rounds)");
        System.out.println("3) Hard (spawn every 4 rounds)");
        int choice = promptInt("Choice: ", 1, 3);
        switch (choice) {
            case 1: return 8;
            case 2: return 6;
            case 3: return 4;
            default: return 8;
        }
    }

    private Position targetFromDirection(Position current, char dir) {
        int r = current.getRow();
        int c = current.getCol();
        switch (dir) {
            case 'W': r -= 1; break;
            case 'S': r += 1; break;
            case 'A': c -= 1; break;
            case 'D': c += 1; break;
            default: return null;
        }
        try {
            return new Position(r, c);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int promptInt(String prompt, int min, int max) {
        return InputUtil.promptInt(in, prompt, min, max, () -> {
            System.out.println("Input closed. Exiting game.");
            quitGame();
        });
    }

    private boolean quitGame() {
        endGame(GameEndReason.PLAYER_QUIT);
        return true;
    }

    private boolean logResult(ActionResult result) {
        if (result == null) {
            return false;
        }
        System.out.println(result.getMessage());
        needsRender = true;
        return result.isSuccess();
    }

    private void endGame(GameEndReason reason) {
        endReason = reason;
        gameOver = true;
    }

    private void handleHeroReachingMonsterNexus(Position destination) {
        if (destination != null && board.isMonsterNexus(destination)) {
            endGame(GameEndReason.HERO_WIN_NEXUS);
        }
    }

    private void renderGameView(boolean includeRoundHeader, Hero currentHero) {
        if (includeRoundHeader) {
            System.out.println("===== ROUND " + roundNumber + " =====");
        }
        world.printMap();
        battleUI.printMonsterSummary(monsters, board);
        battleUI.printHeroesOnBoard(heroes, currentHero, board);
    }

    private Hero nextAliveHero(int startIdx) {
        for (int i = startIdx; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            if (!h.isFainted()) {
                return h;
            }
        }
        return null;
    }

    private void printGameOverSummary() {
        if (endReason == null) {
            return;
        }
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_WHITE + "======================" + ColorUtil.RESET);
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_WHITE + "GAME OVER" + ColorUtil.RESET);
        String resultLine;
        switch (endReason) {
            case HERO_WIN_NEXUS:
                resultLine = "Result: VICTORY (Heroes reached the Monster Nexus)";
                break;
            case MONSTER_WIN_NEXUS:
                resultLine = "Result: DEFEAT (A monster reached the Hero Nexus)";
                break;
            case PLAYER_QUIT:
            default:
                resultLine = "Result: QUIT (Player chose to quit)";
                break;
        }
        System.out.println(resultLine);
        System.out.println("Rounds played: " + roundNumber);
        System.out.println("Final Heroes:");
        for (Hero hero : heroes) {
            Position pos = board.getPosition(hero);
            int lane = pos != null ? board.laneId(pos) + 1 : -1;
            System.out.printf("  H: %s Lane %d at (%d,%d)%n", hero.shortStatus(), lane,
                    pos != null ? pos.getRow() : -1, pos != null ? pos.getCol() : -1);
        }
        System.out.println("Final Monsters on board:");
        for (Monster monster : monsters) {
            if (monster.isFainted()) continue;
            Position pos = board.getPosition(monster);
            int lane = pos != null ? board.laneId(pos) + 1 : -1;
            System.out.printf("  M: %s Lane %d at (%d,%d)%n", monster.shortStatus(), lane,
                    pos != null ? pos.getRow() : -1, pos != null ? pos.getCol() : -1);
        }
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_WHITE + "======================" + ColorUtil.RESET);
    }

    private MarketAccessPolicy createValorMarketPolicy() {
        return new MarketAccessPolicy() {
            @Override
            public boolean canAccessMarket(Hero hero) {
                return board.isHeroNexus(board.getPosition(hero));
            }

            @Override
            public String denyMessage() {
                return "You must be on your Nexus to access the market.";
            }
        };
    }

    private MarketCallbacks createValorMarketCallbacks() {
        return new MarketCallbacks() {
            @Override
            public void onClose(Hero hero) {
                needsRender = true;
            }

            @Override
            public void onInputClosed(Hero hero) {
                System.out.println("Input closed. Exiting game.");
                quitGame();
            }
        };
    }

    private InfoProvider createInfoProvider() {
        return new InfoProvider() {
            @Override
            public void printHeroDetails(Hero hero) {
                battleUI.printHeroInfo(hero, board);
            }

            @Override
            public void printInventory(Hero hero) {
                battleUI.printHeroInventory(hero);
            }

            @Override
            public void printSpells(Hero hero) {
                battleUI.printHeroSpells(hero);
            }

            @Override
            public void printTeamSummary() {
                System.out.println("--- Team Summary ---");
                for (Hero h : heroes) {
                    Position pos = board.getPosition(h);
                    int lane = pos != null ? board.laneId(pos) + 1 : -1;
                    System.out.printf("H: %s Lane %d at (%d,%d) %s%n", h.shortStatus(), lane,
                            pos != null ? pos.getRow() : -1, pos != null ? pos.getCol() : -1,
                            h.isFainted() ? "[Fainted]" : "");
                }
            }

            @Override
            public void printMonstersSummary() {
                System.out.println("--- Monsters Summary ---");
                boolean any = false;
                for (Monster m : monsters) {
                    if (m.isFainted()) continue;
                    Position pos = board.getPosition(m);
                    if (pos == null) continue;
                    any = true;
                    int lane = board.laneId(pos) + 1;
                    System.out.printf("M: %s Lane %d at (%d,%d)%n", m.shortStatus(), lane, pos.getRow(), pos.getCol());
                }
                if (!any) {
                    System.out.println("No active monsters.");
                }
            }
        };
    }

}
