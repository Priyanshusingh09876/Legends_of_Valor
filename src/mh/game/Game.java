package mh.game;

/**
 * Generic game loop abstraction.
 */
public abstract class Game {
    public final void start() {
        initGame();
        while (isRunning()) {
            runTurn();
        }
        shutdown();
    }

    protected abstract void initGame();

    protected abstract boolean isRunning();

    protected abstract void runTurn();

    protected void shutdown() {
        // hook for subclasses
    }
}
