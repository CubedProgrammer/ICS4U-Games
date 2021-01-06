package poke.arena;
import java.util.Arrays;
/**
 * Pokemon.java
 * @author Kevin Zhang
 */
public class Pokemon {
	
	/**
	 * Pokemon types enum
	 * @author Kevin Zhang
	 */
	public static enum Types {
		NONE,EARTH,FIGHTING,FIRE,WATER,GRASS,ELECTRIC;
	}
	
	/**
	 * Pokemon special attacks enum
	 * @author Kevin Zhang
	 */
	public static enum Special {
		NONE,STUN,WCARD,WSTORM,DISABLE,RECHARGE;
	}
	
	/**
	 * Outcomes of a pokemon attacking another pokemon
	 * @author Kevin Zhang
	 */
	public static enum Outcome {
		NOT_ENOUGH_ENERGY,STUNNED,ALREADY_DISABLED,FAIL,SUCCESS_NO_STUN,SUCCESS;
	}
	
	/**
	 * Pokemon attacks class
	 * @author Kevin Zhang
	 */
	public static class Attk {
		
		/**
		 * Attack name
		 */
		public final String name;
		
		/**
		 * Attack type
		 */
		public final Special special;
		
		/**
		 * Attack damage
		 */
		public final int dmg;
		
		/**
		 * Attack energy cost
		 */
		public final int cost;
		
