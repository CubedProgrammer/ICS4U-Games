package kz.si.main;
import static java.lang.Math.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
/**
 * SpaceInvaders.java
 * @author Kevin Zhang
 */
public class SpaceInvaders extends Canvas {

    /**
     * Width of the window
     */
    public static final int WIDTH=960;

    /**
     * Height of the window
     */
    public static final int HEIGHT=640;

    /**
     * Change in time for one frame
     */
    public static final long CHANGE=16666667;

    /**
     * A boolean indicating if the game is running
     */
    private boolean running;

    /**
     * The thread for running this game
     */
    private Thread thread;

    /**
     * Shield array
     */
    private byte[][]shield;

    /**
     * Shield array
     */
    private BufferedImage shimg;

    /**
     * List of entities
     */
    private LinkedList<AbstractEntity>ens;

    /**
     * List of health packs
     */
    private LinkedList<HealthPack>packs;

    /**
     * Screen buffered image
     */
    private BufferedImage screen;

    /**
     * Random number generator
     */
    private Random random;

    /**
     * Movement timer for enemies
     */
    private int mt;

    /**
     * Whether or not the player has won
     */
    private boolean won;

    /**
     * Whether or not the player has lost
     */
    private boolean lost;

    /**
     * Frame counter
     */
    private int frames;

    /**
     * Main method
     * @param args Command line arguments
     */
    public static final void main(String[]args) {

        //JFrame and game canvas
        JFrame frame=new JFrame("Space Invaders");
        SpaceInvaders game=new SpaceInvaders();

        //set up the window
        frame.add(game);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.start();

    }

    /**
     * Constructor for game space invaders
     */
    public SpaceInvaders() {

        //sets the size
        setSize(WIDTH,HEIGHT);
        setPreferredSize(getSize());
        //sets up the shield and image
        this.shield=new byte[getHeight()][getWidth()];
        this.shimg=new BufferedImage(getWidth(),getHeight(),2);
        //entities and health packs
        this.ens=new LinkedList<>();
        this.packs=new LinkedList<>();
        //buffered image for screen
        this.screen=new BufferedImage(getWidth(),getHeight(),2);
        ens.add(new Player(480,480));
        //key listener
        addKeyListener(new KeyHandler(this,(Player)ens.getFirst()));

        //the four shields
        for(int i=0;i<4;i++) {
            makeRectShield(60+240*i,330,120,120,5);
        }

        //initialize random number generator and enemy movement timer
        this.random=new Random();
        this.mt=240;

        //adds the enemies into the entities list
        for(int i=0;i<4;i++) {
            for(int j=0;j<10;j++) {
                ens.add(new EnemyShip(90+j*60,120+i*36,18,120,random.nextInt(420)+300));
                ens.getLast().setVelX(1);
            }
        }

    }

    /**
     * Run method for running the game
     */
    public void run() {

        //timer setup and frame counter
        long last=System.nanoTime();
        long now=0;
        long passed=0;
        long timer=0;
        int frames=0;

        //while the game is running
        while(running) {

            //gets the time since last loop
            now=System.nanoTime();
            passed+=now-last;
            timer+=now-last;
            last=now;

            //if enough time has passed
            if(passed>CHANGE) {

                //tick, render, reset timer and increment frame counter
                tick();
                render();
                passed-=CHANGE;
                frames++;

            }

            //if one second has passed
            if(timer>1000000000) {

                this.frames=frames;//sets frames
                frames=0;//reset frame counter
                timer=0;//resets timer

            }

        }

    }

    /**
     * Starts the threads
     */
    public synchronized void start() {

        this.thread=new Thread(this::run);
        this.running=true;
        thread.start();

    }

