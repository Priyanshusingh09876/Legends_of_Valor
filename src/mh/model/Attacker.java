package mh.model;

import java.util.Random;

public interface Attacker {
    AttackResult attack(Creature target, Random random);
}
