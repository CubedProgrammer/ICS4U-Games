package lib.csv;
import java.io.*;
import java.util.regex.*;
/**
 * CSVReader.java
 * @author Kevin Zhang
 */
public class CSVReader {
	
	/**
	 * Entry for csv reader
	 * @author Kevin Zhang
	 */
	public static class CSVEntry {
		
		/**
		 * Types of entry
		 * @author Kevin Zhang
		 */
		private static enum CSVType {
			INT,FLOAT,STR;
		}
		
		/**
		 * Type of this entry
		 */
		private final CSVType type;
		
		/**
		 * Value of this entry
		 */
		private Object v;
		
		/**
		 * Constructs an entry
		 * @param type The type of this entry
		 * @param v The value of this entry
		 */
		public CSVEntry(CSVType type,Object v) {
			this.type=type;
			this.v=v;
		}
		
		/**
		 * Gets the value
		 * @return The value of this entry
		 */
		public Object val() {
			return v;
		}
		
		/**
		 * Gets the type
		 * @return The type of this entry
		 */
		public CSVType vtype() {
			return type;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return v.toString();
		}
		
	}
	
	/**
	 * Internal buffered reader
	 */
	private BufferedReader reader;
	
	/**
	 * Constructs a new csv reader
	 * @param reader The reader to read from
	 */
	public CSVReader(Reader reader) {
		this.reader=new BufferedReader(reader);
	}
	
	/**
	 * Constructs a new csv reader
	 * @param in The input stream to read from
	 */
	public CSVReader(InputStream in) {
		this(new InputStreamReader(in));
	}
	
	/**
	 * Constructs a new csv reader
	 * @param f The file to read from
	 */
	public CSVReader(File f)throws FileNotFoundException {
		this(new FileInputStream(f));
	}
	
	/**
	 * Constructs a new csv reader
	 * @param s The name of the file to read from
	 */
	public CSVReader(String s)throws FileNotFoundException {
		this(new File(s));
	}
	
	/**
	 * Gets the next entry
	 * @return An array of entries, representing the next row
	 */
	public CSVEntry[]getNextEntry() {
		
		String[]ln=null;//variables initialized
		CSVEntry[]entries=null;//variables initialized
		
		try {
			
			ln=reader.readLine().split(",");//read line split amongst comma, hense, "comma seperated values"
			entries=new CSVEntry[ln.length];//make an array of entries the same as the length of the string array
			//loop through the entries
			for(int i=0;i<entries.length;i++) {
				//if the entry is a number, using a regular expression
				if(Pattern.matches("(-?[0-9]+\\.?[0-9]*)",ln[i])) {
					
					if(ln[i].contains(".")) {
						entries[i]=new CSVEntry(CSVEntry.CSVType.FLOAT,Double.parseDouble(ln[i]));//parse as double if it contains decimal point
					} else {
						entries[i]=new CSVEntry(CSVEntry.CSVType.INT,Integer.parseInt(ln[i]));//parse as int if it doesn't
					}
					
				} else {
					entries[i]=new CSVEntry(CSVEntry.CSVType.STR,ln[i]);//parse as string if it isn't a number
				}
				
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return entries;
		
	}
	
	/**
	 * Closes the stream
	 * @throws An IOException if the underlying stream could not be closed
	 */
	public void close()throws IOException {
		reader.close();
	}
	
}