    /**
     * Game tick method, ticks sixty times per second
     */
    public void tick() {

        //don't tick if the player has already won or lost
        if(won||lost)
            return;

        Player player=(Player)ens.getFirst();//gets the player
        Iterator<AbstractEntity>it=ens.iterator(),_it_;//iterators
        LinkedList<AbstractEntity>queue=new LinkedList<>();//entities to be appended are queued up here
        AbstractEntity en,bb;//entity variables
        EnemyShip em;//enemy
        BasicBullet b;//bullet

        //used for checking rectangle collision
        boolean lr1,rl1;
        boolean ud1,du1;
        boolean lr2,rl2;
        boolean ud2,du2;
        boolean lr,rl,ud,du;

        int old=0;//old hp for damage on collision
        won=true;//set won to true for now

        //randomly add a health pack
        if(random.nextInt(180)==0) {
            packs.add(new HealthPack(random.nextInt(960),480,random.nextInt(10)+10));
        }

        //loops through the list of entities
        while(it.hasNext()) {

            en=it.next();//get the next entity
            en.run();//and run it

            //special case for enemies
            if(en.type==EntityType.ENEMY) {

                em=(EnemyShip)en;//cast to enemy
                won=false;//set won to false, there are still enemies remaining

                //if shooting cooldown is zero
                if(em.getCD()==0) {

                    //attack the player
                    em.target(player);

                    //if the shot is requested by the enemy
                    if(em.isShotRequested()) {

                        queue.add(em.getBullet());//queue up the bullet to be added
                        em.setShotRequested(false);//the shot request has been handled
                        em.setCD(em.defaultCD);//reset cooldown

                    }

                }

                //if enemy movement timer is up
                if(mt==0) {

                    //moves the enemy in opposite direction
                    em.setVelX(em.getVelX()*-1);

                    //and move them down
                    if(em.getY()<300)
                        em.setY(em.getY()+1);

                }

            }

            //if entity is a bullet
            if(en.type==EntityType.BULLET) {

                b=((BasicBullet)en);//gets the bullet
                _it_=ens.iterator();//sets the iterator

                //checks if bullet collides with shield
                if(dmgln(b.getX(),b.getY(),(int)(b.getX()+b.getVelX()),(int)(b.getY()+b.getVelY()),b.length()>>1,1)) {
                    b.dmg(10);
                }

                //checks for collisions
                while(_it_.hasNext()) {

                    //gets the next entity
                    bb=_it_.next();

                    //don't check collisions with itself
                    if(bb!=en) {

                        //checks if any of the edges of bb is inside b
                        lr1=bb.getX()-bb.length()/2<b.getX()+b.length()/2&&bb.getX()-bb.length()/2>b.getX()-b.length()/2;
                        rl1=bb.getX()+bb.length()/2>b.getX()-b.length()/2&&bb.getX()+bb.length()/2<b.getX()+b.length()/2;
                        ud1=bb.getY()-bb.length()/2<b.getY()+b.length()/2&&bb.getY()-bb.length()/2>b.getY()-b.length()/2;
                        du1=bb.getY()+bb.length()/2>b.getY()-b.length()/2&&bb.getY()+bb.length()/2<b.getY()+b.length()/2;

                        //checks if any of the edges of b is inside bb
                        lr2=b.getX()-b.length()/2<bb.getX()+bb.length()/2&&b.getX()-b.length()/2>bb.getX()-bb.length()/2;
                        rl2=b.getX()+b.length()/2>bb.getX()-bb.length()/2&&b.getX()+b.length()/2<bb.getX()+bb.length()/2;
                        ud2=b.getY()-b.length()/2<bb.getY()+bb.length()/2&&b.getY()-b.length()/2>bb.getY()-bb.length()/2;
                        du2=b.getY()+b.length()/2>bb.getY()-bb.length()/2&&b.getY()+b.length()/2<bb.getY()+bb.length()/2;

                        //combines booleans
                        lr=lr1||lr2;
                        rl=rl1||rl2;
                        ud=ud1||ud2;
                        du=du1||du2;

                        //a bullet can't hurt an entity of it's shooter's type
                        if(b.shooter!=bb.type&&(lr||rl)&&(ud||du)) {

                            old=b.getHP();//stores old hp in temporary variable
                            b.dmg(bb.getHP());//damage b
                            bb.dmg(old);//damage bb

                        }

                    }

                }

                //if bullet lifespan is up, remove it
                if(b.life()==0) {
                    it.remove();
                    continue;
                }

            }

            //checks if entity is outside the screen, or has zero health
            if(en.getHP()==0||en.getX()+en.getVelX()<0||en.getX()+en.getVelX()>=getWidth()||en.getY()+en.getVelY()<0||en.getY()+en.getVelY()>=getHeight()) {
                //restrict it if it is the player, and remove it if it is not
                if(en.type==EntityType.PLAYER) {
                    en.setX(max(0,min(getWidth(),en.getX())));
                    en.setY(max(0,min(getHeight(),en.getY())));
                } else {

                    //gives the player the reward if it is an enemy
                    if(en.type==EntityType.ENEMY) {
                        em=(EnemyShip)en;
                        player.addScore(em.reward);
                    }

                    it.remove();

                }
            }

        }

        //health pack iterator and temporary variable
        Iterator<HealthPack>ith=packs.iterator();
        HealthPack h;

        //loop through list of health packs
        while(ith.hasNext()) {

            //get the next health pack and decrease it's timer
            h=ith.next();
            h.decrement();

            //if the player is close enough to the health pack
            if(player.getHP()>0&&sqrt((h.x-player.getX())*(h.x-player.getX())+(h.y-player.getY())*(h.y-player.getY()))<player.length()/2+15) {

                //heal the player and remove the health pack
                player.dmg(h.pow*-1);
                ith.remove();
                continue;

            }

            //remove the health pack if timer is up
            if(h.getLife()==0)
                ith.remove();

        }

        //add everything that was queued up to be added
        ens.addAll(queue);

        //decrease enemy movement timer
        if(mt>0)
            mt--;
        else
            mt=240;

        //checks for player shooting
        if(player.getHP()>0&&player.getCD()==0&&player.isShooting()) {

            //shoots the bullet and reset cooldown
            b=new BasicBullet(player.getX(),player.getY(),6,player.type);
            b.setVelY(-5);
            ens.add(b);
            player.setCD(Player.DEFAULT_COOLDOWN);

        }

        //checks if the player lost
        lost=player.getHP()==0;

    }

