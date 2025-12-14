package mh.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import mh.model.Hero;
import mh.model.Monster;
import mh.world.MapRenderer;
import mh.world.TileType;
import mh.world.TileView;
import mh.world.tile.BushSpace;
import mh.world.tile.CaveSpace;
import mh.world.tile.InaccessibleSpace;
import mh.world.tile.KoulouSpace;
import mh.world.tile.NexusSpace;
import mh.world.tile.ObstacleSpace;
import mh.world.tile.PlainSpace;
import mh.world.tile.Space;

/**
 * Legends of Valor world implementation with lanes, walls, nexus rows,
 * occupancy, movement, teleport, and buff spaces.
 */
public class ValorWorld implements TileView, ValorRules {
    private static final int SIZE = 8;
    private static final int[] WALL_COLUMNS = {2, 5};

    private final Space[][] grid = new Space[SIZE][SIZE];
    private final Random random;
    private final Map<Hero, Position> heroPositions = new LinkedHashMap<>();
    private final Map<Monster, Position> monsterPositions = new LinkedHashMap<>();
    private final Map<Hero, Position> heroSpawn = new LinkedHashMap<>();

    public ValorWorld() {
        this(new Random());
    }

    public ValorWorld(long seed) {
        this(new Random(seed));
    }

    public ValorWorld(Random random) {
        this.random = Objects.requireNonNull(random, "random must not be null");
        generate();
    }

