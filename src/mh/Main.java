package mh;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import mh.game.HeroesAndMonstersGame;
import mh.world.MapRenderer;
import mh.world.TileView;
import mh.world.ValorWorld;
import mh.game.LegendsOfValorGame;  // ← ADD THIS LINE

/**
 * Entry point that lets the user choose between the legacy
 * Legends: Monsters and Heroes mode and the Legends of Valor mode.
 * Only handles prompting and delegation; no gameplay logic lives here.
 * TODO: Valor gameplay entry point is intentionally missing. When a ValorGame/ValorController
 *       exists, launch it here instead of just previewing the map. Keep gameplay logic out
 *       of the launcher.
 */
public final class Main {

    private static final Map<String, Runnable> ACTIONS = createActions();
    private static final Map<String, String> LABELS = createLabels();

    private Main() {}

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Legends Launcher ===");
        printMenu();

        String choice = readChoice(scanner);
        ACTIONS.get(choice).run();
    }

    private static void launchLegacy() {
        System.out.println("Launching Legends: Monsters and Heroes...");
        new HeroesAndMonstersGame().start();
    }

    private static void launchValorPreview() {
        System.out.println("Starting Legends of Valor...");
        new LegendsOfValorGame().start();
    }

    private static Map<String, Runnable> createActions() {
        Map<String, Runnable> actions = new LinkedHashMap<>();
        actions.put("1", Main::launchLegacy);
        actions.put("2", Main::launchValorPreview);
        return actions;
    }

    private static Map<String, String> createLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("1", "Legends: Monsters and Heroes");
        labels.put("2", "Legends of Valor");
        return labels;
    }

    private static void printMenu() {
        LABELS.forEach((key, label) -> System.out.printf("%s) %s%n", key, label));
        System.out.print("Choose game mode: ");
    }

    private static String readChoice(Scanner scanner) {
        String choice = scanner.nextLine().trim();
        while (!ACTIONS.containsKey(choice)) {
            System.out.print("Invalid selection. Please choose again: ");
            choice = scanner.nextLine().trim();
        }
        return choice;
    }
}
