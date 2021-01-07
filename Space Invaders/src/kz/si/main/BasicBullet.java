package kz.si.main;
import java.awt.*;
/**
 * Class BasicBullet
 * @author Kevin Zhang
 */
public class BasicBullet extends AbstractEntity {

    /**
     * Lifespan of the entity
     */
    private int lifespan;

    /**
     * The type of entity that shot this bullet
     */
    public final EntityType shooter;

    /**
     * Constructs a new bullet
     * @param x The x coordinate
     * @param y The y coordinate
     * @param size The size of the bullet
     * @param shooter The shooter of the bullet
     */
    public BasicBullet(int x,int y,int size,EntityType shooter) {

        super(EntityType.BULLET,x,y,size,size*size,"/bullet.png");
        this.lifespan=480;
        this.shooter=shooter;

    }

    /**
     * Gets the life of the bullet
     * @return The time the bullet has left
     */
    public int life() {
        return lifespan;
    }

    /**
     * {@inheritDoc}
     */
    public AbstractEntity clone() {
        return new BasicBullet(getX(),getY(),length(),shooter);
    }

    /**
     * {@inheritDoc}
     */
    public void tick() {
        if(lifespan>0)
            lifespan--;
    }

    /**
     * {@inheritDoc}
     */
    public void render(Graphics2D g) {
        g.drawImage(texture,getX()-(length()>>>1),getY()-(length()>>>1),length(),length(),null);
    }

}