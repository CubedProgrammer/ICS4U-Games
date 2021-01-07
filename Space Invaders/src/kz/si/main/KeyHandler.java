package kz.si.main;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
/**
 * Class KeyHandler
 * @author Kevin Zhang
 */
public class KeyHandler extends KeyAdapter {

    /**
     * Pointer to the game
     */
    private SpaceInvaders game;

    /**
     * Pointer to the player
     */
    private Player player;

    /**
     * Constructs a new KeyHandler
     * @param game The game to control
     * @param player The player to control
     */
    public KeyHandler(SpaceInvaders game,Player player) {
        this.game=game;
        this.player=player;
    }

    /**
     * {@inheritDoc}
     */
    public void keyPressed(KeyEvent e) {

        //gets the key code
        int key=e.getKeyCode();

        //moves the player with A and D keys
        if(key==KeyEvent.VK_A) {
            player.setVelX(player.getX()>3?-3:0);
        } else if(key==KeyEvent.VK_D) {
            player.setVelX(player.getX()<957?3:0);
        }

        //shooting and restarting
        if(key==KeyEvent.VK_SPACE) {
            player.setShooting(true);
        } else if(game.finished()&&key==KeyEvent.VK_ENTER) {
            game.restart();
        }

    }

    /**
     * {@inheritDoc}
     */
    public void keyReleased(KeyEvent e) {

        //gets the key code
        int key=e.getKeyCode();

        //stop the player from moving
        if(key==KeyEvent.VK_A) {
            player.setVelX(0);
        } else if(key==KeyEvent.VK_D) {
            player.setVelX(0);
        }

        //stop the player from shooting
        if(key==KeyEvent.VK_SPACE) {
            player.setShooting(false);
        }

    }

    /**
     * Sets the player to control
     * @param player The player to control
     */
    public void setPlayer(Player player) {
        this.player=player;
    }

}