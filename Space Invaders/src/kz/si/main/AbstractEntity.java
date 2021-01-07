package kz.si.main;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
/**
 * Class AbstractEntity
 * This is the super class of all entities in the game
 * It contains predefined methods that it's subclasses will need to override
 * @author Kevin Zhang
 */
public abstract class AbstractEntity {

    /**
     * The x-coordinate of this entity
     */
    private double x;

    /**
     * The y-coordinate of this entity
     */
    private double y;

    /**
     * The x component of the velocity
     */
    private double vx;

    /**
     * The y-component of the velocity
     */
    private double vy;

    /**
     * The y-component of the velocity
     */
    private int size;

    /**
     * The health of the entity
     */
    private int hp;

    /**
     * The maximum health of the entity
     */
    public final int mhp;

    /**
     * The type of the entity
     */
    public final EntityType type;

    /**
     * The picture of the entity
     */
    protected BufferedImage texture;

    /**
     * Constructs an abstract entity
     * @parem type The type of the entity
     * @param x The x-coordinate of the entity
     * @param y The y coordinate of the entity
     * @param size The size of the entity
     * @param texture The path to the texture file
     */
    public AbstractEntity(EntityType type,int x,int y,int size,int mhp,String texture) {

        this.type=type;
        this.x=x;
        this.y=y;
        this.size=size;
        this.mhp=this.hp=mhp;

        try {
            this.texture=ImageIO.read(getClass().getResource(texture));
        } catch(IOException e) {
            e.printStackTrace();
        }

        if(this.texture==null) {

            this.texture=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=this.texture.createGraphics();

            g.setColor(new Color(238,238,18));
            g.fillRect(0,0,this.texture.getWidth(),this.texture.getHeight());

        }

    }

    /**
     * Gets the x coordinate
     * @return The y coordinate
     */
    public int getX() {
        return(int)x;
    }

    /**
     * Gets the y coordinate
     * @return The y coordinate
     */
    public int getY() {
        return(int)y;
    }

    /**
     * Gets the x velocity
     * @return The x component of the velocity vector
     */
    public double getVelX() {
        return vx;
    }

    /**
     * Gets the y velocity
     * @return The y component of the velocity vector
     */
    public double getVelY() {
        return vy;
    }

    /**
     * Gets the size of the entity
     * @return The side length of the entity's hitbox
     */
    public int length() {
        return size;
    }

    /**
     * Gets the health of the entity
     * @return The health points of the entity
     */
    public int getHP() {
        return hp;
    }

    /**
     * Sets the x coordinate
     * @param x The x coordinate to set to
     */
    public void setX(int x) {
        this.x=x;
    }

    /**
     * Sets the y coordinate
     * @param y The y coordinate to set to
     */
    public void setY(int y) {
        this.y=y;
    }

    /**
     * Sets the x velocity
     * @param vx The x component of the velocity vector
     */
    public void setVelX(double vx) {
        this.vx=vx;
    }

    /**
     * Sets the y velocity
     * @param vy The y component of the velocity vector
     */
    public void setVelY(double vy) {
        this.vy=vy;
    }

    /**
     * Damages the entity
     * @param dmg The damage to take
     */
    public void dmg(int dmg) {
        this.hp=Math.max(0,Math.min(mhp,hp-dmg));
    }

    /**
     * Runs the entity
     */
    public void run() {

        x+=vx;
        y+=vy;
        tick();

    }

    /**
     * {@inheritDoc}
     */
    public abstract AbstractEntity clone();

    /**
     * Ticks the entity
     */
    public abstract void tick();

    /**
     * Renders the entity
     * @param g The graphics object to render the entity onto
     */
    public abstract void render(Graphics2D g);

}