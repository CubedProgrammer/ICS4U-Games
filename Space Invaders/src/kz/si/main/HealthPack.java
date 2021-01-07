package kz.si.main;
/**
 * HealthPack.java
 * @author Kevin Zhang
 */
public class HealthPack {

    /**
     * The x coordinate
     */
    public final int x;

    /**
     * The y coordinate
     */
    public final int y;

    /**
     * The amount to heal
     */
    public final int pow;

    /**
     * The life of the health pack
     */
    private int life;

    /**
     * Constructs a health pack
     * @param x The x coordinate
     * @param y The y coordinate
     * @param pow The amount to heal
     */
    public HealthPack(int x,int y,int pow) {

        this.x=x;
        this.y=y;
        this.pow=pow;
        this.life=120;

    }

    /**
     * Decreases lifespan
     */
    public void decrement() {
        if(life>0)
            life--;
    }

    /**
     * Gets the lifespan
     * @return The time this health pack has left to be picked up
     */
    public int getLife() {
        return life;
    }

}
