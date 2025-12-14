package mh.world;

import java.util.function.Function;

import mh.util.ColorUtil;
import mh.world.Position;
import mh.world.TileType;
import mh.world.TileView;

/**
 * Shared renderer for tile-based boards. Map classes expose terrain via {@link TileView};
 * rendering details live here to avoid duplication.
 */
public final class MapRenderer {

    private MapRenderer() {}

    public static String render(TileView view) {
        return render(view, pos -> null);
    }

    /**
     * Renders the map, optionally overlaying occupancy markers (e.g., hero/monster/party).
     *
     * @param view terrain provider
     * @param markerProvider returns a marker string for a position (or null for none)
     */
    public static String render(TileView view, Function<Position, String> markerProvider) {
        StringBuilder sb = new StringBuilder();
        String horizontal = horizontalSeparator(view.cols());
        for (int r = 0; r < view.rows(); r++) {
            sb.append(horizontal).append("\n");
            StringBuilder row = new StringBuilder();
            row.append("|");
            for (int c = 0; c < view.cols(); c++) {
                Position pos = new Position(r, c);
                TileType tileType = view.tileTypeAt(pos);
            String marker = markerProvider != null ? markerProvider.apply(pos) : null;
            String baseSymbol = marker != null ? marker : symbolForTile(tileType);
            String raw = sanitize(baseSymbol);
            String colored = marker != null ? colorHero(raw) : colorForTile(tileType, raw);
            row.append(" ").append(colored).append(pad(raw)).append("|");
        }
        sb.append(row).append("\n");
    }
    sb.append(horizontal);
    return sb.toString();
    }

    private static String horizontalSeparator(int cols) {
        StringBuilder sb = new StringBuilder("+");
        for (int c = 0; c < cols; c++) {
            sb.append("----+");
        }
        return sb.toString();
    }

    private static String sanitize(String content) {
        if (content == null) return " ";
        String cleaned = content.replace("|", "").replace("\n", "").trim();
        if (cleaned.length() > 3) {
            cleaned = cleaned.substring(0, 3);
        }
        return cleaned;
    }

    private static String pad(String raw) {
        int len = raw == null ? 0 : raw.length();
        int spaces = Math.max(0, 3 - len);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String symbolForTile(TileType type) {
        if (type == TileType.PLAIN) {
            return " ";
        }
        return String.valueOf(type.getSymbol());
    }

    private static String colorForTile(TileType type, String symbol) {
        switch (type) {
            case INACCESSIBLE:
                return ColorUtil.gray(symbol);
            case MARKET:
                return ColorUtil.gold(symbol);
            default:
                return symbol;
        }
    }

    private static String colorHero(String symbol) {
        return ColorUtil.BRIGHT_GREEN + symbol + ColorUtil.RESET;
    }
}
