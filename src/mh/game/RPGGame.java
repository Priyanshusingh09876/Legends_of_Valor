package mh.game;

/**
 * Base class for RPG-style games with a controlled loop.
 */
public abstract class RPGGame extends Game {
    private boolean running = true;

    protected void stopGame() {
        running = false;
    }

    @Override
    protected boolean isRunning() {
        return running && !isGameOver();
    }

    protected boolean isGameOver() {
        return false;
    }
}
