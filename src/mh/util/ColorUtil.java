package mh.util;

public final class ColorUtil {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String GRAY = "\u001B[90m";

    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    private ColorUtil() {}

    public static String bold(String s) {
        return BOLD + s + RESET;
    }

    public static String underline(String s) {
        return UNDERLINE + s + RESET;
    }

    public static String padRight(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String hpBar(int hp, int maxHp) {
        int barLength = 20;
        if (maxHp <= 0) {
            maxHp = 1;
        }
        double ratio = Math.max(0, Math.min(1, (double) hp / maxHp));
        int filled = (int) Math.round(ratio * barLength);

        String color;
        if (ratio >= 0.70) {
            color = GREEN;
        } else if (ratio >= 0.30) {
            color = YELLOW;
        } else {
            color = BRIGHT_RED;
        }

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        for (int i = filled; i < barLength; i++) {
            bar.append("─");
        }
        bar.append("]");
        return color + bar + RESET;
    }

    public static String formatHP(int hp, int maxHp) {
        return BRIGHT_RED + hp + "/" + maxHp + "  " + hpBar(hp, maxHp) + RESET;
    }

    public static String formatMP(int mp) {
        return BRIGHT_BLUE + mp + RESET;
    }

    public static String gold(String s) {
        return YELLOW + s + RESET;
    }

    public static String gray(String s) {
        return GRAY + s + RESET;
    }
}
