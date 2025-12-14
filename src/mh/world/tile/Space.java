package mh.world.tile;

import mh.model.Creature;
import mh.model.Hero;

/**
 * Base space representation for the world map.
 */
public abstract class Space {
    private final char symbol;
    private final boolean walkable;

    protected Space(char symbol, boolean walkable) {
        this.symbol = symbol;
        this.walkable = walkable;
    }

    public char symbol() {
        return symbol;
    }

    // Backward compatibility with older code.
    public char getSymbol() {
        return symbol();
    }

    public boolean isWalkableFor(Creature creature) {
        return walkable;
    }

    // Backward compatibility with older code.
    public boolean canEnter() {
        return isWalkableFor(null);
    }

    public boolean isMarket() {
        return false;
    }

    public void onEnter(Hero hero) {}

    public void onExit(Hero hero) {}

    public String describe() {
        return getClass().getSimpleName().replace("Space", "");
    }
}
