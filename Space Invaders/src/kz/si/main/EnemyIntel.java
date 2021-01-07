package kz.si.main;
/**
 * EnemyIntel.java
 * @author Kevin Zhang
 */
public interface EnemyIntel {

    /**
     * Enemy shooting intellicence
     * @param e The ship that is shooting
     * @param p The player to shoot at
     */
    public abstract void intel(EnemyShip e,Player p);

    /**
     * Normal shooting intelligence
     * @param e The ship that is shooting
     * @param p The player to shoot at
     */
    public static void normal(EnemyShip e,Player p) {
        e.shoot(Math.atan2(p.getY()-e.getY(),p.getX()-e.getX()));
    }

}