		/**
		 * Attack constructor
		 * @param name The name of the attack
		 * @param special The type of the attack
		 * @param dmg Damage of the attack
		 * @param cost Attack energy cost
		 */
		public Attk(String name,Special special,int dmg,int cost) {
			
			this.name=name;
			this.special=special;
			this.dmg=dmg;
			this.cost=cost;
			
		}
		
		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Attk at) {
			return this.name.equals(at.name)&&this.special==at.special&&this.dmg==at.dmg&&this.cost==at.cost;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return String.format("{Name: %s, Special: %s, Damage: %d,Cost: %d}",this.name,this.special.toString(),this.dmg,this.cost);
		}
		
	}
	
	/**
	 * Gets the strength of an attack given aggressor and victim
	 * @param a The attack to perform
	 * @param aggresser The attacking pokemon
	 * @param victim The attacked pokemon
	 * @return A value regarding the strength of the attack
	 */
	public static int strength(Attk a,Pokemon aggresser,Pokemon victim) {
		
		int s=a.dmg<<1;//damage, multiplied by two
		//does less damage if disabled
		if(aggresser.disabled) {
			s-=20;
		}
		
		s=Math.max(0,s);//can't go below zero
		//does weakness and resistance accordingly
		if(aggresser.type==victim.resist) {
			s>>>=1;
		} else if(aggresser.type==victim.weak) {
			s<<=1;
		}
		
		//there are four cases when the strength needs to be adjusted
		//wild storm has same average damage as the normal damage of the attack
		switch(a.special) {
			
			case STUN:
				s+=STUN_STRENGTH_ADDER;//add on a constant for stun
				break;
				
			case WCARD:
				s>>>=1;//since there is a one in two chance wild card succeeds, divide by two
				break;
				
			case DISABLE:
				if(!victim.disabled) {
					s+=DISABLE_STRENGTH_ADDER;//add disabling constant
				}
				break;
				
			case RECHARGE:
				s+=RECHARGE_STRENGTH_ADDER;//add recharge constant
				break;
			
		}
		
		return s;
		
	}
	
	/**
	 * Performs the action of having a pokemon attack another pokemon
	 * @param at The attack to perform
	 * @param aggresser The attacking pokemon
	 * @param victim The attacked pokemon
	 * @return A value regarding the outcome of the attack
	 */
	public static final Outcome atk(Attk at,Pokemon aggresser,Pokemon victim) {
		
		if(aggresser.ener<at.cost) {
			return Outcome.NOT_ENOUGH_ENERGY;//terminate if the aggresser is too tired to perform this attack
		} else if(aggresser.stunned) {
			aggresser.stunned=false;
			return Outcome.STUNNED;
		} else {
			//keep track of whether the attack was successful
			boolean successful=false;
			boolean stunned=true;
			boolean disabled=true;
			//cheap way of generating a random number
			long tm=System.currentTimeMillis();
			int r=(int)(tm*tm/RANDOM_DIVISOR);
			int ss=1;//number of successful strikes
			
			switch(at.special) {
				
				case NONE:
					//this is always successful;
					successful=true;
					break;
					
				case STUN:
					//this will also always be successful
					successful=true;
					//randomly stun the opponent
					if(stunned=(r&1)==1) {
						victim.stunned=true;
					}
					
					break;
					
				case WCARD:
					//the odds of a successful wild card attack is 50-50
					successful=(r&1)==1;
					break;
					
				case WSTORM:
					//set successful to false for now
					successful=false;
					ss=0;
					//toss a coin, and continue if it is heads, ends if it is tails
					while((r&1)==1) {
						//cheap way of generating random number
						tm=System.currentTimeMillis();
						r=(int)(tm*tm/RANDOM_DIVISOR);
						ss++;//increment the number of successful strikes
						successful=true;
						
					}
					
					break;
					
				case DISABLE:
					//the attack is alway sucessful
					successful=true;
					disabled=!victim.disabled;//if the victim is already disabled, then it wasn't successful
					victim.disabled=true;//disable the victim
					
					break;
					
				case RECHARGE:
					//this attack will always be successful
					successful=true;
					aggresser.ener=Math.min(aggresser.ener+20,MAX_ENERGY);
					
					break;
					
				default:
					throw new InternalError("Something went horribly wrong!");
				
			}
			//subtract the energy from the aggresser
			aggresser.ener-=at.cost;
			//if the attack is successful
			if(successful) {
				
				int dmg=at.dmg-(aggresser.disabled?10:0)<<1;
				dmg=Math.max(0,dmg);
				
				if(victim.weak==aggresser.type) {
					dmg<<=1;
				} else if(victim.resist==aggresser.type) {
					dmg>>>=1;
				}
				
				dmg*=ss;//multiply damage by the number of successful attacks, in case of wild storm
				victim.hhp-=dmg;//subtract the damage from the health
				victim.hhp=Math.max(0,Math.min(victim.mhhp,victim.hhp));//cap the victim's health
				
			}
			
			return successful?stunned?disabled?Outcome.SUCCESS:Outcome.ALREADY_DISABLED:Outcome.SUCCESS_NO_STUN:Outcome.FAIL;
			
		}
		
	}
	
	/**
	 * Constant for maximum energy
	 */
	public static final int MAX_ENERGY=50;
	
	/**
	 * Constant for the additional strength stun adds, a heuristic
	 */
	public static final int STUN_STRENGTH_ADDER=24;
	
	/**
	 * Constant for the additional strength disable adds, a heuristic
	 */
	public static final int DISABLE_STRENGTH_ADDER=20;
	
	/**
	 * Constant for the additional strength recharging adds, a heuristic
	 */
	public static final int RECHARGE_STRENGTH_ADDER=20;
	
	/**
	 * Constant for making random numbers
	 */
	public static final int RANDOM_DIVISOR=9765625;
	/**
	 * Name of the pokemon
	 */
	private String name;
	
	/**
	 * Health and maximum hit points of the pokemon, multiplied by two to account for resistances
	 */
	private int hhp,mhhp;
	
	/**
	 * Energy of the pokemon
	 */
	private int ener;
	
	/**
	 * Type of the pokemon
	 */
	private Types type;
	
	/**
	 * Weakness of the pokemon
	 */
	private Types weak;
	
	/**
	 * Resistance of the pokemon
	 */
	private Types resist;
	
	/**
	 * The pokemon's attacks
	 */
	private Attk[]atks;
	
	/**
	 * The pokemon's disabled status
	 */
	private boolean disabled;
	
	/**
	 * The pokemon's stunned status
	 */
	private boolean stunned;
	
	/**
	 * Constructor for the pokemon
	 * @param name The name of the pokemon
	 * @param hhp The hit points of the pokemon
	 * @param type The pokemons type
	 * @param weak The pokemon's weakness
	 * @param resist The pokemon's resistance
	 * @param atks The pokemon's possible attacks
	 */
	public Pokemon(String name,int hhp,Types type,Types weak,Types resist,Attk[]atks) {
		
		this.name=name;
		this.ener=MAX_ENERGY;
		this.hhp=this.mhhp=hhp<<1;
		
		this.type=type;
		this.weak=weak;
		this.resist=resist;
		this.atks=atks;
		
	}
	
	/**
	 * Gets the name of the pokemon
	 * @return The name of the pokemon
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Gets the type of the pokemon
	 * @return The type of the pokemon
	 */
	public Types getType() {
		return this.type;
	}
	
	/**
	 * Gets the weakness of the pokemon
	 * @return The weakness of the pokemon
	 */
	public Types getWeakness() {
		return this.weak;
	}
	
	/**
	 * Gets the resistance of the pokemon
	 * @return The resistance of the pokemon
	 */
	public Types getResistance() {
		return this.resist;
	}
	
	/**
	 * Gets the current health of the pokemon
	 * @return The current health of the pokemon
	 */
	public int getHP() {
		return this.hhp>>>1;
	}
	
	/**
	 * Gets the maximum health of the pokemon
	 * @return The maximum health of the pokemon
	 */
	public int getMaxHP() {
		return this.mhhp>>>1;
	}
	
	/**
	 * Gets the energy of the pokemon
	 * @return The energy of the pokemon
	 */
	public int getEnergy() {
		return this.ener;
	}
	
	public boolean isStunned() {
		return this.stunned;
	}
	
	/**
	 * Gets the number of different attacks the pokemon can perform
	 * @return The number of attacks
	 */
	public int getNumAttacks() {
		return this.atks.length;
	}
	
	/**
	 * Gets an attack
	 * @param i Which attack to get
	 * @return The attack at position i
	 */
	public Attk getAttack(int i) {
		return this.atks[i];
	}
	
	/**
	 * Sets the health of the pokemon
	 * @param hp The health to set to
	 */
	public void setHP(int hp) {
		this.hhp=hp<<1;
		this.hhp=Math.min(this.mhhp,Math.max(0,this.hhp));
	}
	
	/**
	 * Sets the energy of the pokemon
	 * @param ener The energy to set to
	 */
	public void setEnergy(int ener) {
		this.ener=Math.min(50,ener);
	}
	
	/**
	 * Sets the stunned status of the pokemon
	 * @param stunned The status to set to
	 */
	public void setStunned(boolean stunned) {
		this.stunned=stunned;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Pokemon poke) {
		return this.hhp==poke.hhp&&this.mhhp==poke.mhhp&&this.ener==poke.ener&&this.type==poke.type&&this.weak==poke.weak&&this.resist==poke.resist&&this.name.equals(poke.name)&&Arrays.equals(this.atks,poke.atks);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("{HP: %d/%d, Energy: %d, Name: %s, Type: %s, Weakness: %s, Resistance: %s, Attacks: %s}",this.hhp>>1,this.mhhp>>1,this.ener,this.name,this.type.toString(),this.weak.toString(),this.resist.toString(),Arrays.toString(this.atks));
	}
	
}