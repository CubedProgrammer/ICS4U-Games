package poke.main;
import static java.lang.System.out;
import java.io.*;
import java.util.*;
import poke.arena.*;
/**
 * PokeMain.java
 * @author Kevin Zhang
 */
public class PokeMain {
	
	/**
	 * Arena for pokemons
	 */
	public static PokemonArena arena;
	
	/**
	 * Main method where everything starts
	 * @param args Command line arguments
	 */
	public static final void main(String[]args) {
		
		try {
			
			//reader for reading from stdin
			BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
			//shows the user all the pokemon names
			for(int i=0;i<PokemonArena.all.length;i++) {
				System.out.println(Integer.toString(i+1)+". "+PokemonArena.all[i].getName());
			}
			
			out.println("Please pick four pokemon by number: ");
			boolean[]taken=new boolean[PokemonArena.all.length];//keep track of which ones have been picked
			int[]picks={-1,-1,-1,-1};//the four choices
			int pk=0;//current choice
			int picked=0;//number of already picked
			
			//keep picking until four pokemons are picked
			while(picked<picks.length) {
				//read an int from the user, their choice of pokemon
				pk=Integer.parseInt(reader.readLine())-1;
				//if it is out of bounds
				if(pk<0||pk>=PokemonArena.all.length) {
					out.println("The number you picked is out of bounds!");//tell the player that
				} else if(taken[pk]) {
					out.println("You already picked that one, dummy!");//tell them if one has already been picked
				} else {
					taken[picks[picked++]=pk]=true;//select the pokemon
					out.printf("You picked %s.\r\n",PokemonArena.all[pk].getName());//and tell the user their choice
				}
				
			}
			
			Arrays.sort(picks);
			//array lists of enemy and user pokemons
			ArrayList<Pokemon>enemy=new ArrayList<>();
			ArrayList<Pokemon>user=new ArrayList<>();
			//add everything as enemies at first
			for(int i=0;i<PokemonArena.all.length;i++) {
				enemy.add(PokemonArena.all[i]);
			}
			//then remove the ones the user picked, and add to user
			for(int i=picks.length-1;i>=0;i--) {
				user.add(enemy.remove(picks[i]));
			}
			//initialze the arena and start it
			arena=new PokemonArena(user,enemy,reader);
			arena.start();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
}