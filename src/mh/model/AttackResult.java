package mh.model;

public class AttackResult {
    private final double damageApplied;
    private final boolean critical;
    private final boolean dodged;

    private AttackResult(double damageApplied, boolean critical, boolean dodged) {
        this.damageApplied = damageApplied;
        this.critical = critical;
        this.dodged = dodged;
    }

    public static AttackResult dodged() {
        return new AttackResult(0, false, true);
    }

    public static AttackResult hit(double damageApplied, boolean critical) {
        return new AttackResult(damageApplied, critical, false);
    }

    public double getDamageApplied() {
        return damageApplied;
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean isDodged() {
        return dodged;
    }
}
