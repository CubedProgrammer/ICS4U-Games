package kz.si.main;
import static java.lang.Math.*;
import java.awt.*;
/**
 * EnemyShip.java
 * @author Kevin Zhang
 */
public class EnemyShip extends AbstractEntity {

    /**
     * The bullet to shoot
     */
    private AbstractEntity bul;

    /**
     * Enemy AI
     */
    private EnemyIntel intel;

    /**
     * Shot request
     */
    private boolean shot;

    /**
     * Shooting cooldown
     */
    private int cooldown;

    /**
     * Default shooting cooldown
     */
    public final int defaultCD;

    /**
     * Bullet speed
     */
    private int bs;

    /**
     * Reward when killed by the player
     */
    public final int reward;

    /**
     * Constructs a new enemy ship
     * @param x The x coordinate
     * @param y The y coordinate
     * @param size The size of the enemy
     * @param mhp The maximum health
     * @param dcd The default cooldown
     */
    public EnemyShip(int x,int y,int size,int mhp,int dcd) {

        super(EntityType.ENEMY,x,y,size,mhp,"/enemy.png");
        this.bs=3;
        this.intel=EnemyIntel::normal;
        this.cooldown=this.defaultCD=dcd;
        this.reward=mhp;

    }

    /**
     * Target the player
     * @param p The player to shoot
     */
    public void target(Player p) {
        intel.intel(this,p);
    }

    /**
     * Shoot a bullet
     * @param direction The direction to shoot in
     */
    public void shoot(double direction) {

        //uses the winding function to convert direction into coordinates
        this.bul=new BasicBullet(getX(),getY(),length()/3,this.type);
        bul.setVelX(bs*cos(direction));
        bul.setVelY(bs*sin(direction));
        shot=true;

    }

    /**
     * Gets the bullet
     * @return The bullet this enemy ship is shooting
     */
    public AbstractEntity getBullet() {
        return bul;
    }

    /**
     * Gets the cooldown
     * @return The current cooldown
     */
    public int getCD() {
        return cooldown;
    }

    /**
     * Sets the cooldown
     * @param cd The cooldown to set to
     */
    public void setCD(int cd) {
        this.cooldown=cd;
    }

    /**
     * Checks if a shot is requested
     * @return True if the enemy ship is requesting a shot
     */
    public boolean isShotRequested() {
        return shot;
    }

    /**
     * Sets the shot requested
     * @param shot The requested state to set to
     */
    public void setShotRequested(boolean shot) {
        this.shot=shot;
    }

    /**
     * {@inheritDoc}
     */
    public AbstractEntity clone() {
        return new EnemyShip(getX(),getY(),mhp,length(),defaultCD);
    }

    /**
     * {@inheritDoc}
     */
    public void tick() {
        if(cooldown>0)
            cooldown--;
    }

    /**
     * {@inheritDoc}
     */
    public void render(Graphics2D g) {
        g.drawImage(texture,getX()-(length()>>>1),getY()-(length()>>>1),length(),length(),null);
    }

}