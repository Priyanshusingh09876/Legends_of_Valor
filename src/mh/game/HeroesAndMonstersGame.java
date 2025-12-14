package mh.game;

import java.util.Random;
import java.util.Scanner;

/**
 * Explicit Heroes and Monsters game entry that satisfies the inheritance requirement.
 */
public class HeroesAndMonstersGame extends GameEngine {
    public HeroesAndMonstersGame() {
        super();
    }

    public HeroesAndMonstersGame(Scanner scanner, Random random) {
        super(scanner, random);
    }
}