    /**
     * Performs rendering with triple buffering
     */
    public void render() {

        //gets the buffer strategy
        BufferStrategy bs=getBufferStrategy();
        Graphics2D g;

        //create the buffer strategy if there is none
        if(bs==null) {
            createBufferStrategy(3);
            return;
        }

        //get the graphics and paint onto screen image, and then draw screen image onto screen
        g=(Graphics2D)bs.getDrawGraphics();
        Graphics2D g2d=screen.createGraphics();
        paint(g2d);
        g.drawImage(screen,0,0,null);
        g2d.dispose();
        g.dispose();
        bs.show();

    }

    /**
     * Paints the screen
     * @param g The graphics object to draw onto
     */
    public void paint(Graphics2D g) {

        //if the player won display winning message
        if(won) {

            g.setColor(new Color(18,238,238));
            g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(new Color(238,18,18));
            g.setFont(new Font("Arial",0,60));
            FontMetrics fontMetrics=g.getFontMetrics();
            String str="Congradulations, you won!";
            g.drawString(str,getWidth()/2-fontMetrics.stringWidth(str)/2,60);
            String res="Press Enter to play again";
            g.drawString(res,getWidth()/2-fontMetrics.stringWidth(res)/2,135);
            return;

        }

        //if the player lost display losing message
        if(lost) {

            g.setColor(new Color(238,18,18));
            g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(new Color(18,238,238));
            g.setFont(new Font("Arial",0,60));
            FontMetrics fontMetrics=g.getFontMetrics();
            String str="Aww, you lost!";
            g.drawString(str,getWidth()/2-fontMetrics.stringWidth(str)/2,60);
            String res="Press Enter to play again";
            g.drawString(res,getWidth()/2-fontMetrics.stringWidth(res)/2,135);
            return;

        }

        //fill the background black
        g.setColor(new Color(18,18,18));
        g.fillRect(0,0,WIDTH,HEIGHT);
        //gets pixel array of shield image
        int[]pxls=((DataBufferInt)shimg.getRaster().getDataBuffer()).getData();

        //and make and shield pixels translucent green
        for(int i=0;i<shield.length;i++) {
            for(int j=0;j<shield[i].length;j++) {
                pxls[i*getWidth()+j]=0x12ee12|32*(shield[i][j]%=16)<<24;
            }
        }

        //draw the shield image onto the screen
        g.drawImage(shimg,0,0,null);

        //iterators
        Iterator<AbstractEntity>it=ens.iterator();
        Iterator<HealthPack>ith=packs.iterator();
        HealthPack h;

        //render all the entities
        while(it.hasNext()) {
            it.next().render(g);
        }

        //renders all the health packs
        while(ith.hasNext()) {

            h=ith.next();
            g.setColor(new Color(18,238,18,h.getLife()*2));
            g.fillRect(h.x-15,h.y-15,30,30);
            g.setColor(new Color(238,18,18,h.getLife()*2));
            g.fillRect(h.x-13,h.y-3,26,6);
            g.fillRect(h.x-3,h.y-13,6,26);

        }

        //gets the player
        Player player=(Player)ens.getFirst();

        //displays health bar and score
        g.setColor(new Color(105,105,105));
        g.fillRect(30,30,player.mhp+30,60);
        g.setColor(new Color(238,18,18));
        g.fillRect(45,45,player.mhp,30);
        g.setColor(new Color(18,238,18));
        g.fillRect(45,45,player.getHP(),30);
        g.setColor(new Color(238,238,238));
        g.setFont(new Font("Times new roman",0,30));
        g.drawString("Score: "+Integer.toString(player.getScore()),15,120);

        //displays fps
        String s="Fps: "+frames;
        FontMetrics fm=g.getFontMetrics();
        g.drawString(s,getWidth()-fm.stringWidth(s)-15,45);

    }

