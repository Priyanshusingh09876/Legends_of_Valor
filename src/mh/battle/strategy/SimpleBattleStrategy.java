package mh.battle.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import mh.items.Armor;
import mh.items.Potion;
import mh.items.Spell;
import mh.items.Weapon;
import mh.model.Hero;
import mh.model.AttackResult;
import mh.model.Monster;
import mh.util.ColorUtil;

/**
 * // FEATURE FROM LEONARDO: SimpleBattleStrategy implements the BattleStrategy pattern for decoupled combat flows.
 */
public class SimpleBattleStrategy implements BattleStrategy {
    private final Scanner scanner;
    private final Random random;

    private List<Hero> heroes;
    private List<Monster> monsters;
    private int highestMonsterLevel;
    private int monsterCount;

    public SimpleBattleStrategy(Scanner scanner, Random random) {
        this.scanner = scanner;
        this.random = random;
    }

    @Override
    public boolean executeBattle(List<Hero> partyHeroes, List<Monster> spawnedMonsters) {
        this.heroes = partyHeroes;
        this.monsters = new ArrayList<>(spawnedMonsters);
        this.highestMonsterLevel = spawnedMonsters.stream().mapToInt(Monster::getLevel).max().orElse(1);
        this.monsterCount = spawnedMonsters.size();
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_CYAN + "A battle begins!" + ColorUtil.RESET);
        heroes.forEach(Hero::prepareForBattle);
        while (!heroesDefeated() && !monstersDefeated()) {
            displayStatus();
            heroesTurn();
            if (monstersDefeated()) {
                break;
            }
            monstersTurn();
            endOfRoundRegeneration();
        }
        boolean heroesWon = monstersDefeated();
        heroes.forEach(Hero::resetAfterBattleCaps);
        distributeRewards(heroesWon);
        return heroesWon;
    }

    private void heroesTurn() {
        // FEATURE FROM PRIYANSHU: Battle actions cover attacks, spells, potions, equipment, and info lookup.
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                continue;
            }
            boolean actionTaken = false;
            while (!actionTaken) {
                System.out.printf("%s's turn. Choose action: [A]ttack, [S]pell, [P]otion, [E]quip, [I]nfo%n", hero.getName());
                char input = readActionChoice();
                switch (input) {
                    case 'S':
                        actionTaken = castSpell(hero);
                        break;
                    case 'P':
                        actionTaken = usePotion(hero);
                        break;
                    case 'E':
                        actionTaken = equip(hero);
                        break;
                    case 'I':
                        showInfo();
                        break;
                    case 'A':
                    default:
                        actionTaken = attack(hero);
                        break;
                }
            }
            monsters.removeIf(Monster::isFainted);
            if (monsters.isEmpty()) {
                break;
            }
        }
    }

    private void monstersTurn() {
        for (Monster monster : new ArrayList<>(monsters)) {
            if (monster.isFainted()) {
                continue;
            }
            Hero target = pickHero();
            if (target == null) {
                return;
            }
            AttackResult result = monster.attack(target, random);
            if (result.isDodged()) {
                System.out.printf(ColorUtil.MAGENTA + "%s dodged %s's attack!%s%n", target.getName(), monster.getName(), ColorUtil.RESET);
                continue;
            }
            if (result.isCritical()) {
                System.out.println(ColorUtil.BRIGHT_YELLOW + ColorUtil.BOLD + "[CRITICAL HIT!] "
                        + monster.getName() + " deals massive damage!" + ColorUtil.RESET);
            }
            double applied = result.getDamageApplied();
            String critTag = result.isCritical() ? " [✦ CRIT]" : "";
            System.out.printf(ColorUtil.YELLOW + "%s attacked %s for %.0f damage!%s%s%n",
                    monster.getName(), target.getName(), applied, critTag, ColorUtil.RESET);
            if (target.isFainted()) {
                System.out.printf("%s fainted!%n", target.getName());
            }
        }
    }

    private boolean attack(Hero hero) {
        Monster target = chooseMonsterTarget();
        if (target == null) {
            return false;
        }
        AttackResult result = hero.attack(target, random);
        if (result.isDodged()) {
            System.out.printf(ColorUtil.MAGENTA + "%s dodged the attack!%s%n", target.getName(), ColorUtil.RESET);
            return true;
        }
        if (result.isCritical()) {
            System.out.println(ColorUtil.BRIGHT_YELLOW + ColorUtil.BOLD + "[CRITICAL HIT!] "
                    + hero.getName() + " deals massive damage!" + ColorUtil.RESET);
        }
        double applied = result.getDamageApplied();
        String critTag = result.isCritical() ? " [✦ CRIT]" : "";
        System.out.printf(ColorUtil.YELLOW + "%s attacked %s for %.0f damage!%s%s%n",
                hero.getName(), target.getName(), applied, critTag, ColorUtil.RESET);
        return true;
    }

    private boolean castSpell(Hero hero) {
        List<Spell> spells = new ArrayList<>(hero.getSpells());
        if (spells.isEmpty()) {
            System.out.println("No spells left for this hero. Performing basic attack instead.");
            return attack(hero);
        }
        System.out.println("Choose a spell:");
        for (int i = 0; i < spells.size(); i++) {
            Spell sp = spells.get(i);
            System.out.printf("%d) %s (DMG: %.0f, Mana: %.0f, Type: %s)%n", i + 1, sp.getName(),
                    (double) sp.getBaseDamage(), (double) sp.getManaCost(), sp.getSpellType());
        }
        int choice = readIndexChoice(spells.size());
        Spell spell = spells.get(choice);
        if (hero.getMana() < spell.getManaCost()) {
            System.out.println("Not enough mana! Turn wasted.");
            return true;
        }
        Monster target = chooseMonsterTarget();
        if (target == null) {
            return false;
        }
        hero.useMana(spell.getManaCost());
        hero.removeSpell(spell);
        double mitigationFactor = Math.max(0.1, 100.0 / (100.0 + target.getDefense()));
        double damage = Math.max(1, hero.spellDamage(spell) * mitigationFactor);
        if (random.nextDouble() < target.getDodgeChance()) {
            System.out.printf(ColorUtil.MAGENTA + "%s resisted the spell!%s%n", target.getName(), ColorUtil.RESET);
            return true;
        }
        double applied = target.takeDamage(damage);
        spell.applyEffect(hero, target);
        System.out.printf(ColorUtil.YELLOW + "%s cast %s on %s for %.0f damage!%s%n",
                hero.getName(), spell.getName(), target.getName(), applied, ColorUtil.RESET);
        return true;
    }

    private boolean usePotion(Hero hero) {
        List<Potion> potions = hero.getInventory().getPotions();
        if (potions.isEmpty()) {
            System.out.println("No potions in inventory.");
            return false;
        }
        System.out.println("Choose a potion:");
        for (int i = 0; i < potions.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, potions.get(i).getDescription());
        }
        int choice = readIndexChoice(potions.size());
        if (hero.usePotion(choice)) {
            Potion potion = potions.get(choice);
            System.out.printf("%s used %s.%n", hero.getName(), potion.getName());
            return true;
        }
        return false;
    }

    private boolean equip(Hero hero) {
        List<Weapon> weapons = hero.getInventory().getWeapons();
        List<Armor> armors = hero.getInventory().getArmors();
        System.out.println("Equip [W]eapon or [A]rmor?");
        String input = readEquipChoice();
        if (input.equals("W")) {
            if (weapons.isEmpty()) {
                System.out.println("No weapons available.");
                return false;
            }
            for (int i = 0; i < weapons.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, weapons.get(i).getDescription());
            }
            int choice = readIndexChoice(weapons.size());
            Weapon weapon = weapons.get(choice);
            if (hero.equipWeapon(choice)) {
                System.out.printf("%s equipped %s.%n", hero.getName(), weapon.getName());
                return true;
            }
            return false;
        } else {
            if (armors.isEmpty()) {
                System.out.println("No armor available.");
                return false;
            }
            for (int i = 0; i < armors.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, armors.get(i).getDescription());
            }
            int choice = readIndexChoice(armors.size());
            Armor armor = armors.get(choice);
            if (hero.equipArmor(choice)) {
                System.out.printf("%s equipped %s.%n", hero.getName(), armor.getName());
                return true;
            }
            return false;
        }
    }

    private void endOfRoundRegeneration() {
        // BALANCING FIX: regeneration during combat has been disabled to ensure
        // that damage from monsters persists between rounds.
    }

    private boolean heroesDefeated() {
        return heroes.stream().allMatch(Hero::isFainted);
    }

    private boolean monstersDefeated() {
        return monsters.stream().allMatch(Monster::isFainted);
    }

    private Monster chooseMonsterTarget() {
        List<Monster> alive = monsters.stream().filter(m -> !m.isFainted()).collect(Collectors.toList());
        if (alive.isEmpty()) return null;
        if (alive.size() == 1) {
            return alive.get(0);
        }
        System.out.println("Choose a monster to target:");
        for (int i = 0; i < alive.size(); i++) {
            Monster monster = alive.get(i);
            System.out.printf("%d) %s (HP: %.0f)%n", i + 1, monster.getName(), monster.getHp());
        }
        int choice = readIndexChoice(alive.size());
        return alive.get(choice);
    }

    private Hero pickHero() {
        List<Hero> alive = heroes.stream().filter(h -> !h.isFainted()).collect(Collectors.toList());
        if (alive.isEmpty()) return null;
        return alive.get(random.nextInt(alive.size()));
    }

    private int readIndexChoice(int size) {
        int raw = readIntInRange(1, size);
        return raw - 1;
    }

    private int readIntInRange(int min, int max) {
        while (true) {
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) {
                    return val;
                }
            } catch (Exception ignored) {
            }
            System.out.println("Invalid choice, please try again.");
        }
    }

    private char readActionChoice() {
        while (true) {
            String in = scanner.nextLine().trim().toUpperCase();
            if (in.length() == 1 && "ASPEI".contains(in)) {
                return in.charAt(0);
            }
            System.out.println("Invalid choice, please try again.");
        }
    }

    private void showInfo() {
        System.out.println("Heroes:");
        heroes.forEach(h -> System.out.println(" - " + h.shortStatus()));
        System.out.println("Monsters:");
        monsters.forEach(m -> System.out.println(" - " + m.shortStatus()));
    }

    private void displayStatus() {
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_WHITE + "--- Current Status ---" + ColorUtil.RESET);
        heroes.forEach(this::printHeroStatus);
        monsters.forEach(this::printMonsterStatus);
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_WHITE + "----------------------" + ColorUtil.RESET);
    }

    private String readEquipChoice() {
        while (true) {
            String raw = scanner.nextLine().trim().toUpperCase();
            if (raw.startsWith("W")) {
                return "W";
            }
            if (raw.startsWith("A")) {
                return "A";
            }
            System.out.println("Invalid choice, please try again.");
        }
    }

    private void distributeRewards(boolean heroesWon) {
        if (!heroesWon) {
            System.out.println("The monsters have prevailed...");
            return;
        }
        // BALANCE FIX: reduce battle rewards so early heroes do not become instantly rich.
        int rewardExp = Math.max(2, monsterCount * 2);
        int rewardGold = Math.max(50, highestMonsterLevel * 80);
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                // FEATURE FROM PRIYANSHU: Fainted heroes revive after victory.
                hero.reviveHalf();
                continue;
            }
            hero.gainRewards(rewardExp, rewardGold);
        }
        System.out.println(ColorUtil.BOLD + ColorUtil.BRIGHT_GREEN + "Heroes won the battle! Rewards granted." + ColorUtil.RESET);
    }

    private void printHeroStatus(Hero hero) {
        final int nameWidth = 22;
        final int hpWidth = 32;
        final int mpWidth = 10;

        String name = ColorUtil.padRight(colorHeroName(hero), nameWidth);
        System.out.printf("H: %s (Lvl %d)%n", name, hero.getLevel());

        int currentHp = (int) Math.round(hero.getHp());
        int maxHp = (int) Math.round(hero.getMaxHp());
        String hp = ColorUtil.padRight("HP: " + ColorUtil.formatHP(currentHp, maxHp), hpWidth);
        System.out.println("   " + hp);

        String mp = ColorUtil.padRight("MP: " + ColorUtil.BRIGHT_BLUE + (int) Math.round(hero.getMana()) + ColorUtil.RESET, mpWidth);
        String weapon = "Weapon: " + (hero.getEquippedWeapon() != null ? hero.getEquippedWeapon().getName() : "None");
        String armor = "Armor: " + (hero.getEquippedArmor() != null ? hero.getEquippedArmor().getName() : "None");
        System.out.println("   " + mp + " " + weapon + " " + armor);
    }

    private void printMonsterStatus(Monster monster) {
        final int nameWidth = 22;
        final int hpWidth = 32;

        String name = ColorUtil.padRight(colorMonsterName(monster), nameWidth);
        System.out.printf("M: %s [%s]%n", name, coloredType(monster));

        int currentHp = (int) Math.round(monster.getHp());
        int maxHp = (int) Math.round(monster.getMaxHp());
        String hp = ColorUtil.padRight("HP: " + ColorUtil.formatHP(currentHp, maxHp), hpWidth);
        String stats = String.format("DMG: %.0f DEF: %.0f Dodge: %.2f",
                monster.getBaseDamage(), monster.getDefense(), monster.getDodgeChance());
        System.out.println("   " + hp);
        System.out.println("   " + stats);
    }

    private String colorHeroName(Hero hero) {
        String favored = hero.favoredStats();
        if ("Strength & Agility".equals(favored)) {
            return ColorUtil.RED + hero.getName() + ColorUtil.RESET;
        } else if ("Dexterity & Agility".equals(favored)) {
            return ColorUtil.BLUE + hero.getName() + ColorUtil.RESET;
        } else if ("Strength & Dexterity".equals(favored)) {
            return ColorUtil.CYAN + hero.getName() + ColorUtil.RESET;
        }
        return ColorUtil.WHITE + hero.getName() + ColorUtil.RESET;
    }

    private String colorMonsterName(Monster monster) {
        switch (monster.getMonsterType()) {
            case DRAGON:
                return ColorUtil.YELLOW + monster.getName() + ColorUtil.RESET;
            case EXOSKELETON:
                return ColorUtil.GREEN + monster.getName() + ColorUtil.RESET;
            case SPIRIT:
                return ColorUtil.MAGENTA + monster.getName() + ColorUtil.RESET;
            default:
                return ColorUtil.WHITE + monster.getName() + ColorUtil.RESET;
        }
    }

    private String coloredType(Monster monster) {
        switch (monster.getMonsterType()) {
            case DRAGON:
                return ColorUtil.YELLOW + "Dragon" + ColorUtil.RESET;
            case EXOSKELETON:
                return ColorUtil.GREEN + "Exoskeleton" + ColorUtil.RESET;
            case SPIRIT:
                return ColorUtil.MAGENTA + "Spirit" + ColorUtil.RESET;
            default:
                return monster.getMonsterType().name();
        }
    }
}
