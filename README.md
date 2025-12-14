# Legends: Monsters and Heroes / Legends of Valor

This project is a console-based Java game developed for **CS 611**.  
It supports **two game modes** under a unified codebase:

1. **Legends: Monsters and Heroes** — the original, fully playable game  
2. **Legends of Valor** — an extended mode with a lane-based world/map system  
   (world layer implemented; gameplay intentionally not implemented yet)

The project emphasizes **clean object-oriented design**, **code reuse**, and a
clear separation of responsibilities between **launcher**, **world/map**, and
**gameplay logic**.

---

## Features

### Legends: Monsters and Heroes (Legacy Game)
- Load heroes, monsters, weapons, armor, potions, and spells from supplied text files.
- Create a party of 1–3 heroes (Warrior, Sorcerer, Paladin).
- Explore a randomly generated 8×8 world map with inaccessible, market, and common tiles.
- Battle level-scaled monsters using attacks, spells, and items.
- Markets allow buying and selling items with level and gold checks.
- Heroes level up, regenerate between rounds, and revive after victories.

### Legends of Valor (World Layer)
- 8×8 lane-based map with:
  - Three lanes (top, middle, bottom)
  - Inaccessible wall columns separating lanes
  - Monsters’ Nexus (top row) and Heroes’ Nexus (bottom row)
- Terrain types: Nexus, Inaccessible, Plain, Bush, Cave, Koulou, Obstacle
- Random terrain distribution within lanes
- Console visualization of the world/map only  
  (**no heroes, monsters, or gameplay logic implemented yet**)

---

## Project Structure

- `Main`  
  Responsible **only** for selecting the game mode at runtime.

- `World / Map Layer`  
  - Represents terrain and map structure only  
  - No heroes, monsters, combat, movement, or game rules  
  - Shared abstraction used by different game modes

- `Gameplay Layer (Legacy)`  
  - Existing Monsters and Heroes logic  
  - Not modified by the launcher or Valor world

This separation ensures the project is easy to extend and maintain.

---

## How to Run the Project

### Scheme A – Unified Launcher (Recommended)

This is the recommended way to run the project for development and grading.

### Compile

From the repository root, compile all source files:

```bash
javac -d out $(find src -name "*.java")
```
### Run (Unified Launcher)

```bash
java -cp out mh.Main
```

Option 1 launches the legacy Legends: Monsters and Heroes game.

Option 2 launches a Legends of Valor world/map preview


Controls (Legends: Monsters and Heroes)
When running the legacy game (directly or via the launcher):
    W / A / S / D — Move
    M — Enter market
    I — View hero information
    P — Manage inventory
    Q — Quit the game
