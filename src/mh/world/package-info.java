/**
 * World abstractions and implementations.
 *
 * <p>There are two map models kept for compatibility:</p>
 * <ul>
 *   <li>The legacy Monsters &amp; Heroes map ({@code WorldMap}) uses {@code Space} subclasses
 *       in {@code mh.world.tile} (CommonSpace, MarketSpace, etc.) and implements
 *       {@link mh.world.TileView} for read-only terrain queries.</li>
 *   <li>The Legends of Valor board is implemented by {@link mh.world.ValorWorld}, which
 *       adds lane rules, obstacles, teleport/recall helpers, and rendering and implements
 *       {@link mh.world.TileView} plus {@link mh.world.ValorRules}.</li>
 * </ul>
 *
 * <p>The {@code Tile} hierarchy in {@code mh.world.tile} ({@code InaccessibleTile},
 * {@code MarketTile}, etc.) is legacy-only and kept so older code/tests can still compile.
 * New code should prefer the {@code Space} subclasses.</p>
 */
package mh.world;
