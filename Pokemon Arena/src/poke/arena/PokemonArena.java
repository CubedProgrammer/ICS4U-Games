package poke.arena;
import static java.lang.System.out;
import java.io.*;
import java.util.*;
import lib.csv.CSVReader;
import lib.tries.Trie;
/**
 * PokemonArena.java
 * @author Kevin Zhang
 */
public class PokemonArena {
	
	static {
		
		CSVReader reader=new CSVReader(PokemonArena.class.getResourceAsStream("/pokemon.txt"));//create the csv file reader to read
		CSVReader.CSVEntry ne=reader.getNextEntry()[0];//the number of pokemon in the file
		int n=((Integer)ne.val()).intValue();//parsed into an int
		Pokemon[]ps=new Pokemon[n];//array of pokemons
		CSVReader.CSVEntry[]en=null;//entry from file
		Pokemon.Attk[]attks=null;//attacks array
		
		HashMap<String,PokemonActions>sta=new HashMap<String,PokemonActions>();//for converting string to action
		HashMap<String,Pokemon.Special>sps=new HashMap<String,Pokemon.Special>();//for converting string to special attack type
		HashMap<String,Pokemon.Types>tps=new HashMap<String,Pokemon.Types>();//for converting string to type
		
		String[]stak={"attack","retreat","pass","1","2","3"};
		PokemonActions[]stav={PokemonActions.ATTACK,PokemonActions.RETREAT,PokemonActions.PASS,PokemonActions.ATTACK,PokemonActions.RETREAT,PokemonActions.PASS};
		String[]spsk={" ","stun","wild card","wild storm","disable","recharge"};//array of special attack names
		Pokemon.Special[]spsv={Pokemon.Special.NONE,Pokemon.Special.STUN,Pokemon.Special.WCARD,Pokemon.Special.WSTORM,Pokemon.Special.DISABLE,Pokemon.Special.RECHARGE};//array of special attacks
		String[]tpsk={" ","earth","fighting","fire","water","leaf","electric"};//array of type names
		Pokemon.Types[]tpsv={Pokemon.Types.NONE,Pokemon.Types.EARTH,Pokemon.Types.FIGHTING,Pokemon.Types.FIRE,Pokemon.Types.WATER,Pokemon.Types.GRASS,Pokemon.Types.ELECTRIC};//array of types
		//put actions into hash map
		for(int i=0;i<stak.length;i++) {
			sta.put(stak[i],stav[i]);
		}
		//put specials into hash map
		for(int i=0;i<spsk.length;i++) {
			sps.put(spsk[i],spsv[i]);
		}
		//put types into hash map
		for(int i=0;i<tpsk.length;i++) {
			tps.put(tpsk[i],tpsv[i]);
		}
		//read the file for pokemon
		for(int i=0;i<ps.length;i++) {
			
			en=reader.getNextEntry();//gets the next entry
			attks=new Pokemon.Attk[((Integer)en[5].val()).intValue()];//make the array of attacks, it will be the sixth item of the row
			//parse the attacks
			for(int j=0;j<attks.length;j++) {
				attks[j]=new Pokemon.Attk((String)en[(j<<2)+6].val(),sps.getOrDefault((String)en[(j<<2)+9].val(),Pokemon.Special.NONE),((Integer)en[(j<<2)+8].val()).intValue(),((Integer)en[(j<<2)+7].val()).intValue());
			}
			//and store it in pokemon array
			ps[i]=new Pokemon(en[0].val().toString(),((Integer)en[1].val()).intValue(),tps.getOrDefault((String)en[2].val(),Pokemon.Types.NONE),tps.getOrDefault((String)en[3].val(),Pokemon.Types.NONE),tps.getOrDefault((String)en[4].val(),Pokemon.Types.NONE),attks);
			
		}
		
		try {
			reader.close();//try to close the reader
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		all=ps;//set all pokemon array to ps
		strToAction=sta;//set string to actions map to sta
		
	}
	
	/**
	 * The actions that can happen at each turn
	 * @author Kevin Zhang
	 */
	public static enum PokemonActions {
		ATTACK,RETREAT,PASS;
	}
	
	/**
	 * All pokemons are stored in this array
	 */
	public static final Pokemon[]all;
	
	public static final HashMap<String,PokemonActions>strToAction;
	
	/**
	 * Array lists of pokemons, the user's pokemons and the enemy pokemons
	 */
	private ArrayList<Pokemon>user,enemy;
	
	/**
	 * Reader for reading from stdin
	 */
	private BufferedReader reader;
	
	/**
	 * Whose turn it is
	 */
	private boolean pturn;
	
	/**
	 * Current good pokemon
	 */
	private Pokemon currentGoodPoke;
	
	/**
	 * Current bad pokemon
	 */
	private Pokemon currentBadPoke;
	
	/**
	 * Constructs the arena
	 * @param user The user's starting pokemons
	 * @param enemy The enemy pokemons
	 * @param reader The reader attached to stdin
	 */
	public PokemonArena(ArrayList<Pokemon>user,ArrayList<Pokemon>enemy,BufferedReader reader) {
		
		this.user=user;
		this.enemy=enemy;
		this.reader=reader;
		
	}
	
	/**
	 * This method runs the game
	 */
	public void start() {
		
		try {
			
			this.pturn=(System.nanoTime()>>>16&1)==1;//randomly decides the turn
			boolean opturn=this.pturn;
			
			//while neither side has been all knocked out
			while(this.user.size()>0&&this.enemy.size()>0) {
				
				if(this.currentGoodPoke==null) {
					this.currentGoodPoke=this.pickGoodPokemon();//ask the user to pick a pokemon
				}
				
				if(this.currentBadPoke==null) {
					this.currentBadPoke=this.pickBadPokemon();//randomly picks a bad pokemon
				}
				//declare whose turn it is
				out.println(this.pturn?"It is your turn.":"It is the computer's turn.");
				
				if(this.pturn) {
					this.play(this.currentGoodPoke,this.currentBadPoke);//the user plays a move
				} else {
					this.cmptr(this.currentBadPoke,this.currentGoodPoke);//the computer plays a move
				}
				
				if(pturn!=opturn) {
					//recover 10 energy
					if(this.currentBadPoke!=null) {
						this.currentBadPoke.setEnergy(this.currentBadPoke.getEnergy()+10);
					}
					
					for(int i=0;i<this.user.size();i++) {
						this.user.get(i).setEnergy(this.user.get(i).getEnergy()+10);
					}
					
				}
				//change the turn
				pturn=!pturn;
				
			}
			//win/loss messages
			if(this.user.size()==0) {
				out.println("Aww, you lost, it's alright, you'll do better next time!");
			} else {
				out.println("Congradulations, you won!");
			}
			
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Asks the user to pick an attack
	 * @param gud The good pokemon that is currently in battle
	 * @param baad The bad pokemon that is currently in battle
	 */
	public void atkp(Pokemon gud,Pokemon baad)throws IOException {
		
		//hash map allowing the user to pick attack by name instead of number, and trie for picking by prefix
		HashMap<String,Pokemon.Attk>strToAtk=new HashMap<String,Pokemon.Attk>();
		Trie trie=new Trie();
		boolean awake=false;//check if the pokemon has enough energy to perform any attack
		//display attacks to the user and add attacks and name to hash map
		for(int i=0;i<gud.getNumAttacks();i++) {
			
			out.print(Integer.toString(i+1)+". ");
			out.println(gud.getAttack(i));
			strToAtk.put(gud.getAttack(i).name.toLowerCase(),gud.getAttack(i));
			trie.addWord(gud.getAttack(i).name.toLowerCase());
			awake=awake||gud.getEnergy()>=gud.getAttack(i).cost;
			
		}
		
		if(awake) {
			
			out.println("Pick your attack:");
			String ac=reader.readLine().toLowerCase();//makes the user input lowercase
			Pokemon.Attk selected;//selected attack
			Pokemon.Outcome outc;//outcome of attack
			String[]matches=trie.match(ac);//array of matches
			ac=matches.length==0?"1":matches[0];//either pick one by default, or the first match
			
			if(!strToAtk.containsKey(ac)) {
				selected=gud.getAttack(Integer.parseInt(ac)-1);
			} else {
				selected=strToAtk.get(ac);
			}
			//old hp
			int ohp=baad.getHP();
			
			while((outc=Pokemon.atk(selected,gud,baad))==Pokemon.Outcome.NOT_ENOUGH_ENERGY) {
				
				out.println("Your pokemon does not have enough energy to perform this attack!");
				ac=reader.readLine().toLowerCase();
				//gets the matches of this prefix
				matches=trie.match(ac);
				ac=matches.length==0?"1":matches[0];
				
				if(!strToAtk.containsKey(ac)) {
					selected=gud.getAttack(Integer.parseInt(ac)-1);
				} else {
					selected=strToAtk.get(ac);
				}
				
			}
			
			switch(outc) {
				
				case ALREADY_DISABLED:
					out.printf("The pokemon you attacked was already disabled, but you still dealt %d damage to it.\r\n",ohp-baad.getHP());
					break;
					
				case STUNNED:
					out.println("Your pokemon is stunned!");
					break;
					
				case FAIL:
					out.println("Your attack failed!");
					break;
				
				case SUCCESS_NO_STUN:
					out.printf("The pokemon you attacked was not stunned, but you still dealt %d damage to it.\r\n",ohp-baad.getHP());
					break;
					
				case SUCCESS:
					out.printf("The attack was successful, you dealt %d damage.\r\n",ohp-baad.getHP());
					break;
					
				default:
					throw new InternalError("Something went horribly wrong!");//this case should not be happening
				
			}
			//remove bad pokemon if health reaches zero
			if(baad.getHP()<=0) {
				
				out.println("You have knocked out your enemy pokemon!");
				this.enemy.remove(baad);
				this.currentBadPoke=null;
				
				for(int i=0;i<this.user.size();i++) {
					this.user.get(i).setHP(this.user.get(i).getHP()+20);
				}
				
			}
			//show status of the user's pokemon
			out.print("Your pokemon's status: ");
			out.println(gud);
			//show status of the computer's pokemon
			out.print("Enemy pokemon's status: ");
			out.println(baad);
			
		} else {
			out.println("Attacking is not an option, your pokemon does not have enough energy, either retreat or pass!");
			this.play(gud,baad);
		}
		
	}
	
	/**
	 * Retreats the user's pokemon
	 * @param gud The good pokemon that is currently in battle
	 * @return Whether or not the retreat was successful;
	 */
	public boolean retreat(Pokemon gud) {
		
		if(gud.isStunned()) {
			
			out.println("Your pokemon cannot retreat, it is stunned!");
			gud.setStunned(false);
			return false;
			
		} else {
			out.println("Your pokemon has successfully retreated!");
			return true;
		}
		
	}
	
	/**
	 * Asks the user to make a move
	 * @param gud The good pokemon that is currently in battle
	 * @param baad The bad pokemon that is currently in battle
	 */
	public void play(Pokemon gud,Pokemon baad)throws IOException {
		//print options to the user
		out.println("1. Attack");
		out.println("2. Retreat");
		out.println("3. Pass");
		//reads the action
		String act=this.reader.readLine().toLowerCase();
		
		while(!strToAction.containsKey(act)) {
			out.println("Invalid choice!");
			act=this.reader.readLine().toLowerCase();
		}
		//checks and performs the action
		this.checkActionEvent(gud,baad,strToAction.get(act));
		
	}
	
	/**
	 * Checks and perform an action
	 * @param gud The good pokemon that is currently in battle
	 * @param baad The bad pokemon that is currently in battle
	 * @param action The action to perform
	 */
	public void checkActionEvent(Pokemon gud,Pokemon baad,PokemonActions action)throws IOException {
		
		switch(action) {
			
			case ATTACK:
				this.atkp(gud,baad);//perform the attack
				break;
				
			case RETREAT:
				if(this.retreat(gud)) {
					this.currentGoodPoke=null;//set current pokemon to null if retreat was successful
				}
				break;
				
			case PASS:
				out.println("You chose to do nothing this round.");
				if(gud.isStunned()) {
					gud.setStunned(false);
				}
				break;
				
			default:
				throw new InternalError("Something went horribly wrong!");//there are only supposed to be three cases, otherwise something went horribly wrong
			
		}
		
	}
	
	/**
	 * Asks the computer to make a move
	 * @param baad The bad pokemon that is currently in battle
	 * @param gud The good pokemon that is currently in battle
	 */
	public void cmptr(Pokemon baad,Pokemon gud) {
		
		Pokemon.Attk best=new Pokemon.Attk("dummy",Pokemon.Special.NONE,0,0);
		//finds the optimal attack and does that
		for(int i=0;i<baad.getNumAttacks();i++) {
			if(baad.getEnergy()>=baad.getAttack(i).cost&&Pokemon.strength(baad.getAttack(i),baad,gud)>=Pokemon.strength(best,baad,gud)) {
				best=baad.getAttack(i);
			}
		}
		
		if("dummy".equals(best.name)) {
			out.println("Computer Passes.");//pass if there isn't enough energy
		} else {
			
			out.printf("The computer has decided to use %s against you.\r\n",best.name);
			int ohp=gud.getHP();
			Pokemon.Outcome outc=Pokemon.atk(best,baad,gud);
			//the messages of each outcome
			switch(outc) {
				
				case ALREADY_DISABLED:
					out.printf("The computer attacked your pokemon, and dealt %d damage, and would have disabled your pokemon too if it wasn't already disabled.\r\n",ohp-gud.getHP());
					break;
					
				case STUNNED:
					out.println("The computer's pokemon has been stunned and cannot make a move.");
					break;
					
				case FAIL:
					out.println("The computer's pokemon attempted an attack, but failed, you come out unscathed!");
					break;
				
				case SUCCESS_NO_STUN:
					out.printf("Your pokemon was successfully hit but was not stunned, your pokemon took %d damage.\r\n",ohp-gud.getHP());
					break;
					
				case SUCCESS:
				
					out.printf("The computer successfully hit your pokemon and dealt %d damage.\r\n",ohp-gud.getHP());
					//if the attack was a stun
					if(best.special==Pokemon.Special.STUN) {
						out.println("The computer has also stunned your pokemon!");
					} else if(best.special==Pokemon.Special.DISABLE) {
						out.println("The computer has disabled your pokemon too!");
					}
					
					break;
					
				default:
					throw new InternalError("Something went horribly wrong!");//this case should not be happening
				
			}
			
			//remove good pokemon if health reaches zero
			if(gud.getHP()<=0) {
				
				out.println("Your has been knocked out by the enemy pokemon!");
				this.user.remove(gud);
				this.currentGoodPoke=null;
				//recover twenty health points
				baad.setHP(baad.getHP()+20);
				for(int i=0;i<this.user.size();i++) {
					this.user.get(i).setHP(this.user.get(i).getHP()+20);
				}
				
			} else {
				//show status of the user's pokemon
				out.print("Your pokemon's status: ");
				out.println(gud);
				//show status of the computer's pokemon
				out.print("Enemy pokemon's status: ");
				out.println(baad);
				
			}
			
		}
		
	}
	
	/**
	 * Picks a good pokemon
	 * @return The pokemon the user has picked for this battle
	 */
	public Pokemon pickGoodPokemon()throws IOException {
		
		out.println("Pick a pokemon for this battle:");//tells user to pick a pokemon
		Iterator<Pokemon>it=this.user.iterator();//iterator for pokemons
		//display options to the user
		while(it.hasNext()) {
			out.println(it.next().getName());
		}
		
		int pk=Integer.parseInt(this.reader.readLine())-1;//read line from user
		boolean invalid=pk<0||pk>=this.user.size();//checks valitity of the user's choice
		
		while(invalid) {
			
			out.println("Invalid choice!");//tell the user the choice was invalid, and repick
			pk=Integer.parseInt(this.reader.readLine())-1;
			invalid=pk<0||pk>=this.user.size();
			
		}
		
		out.printf("%s, I choose you!\r\n",this.user.get(pk).getName());
		return this.user.get(pk);
		
	}
	
	/**
	 * Picks a bad pokemon
	 * @return The pokemon the computer has picked for this battle
	 */
	public Pokemon pickBadPokemon() {
		//cheap way of generating a random number
		long tm=System.currentTimeMillis();
		int r=(int)(tm*tm/Pokemon.RANDOM_DIVISOR)%this.enemy.size();
		r=(r+this.enemy.size())%this.enemy.size();
		//tell the user what they are up against
		out.printf("You are up against %s.\r\n",this.enemy.get(r).getName());
		return this.enemy.get(r);
		
	}
	
}