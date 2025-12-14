package mh.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import mh.world.MapRenderer;
import mh.world.TileType;
import mh.world.TileView;
import mh.world.tile.CommonSpace;
import mh.world.tile.InaccessibleSpace;
import mh.world.tile.MarketSpace;
import mh.world.tile.Space;

public class WorldMap implements TileView {
    private final Space[][] tiles;
    private final int size;
    private Position partyPosition;
    private final Random random = new Random();

    public WorldMap(int size) {
        this.size = size;
        this.tiles = new Space[size][size];
        this.partyPosition = new Position(0, 0);
        initializeMap();
    }

    private void initializeMap() {
        generate();
        ensureStartHasExit();
        partyPosition = new Position(0, 0);
    }

    private void generate() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                tiles[r][c] = null;
            }
        }

        int totalCells = size * size;
        int inaccessibleCount = (int) Math.floor(totalCells * 0.2);
        int marketCount = (int) Math.floor(totalCells * 0.3);
        int commonCount = totalCells - inaccessibleCount - marketCount;

        // FEATURE FROM PRIYANSHU: World distribution (20% inaccessible, 30% market, 50% common).
        tiles[0][0] = new CommonSpace();
        if (commonCount > 0) {
            commonCount--;
        }

        List<Position> coordinates = new ArrayList<>(totalCells);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                coordinates.add(new Position(r, c));
            }
        }
        Collections.shuffle(coordinates, random);

        for (Position pos : coordinates) {
            if (tiles[pos.getRow()][pos.getCol()] != null) {
                continue;
            }
            if (inaccessibleCount > 0) {
                tiles[pos.getRow()][pos.getCol()] = new InaccessibleSpace();
                inaccessibleCount--;
            } else if (marketCount > 0) {
                tiles[pos.getRow()][pos.getCol()] = new MarketSpace();
                marketCount--;
            } else if (commonCount > 0) {
                tiles[pos.getRow()][pos.getCol()] = new CommonSpace();
                commonCount--;
            } else {
                tiles[pos.getRow()][pos.getCol()] = new CommonSpace();
            }
        }
    }

    private void ensureStartHasExit() {
        int attempts = 0;
        while (!hasTraversableNeighbor(0, 0) && attempts < 3) {
            generate();
            attempts++;
        }
        if (!hasTraversableNeighbor(0, 0)) {
            if (size > 1) {
                tiles[0][1] = new CommonSpace();
            } else if (size > 0) {
                tiles[0][0] = new CommonSpace();
            }
        }
        tiles[0][0] = new CommonSpace();
    }

    private boolean hasTraversableNeighbor(int r, int c) {
        int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nc >= 0 && nr < size && nc < size) {
                Space tile = tiles[nr][nc];
                if (tile != null && tile.canEnter()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Space getCurrentTile() {
        return tiles[partyPosition.getRow()][partyPosition.getCol()];
    }

    public Position getPartyPosition() {
        return partyPosition;
    }

    public boolean move(char direction) {
        int newRow = partyPosition.getRow();
        int newCol = partyPosition.getCol();
        switch (Character.toUpperCase(direction)) {
            case 'W': newRow--; break;
            case 'S': newRow++; break;
            case 'A': newCol--; break;
            case 'D': newCol++; break;
            default: return false;
        }
        if (newRow < 0 || newCol < 0 || newRow >= size || newCol >= size) {
            return false;
        }
        Space candidate = tiles[newRow][newCol];
        if (candidate == null || !candidate.canEnter()) {
            return false;
        }
        partyPosition = new Position(newRow, newCol);
        return true;
    }

    public String display() {
        return MapRenderer.render(this, pos -> partyPosition.equals(pos) ? "H" : null);
    }

    public Space tileAt(Position position) {
        validatePosition(position);
        return tiles[position.getRow()][position.getCol()];
    }

    public boolean isAccessible(Position position) {
        return tileAt(position).canEnter();
    }

    private void validatePosition(Position position) {
        if (position.getRow() < 0 || position.getRow() >= size
                || position.getCol() < 0 || position.getCol() >= size) {
            throw new IllegalArgumentException("Position out of bounds for map of size " + size);
        }
    }

    @Override
    public int rows() {
        return size;
    }

    @Override
    public int cols() {
        return size;
    }

    @Override
    public TileType tileTypeAt(Position position) {
        Space tile = tileAt(position);
        if (tile instanceof InaccessibleSpace) {
            return TileType.INACCESSIBLE;
        }
        if (tile instanceof MarketSpace) {
            return TileType.MARKET;
        }
        return TileType.PLAIN;
    }

    public int getSize() {
        return size;
    }
}
