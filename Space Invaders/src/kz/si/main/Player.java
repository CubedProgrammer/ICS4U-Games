package kz.si.main;
import java.awt.*;
/**
 * Class Player
 * @author Kevin Zhang
 */
public class Player extends AbstractEntity {

    /**
     * Size of the player
     */
    public static final int SIZE=16;

    /**
     * Default cooldown of the player
     */
    public static final int DEFAULT_COOLDOWN=30;

    /**
     * Whether or not the player is shooting
     */
    private boolean shooting;

    /**
     * Player shooting cooldown
     */
    private int cooldown;

    /**
     * Score from killing enemies
     */
    private int score;

    /**
     * Constructs a new player
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public Player(int x,int y) {
        super(EntityType.PLAYER,x,y,SIZE,120,"/player.png");
    }

    /**
     * Whether or not the player is shooting
     * @return The state of the player's shooting
     */
    public boolean isShooting() {
        return shooting;
    }

    /**
     * Sets whether or not the player is shooting
     * @param shooting True if the player should be shooting
     */
    public void setShooting(boolean shooting) {
        this.shooting=shooting;
    }

    /**
     * Gets the cooldown of the player
     * @return The cooldown
     */
    public int getCD() {
        return cooldown;
    }

    /**
     * Sets the cooldown of the player
     * @param cd The cooldown the player should have
     */
    public void setCD(int cd) {
        this.cooldown=cd;
    }

    /**
     * Gets the score of the player
     */
    public int getScore() {
        return score;
    }

    /**
     * Adds score to the player
     * @param score The score to add
     */
    public void addScore(int score) {
        this.score+=score;
    }

    /**
     * {@inheritDoc}
     */
    public AbstractEntity clone() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void tick(){
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