    // ---------- Generation ----------
    private void generate() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isWallColumn(c)) {
                    grid[r][c] = new InaccessibleSpace();
                } else {
                    grid[r][c] = new PlainSpace();
                }
            }
        }
        placeNexusRows();
        populateLanes();
        ensureSpecialPresence(BushSpace.class);
        ensureSpecialPresence(CaveSpace.class);
        ensureSpecialPresence(KoulouSpace.class);
        ensurePlainExists();
    }

    private void placeNexusRows() {
        for (int col = 0; col < SIZE; col++) {
            if (!isWallColumn(col)) {
                grid[0][col] = new NexusSpace(false);
                grid[SIZE - 1][col] = new NexusSpace(true);
            }
        }
    }

    private void populateLanes() {
        for (int r = 1; r < SIZE - 1; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isWallColumn(c)) {
                    continue;
                }
                grid[r][c] = randomLaneSpace();
            }
        }
    }

    private Space randomLaneSpace() {
        int pick = random.nextInt(5);
        switch (pick) {
            case 0: return new PlainSpace();
            case 1: return new BushSpace();
            case 2: return new CaveSpace();
            case 3: return new KoulouSpace();
            default: return new ObstacleSpace();
        }
    }

    private void ensureSpecialPresence(Class<? extends Space> clazz) {
        if (contains(clazz)) {
            return;
        }
        List<Position> laneCells = laneCells();
        Collections.shuffle(laneCells, random);
        for (Position cell : laneCells) {
            if (!(grid[cell.getRow()][cell.getCol()] instanceof NexusSpace)) {
                grid[cell.getRow()][cell.getCol()] = instantiate(clazz);
                return;
            }
        }
    }

    private void ensurePlainExists() {
        if (contains(PlainSpace.class)) {
            return;
        }
        List<Position> laneCells = laneCells();
        grid[laneCells.get(0).getRow()][laneCells.get(0).getCol()] = new PlainSpace();
    }

    private boolean contains(Class<? extends Space> clazz) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (clazz.isInstance(grid[r][c])) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Position> laneCells() {
        List<Position> positions = new ArrayList<>();
        for (int r = 1; r < SIZE - 1; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isWallColumn(c)) continue;
                positions.add(new Position(r, c));
            }
        }
        return positions;
    }

    private Space instantiate(Class<? extends Space> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new PlainSpace();
        }
    }

    private boolean isWallColumn(int col) {
        for (int wall : WALL_COLUMNS) {
            if (wall == col) {
                return true;
            }
        }
        return false;
    }

    // ---------- Occupancy helpers ----------
    public boolean placeHero(Hero hero, Position position) {
        if (hero == null || position == null || !grid[position.getRow()][position.getCol()].isWalkableFor(hero)) {
            return false;
        }
        if (heroPositions.containsKey(hero)) {
            return false;
        }
        if (heroAt(position) != null) {
            return false;
        }
        heroPositions.put(hero, position);
        heroSpawn.putIfAbsent(hero, position);
        return true;
    }

    public boolean placeMonster(Monster monster, Position position) {
        if (monster == null || position == null || !grid[position.getRow()][position.getCol()].isWalkableFor(monster)) {
            return false;
        }
        if (monsterPositions.containsKey(monster)) {
            return false;
        }
        if (monsterAt(position) != null) {
            return false;
        }
        monsterPositions.put(monster, position);
        return true;
    }

    private Hero heroAt(Position pos) {
        for (Map.Entry<Hero, Position> e : heroPositions.entrySet()) {
            if (e.getValue().equals(pos)) {
                return e.getKey();
            }
        }
        return null;
    }

    private Monster monsterAt(Position pos) {
        for (Map.Entry<Monster, Position> e : monsterPositions.entrySet()) {
            if (e.getValue().equals(pos)) {
                return e.getKey();
            }
        }
        return null;
    }

    public Position getPosition(Hero hero) {
        return heroPositions.get(hero);
    }

    public Position getPosition(Monster monster) {
        return monsterPositions.get(monster);
    }

    public boolean moveEntity(Object unit, Position to) {
        if (unit instanceof Hero) {
            return move((Hero) unit, to);
        }
        if (unit instanceof Monster) {
            return move((Monster) unit, to);
        }
        return false;
    }

    // ---------- Movement rules ----------
    @Override
    public boolean canMove(Hero hero, Position from, Position to) {
        return canMoveInternal(hero, from, to, true);
    }

    @Override
    public boolean canMove(Monster monster, Position from, Position to) {
        return canMoveInternal(monster, from, to, false);
    }

    private boolean canMoveInternal(Object unit, Position from, Position to, boolean isHero) {
        if (unit == null || from == null || to == null) return false;
        if (!from.equals(isHero ? heroPositions.get(unit) : monsterPositions.get(unit))) {
            return false;
        }
        if (manhattan(from, to) != 1) {
            return false;
        }
        if (!inBounds(to)) return false;
        Space dest = grid[to.getRow()][to.getCol()];
        if (!dest.isWalkableFor(null) || dest instanceof ObstacleSpace) {
            return false;
        }
        if (isWallColumn(to.getCol())) return false;
        if (isHero && heroAt(to) != null) return false;
        if (!isHero && monsterAt(to) != null) return false;
        if (isHero && blockedByFrontMonster(from, to)) return false;
        if (!isHero && blockedByFrontHero(from, to)) return false;
        return true;
    }

    @Override
    public boolean move(Hero hero, Position to) {
        Position from = heroPositions.get(hero);
        if (!canMove(hero, from, to)) return false;
        Space current = grid[from.getRow()][from.getCol()];
        Space dest = grid[to.getRow()][to.getCol()];
        current.onExit(hero);
        heroPositions.put(hero, to);
        dest.onEnter(hero);
        return true;
    }

    @Override
    public boolean move(Monster monster, Position to) {
        Position from = monsterPositions.get(monster);
        if (!canMove(monster, from, to)) return false;
        monsterPositions.put(monster, to);
        return true;
    }

    private boolean blockedByFrontMonster(Position from, Position to) {
        int lane = laneOf(from.getCol());
        if (lane == -1) return false;
        if (to.getRow() >= from.getRow()) {
            return false;
        }
        Integer nearestMonsterRow = nearestMonsterRowAhead(lane, from.getRow());
        return nearestMonsterRow != null && to.getRow() < nearestMonsterRow;
    }

    private boolean blockedByFrontHero(Position from, Position to) {
        int lane = laneOf(from.getCol());
        if (lane == -1) return false;
        if (to.getRow() <= from.getRow()) {
            return false;
        }
        Integer nearestHeroRow = nearestHeroRowAhead(lane, from.getRow());
        return nearestHeroRow != null && to.getRow() > nearestHeroRow;
    }

    private Integer nearestMonsterRowAhead(int lane, int fromRow) {
        Integer nearest = null;
        for (Map.Entry<Monster, Position> entry : monsterPositions.entrySet()) {
            Position pos = entry.getValue();
            if (laneOf(pos.getCol()) != lane) continue;
            if (pos.getRow() < fromRow) {
                if (nearest == null || pos.getRow() > nearest) {
                    nearest = pos.getRow();
                }
            }
        }
        return nearest;
    }

    private Integer nearestHeroRowAhead(int lane, int fromRow) {
        Integer nearest = null;
        for (Map.Entry<Hero, Position> entry : heroPositions.entrySet()) {
            Position pos = entry.getValue();
            if (laneOf(pos.getCol()) != lane) continue;
            if (pos.getRow() > fromRow) {
                if (nearest == null || pos.getRow() < nearest) {
                    nearest = pos.getRow();
                }
            }
        }
        return nearest;
    }

    // ---------- Teleport / recall / obstacles ----------
    @Override
    public boolean canTeleport(Hero hero, Hero target, Position dest) {
        if (hero == null || target == null || dest == null) return false;
        Position heroPos = heroPositions.get(hero);
        Position targetPos = heroPositions.get(target);
        if (heroPos == null || targetPos == null) return false;
        if (laneOf(heroPos.getCol()) == laneOf(targetPos.getCol())) {
            return false; // must cross lane
        }
        if (manhattan(targetPos, dest) != 1) return false;
        if (!inBounds(dest)) return false;
        if (dest.getRow() < targetPos.getRow()) return false; // cannot land ahead of target
        if (heroAt(dest) != null) return false;
        Space space = grid[dest.getRow()][dest.getCol()];
        if (!space.isWalkableFor(hero) || space instanceof ObstacleSpace) return false;
        if (isAheadOfFrontMonster(dest)) return false;
        return true;
    }

    private boolean isAheadOfFrontMonster(Position dest) {
        int lane = laneOf(dest.getCol());
        Integer nearest = nearestMonsterRowAhead(lane, dest.getRow());
        return nearest != null && dest.getRow() < nearest;
    }

    @Override
    public Position recallDestination(Hero hero) {
        return heroSpawn.get(hero);
    }

    @Override
    public boolean removeObstacle(Hero hero, Position target) {
        if (hero == null || target == null) return false;
        Position heroPos = heroPositions.get(hero);
        if (heroPos == null) return false;
        if (manhattan(heroPos, target) != 1) return false;
        if (!(grid[target.getRow()][target.getCol()] instanceof ObstacleSpace)) {
            return false;
        }
        grid[target.getRow()][target.getCol()] = new PlainSpace();
        return true;
    }

    public void setSpaceForTesting(Position pos, Space space) {
        grid[pos.getRow()][pos.getCol()] = space;
    }

    // ---------- Rendering ----------
    public String render() {
        return MapRenderer.render(this, this::markerAt);
    }

    public void printMap() {
        System.out.println(render());
    }

    private String markerAt(Position pos) {
        Hero h = heroAt(pos);
        Monster m = monsterAt(pos);
        StringBuilder marker = new StringBuilder();
        if (h != null) {
            marker.append("H").append(heroIndex(h));
        }
        if (m != null) {
            marker.append("M").append(monsterIndex(m));
        }
        return marker.length() == 0 ? null : marker.toString();
    }

    private int heroIndex(Hero hero) {
        int idx = 1;
        for (Hero h : heroPositions.keySet()) {
            if (h.equals(hero)) {
                return idx;
            }
            idx++;
        }
        return idx;
    }

    private int monsterIndex(Monster monster) {
        int idx = 1;
        for (Monster m : monsterPositions.keySet()) {
            if (m.equals(monster)) {
                return idx;
            }
            idx++;
        }
        return idx;
    }

    // ---------- TileView interface ----------
    @Override
    public int rows() {
        return SIZE;
    }

    @Override
    public int cols() {
        return SIZE;
    }

    @Override
    public boolean isWalkable(Position position) {
        Space space = grid[position.getRow()][position.getCol()];
        return space.isWalkableFor(null);
    }

    @Override
    public TileType tileTypeAt(Position position) {
        Space space = grid[position.getRow()][position.getCol()];
        if (space instanceof InaccessibleSpace) return TileType.INACCESSIBLE;
        if (space instanceof ObstacleSpace) return TileType.OBSTACLE;
        if (space instanceof BushSpace) return TileType.BUSH;
        if (space instanceof CaveSpace) return TileType.CAVE;
        if (space instanceof KoulouSpace) return TileType.KOULOU;
        if (space instanceof NexusSpace) return TileType.NEXUS;
        return TileType.PLAIN;
    }

    // ---------- Utility ----------
    private boolean inBounds(Position pos) {
        return pos.getRow() >= 0 && pos.getRow() < SIZE && pos.getCol() >= 0 && pos.getCol() < SIZE;
    }

    private int manhattan(Position a, Position b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }

    private int laneOf(int col) {
        if (col == 0 || col == 1) return 0;
        if (col == 3 || col == 4) return 1;
        if (col == 6 || col == 7) return 2;
        return -1;
    }

    /**
     * Remove a hero from the world (used for teleport/recall).
     */
    public void removeHero(Hero hero) {
        heroPositions.remove(hero);
    }

    /**
     * Remove a monster from the world (for death/respawn).
     */
    public void removeMonster(Monster monster) {
        monsterPositions.remove(monster);
    }

    /**
     * Relocate a hero to a new position with proper space callbacks.
     * Used for teleport and recall actions.
     */
    public boolean relocateHero(Hero hero, Position newPosition) {
        if (!heroPositions.containsKey(hero)) {
            return false;
        }

        Position oldPos = heroPositions.get(hero);
        Space oldSpace = grid[oldPos.getRow()][oldPos.getCol()];
        Space newSpace = grid[newPosition.getRow()][newPosition.getCol()];

        // Trigger space events
        oldSpace.onExit(hero);
        heroPositions.put(hero, newPosition);
        newSpace.onEnter(hero);

        return true;
    }
}