    /**
     * Checks if the pair of coordinates (x,y) is out of bounds
     * @param x The x coordinate
     * @param y The y coordinate
     * @return True if the coordinate is within bounds
     */
    public boolean bounds(int x,int y) {
        return x>=0&&x<getWidth()&&y>=0&&y<getHeight();
    }

    /**
     * Damages the shield when bullet collides
     * @param x1 The starting x coordinate
     * @param y1 The starting y coordinate
     * @param x2 The ending x coordinate
     * @param y2 The ending y coordinate
     * @param size The size of the bullet
     * @param dmg The amount of damage to deal
     * @return True if the bullet would have collided with the shield
     */
    public boolean dmgln(int x1,int y1,int x2,int y2,int size,int dmg) {

        //variables for checking collision and direction
        boolean collided=false;
        int cx=x2-x1,cy=y2-y1;
        int cax=abs(cx),cay=abs(cy);
        double dx=0,dy=0;

        //gets the direction the bullet travels
        if(cax==0)
            dy=cy/cay;
        else if(cay==0)
            dx=cx/cax;
        else if(cax<cay) {
            dy=cy/cay;
            dx=(double)cx/cay;
        } else {
            dx=cx/cax;
            dy=(double)cy/cax;
        }

        //current x and y for simulation
        double x=x1,y=y1;

        //simulate bullet travelling to its location in the next frame
        while(round(x)!=x2||round(y)!=y2) {

            //damages the shield
            for(int i=(int)y-size;i<(int)y+size;i++) {
                for(int j=(int)x-size;j<(int)x+size;j++) {
                    if(bounds(j,i)&&shield[i][j]>0&&shield[i][j]<16){
                        shield[i][j]+=64-dmg;//damages the shield and mark it as checked so it isn't damaged again
                        collided=true;//the bullet collided
                    }
                }
            }

            x+=dx;//update x
            y+=dy;//update y

        }

        return collided;

    }

    /**
     * Makes a rectangular shield
     * @param x The left side of the rectangle
     * @param y The top side of the rectange
     * @param w The width of the rectangle
     * @param h The height of the rectange
     * @param strength The strength of the shield
     */
    public void makeRectShield(int x,int y,int w,int h,int strength) {

        //loop through the rectangle and update shield array
        for(int i=y;i<y+h;i++) {
            for(int j=x;j<x+w;j++) {
                this.shield[i][j]=(byte)strength;
            }
        }

    }

    /**
     * Checks if the game is finished
     */
    public boolean finished() {
        return won||lost;
    }

    /**
     * Restarts the game
     */
    public void restart() {

        //reset shield and shield image
        this.shield=new byte[getHeight()][getWidth()];
        this.shimg=new BufferedImage(getWidth(),getHeight(),2);
        //clear entities and health packs
        ens.clear();
        packs.clear();
        //resets screen
        this.screen=new BufferedImage(getWidth(),getHeight(),2);
        //add player and change the pointer in the key listener
        ens.add(new Player(480,480));
        ((KeyHandler)getKeyListeners()[0]).setPlayer((Player)ens.getFirst());

        //make the rectangular shields
        for(int i=0;i<4;i++) {
            makeRectShield(60+240*i,330,120,120,5);
        }

        //reset enemy movement timer
        this.mt=240;

        //add in the enemies
        for(int i=0;i<4;i++) {
            for(int j=0;j<10;j++) {
                ens.add(new EnemyShip(90+j*60,120+i*36,18,120,random.nextInt(420)+300));
                ens.getLast().setVelX(1);
            }
        }

        //reset win/loss tracker
        won=false;
        lost=false;

    }

}