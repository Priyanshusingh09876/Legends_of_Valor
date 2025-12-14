package mh.world;

import mh.model.Hero;
import mh.model.Monster;

/**
 * Movement and interaction rules specific to Legends of Valor.
 * Terrain queries stay in {@link TileView}; rules live here.
 */
public interface ValorRules {
    boolean canMove(Hero hero, Position from, Position to);

    boolean canMove(Monster monster, Position from, Position to);

    boolean move(Hero hero, Position to);

    boolean move(Monster monster, Position to);

    boolean canTeleport(Hero hero, Hero target, Position dest);

    Position recallDestination(Hero hero);

    boolean removeObstacle(Hero hero, Position target);
}
