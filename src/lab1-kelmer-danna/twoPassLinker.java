import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class twoPassLinker {
	public static void main(String[] args) {
		
		//create symbol table
		HashMap<String,Integer> symbolTable = new HashMap<String,Integer>();
		
		//open file from command line argument
		if (args.length > 0) {
			File file = new File(args[0]);
		
			//File file = new File(f);
			Scanner fileInput = null;
			try {
				fileInput = new Scanner(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			
			int numdef; // number of definitions
			int numuse; // number of uses
			int numprog; // number of programs
			int globalloc = 0; //where i am within entire program
			int loc;
			
			while(fileInput.hasNext()) {
				
				//grab first number in file
				//this is number of definitions in current module
				numdef = Integer.parseInt(fileInput.next());
				
	
				for(int i = 0; i < (numdef); i++) {
					
					//this is my variable name, store it in table
					String var = fileInput.next();
	
					// this is my variable relative location
					// manipulate it and add it to table
					loc = Integer.parseInt(fileInput.next());
					
					//check if item is already in table
					if(symbolTable.get(var) == null) {
						//item is not already in table so add it
						symbolTable.put(var, loc+globalloc);
					}
					else {
						//item is already in table, print error 
						// don't change value
						System.out.printf("Error: The variable, %s, is multiply defined; first value used.\n", var);
					}
					
				}
				
				//next thing in file must be number of uses
				numuse = Integer.parseInt(fileInput.next());
				//System.out.println("numuse is " + numuse);
				
				//go through all uses
				for(int i = 0; i < numuse; i++) {
					
					String next = fileInput.next();
				}
				
				//next thing in file must be number of programs
				numprog = Integer.parseInt(fileInput.next());
				
				//go through all programs
				for (int i = 0; i < (numprog); i++) {
					
					String type = fileInput.next();
					loc = Integer.parseInt(fileInput.next());
					
				}
				
				//make sure that no symbols were added to symbol table beyond size of module
				//if they were, change their location to rel address 0
				for (String key : symbolTable.keySet()) {
				    if ( (symbolTable.get(key) - globalloc) > numprog ) {
				    	// if they do exceed, change to rel address of 0
				    	symbolTable.put(key, globalloc);
				    }
				}
				
				
				globalloc += numprog;
			}
			
			
			
			System.out.println();
			System.out.println("Symbol Table");
			for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
			    System.out.println( entry.getKey() + " = " + entry.getValue());
			}
			System.out.println();
			System.out.println("Memory Map");
			
			//create arraylist of uselist
			ArrayList<String> use_list = new ArrayList<String>();
			//create copy of use list to keep track of whether or not all symbols in use list actually got used
			ArrayList<String> use_list_used = new ArrayList<String>();
			
			// create a list of all symbols defined to keep track of which were used and which weren't
			Set<String> temp = symbolTable.keySet();
			ArrayList<String> used = new ArrayList<String>(temp);
			
			int mem_num = 0;
			globalloc = 0;
			
			try {
				fileInput = new Scanner(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			while(fileInput.hasNext()) {
				
				//grab first number in file
				//this is number of definitions in current module
				numdef = Integer.parseInt(fileInput.next());
				
	
				for(int i = 0; i < (numdef*2); i++) {
					//don't need to do anything during second pass
					//just get through this part
					
					String var = fileInput.next();
				}
				
				
				//next thing in file must be number of uses
				numuse = Integer.parseInt(fileInput.next());
				
				//go through all uses
				for(int i = 0; i < numuse; i++) {
					// add each symbol to array list
					// so that I know in what order they're listed
					String next = fileInput.next();
					use_list.add(next);
				}
				
				use_list_used.addAll(use_list);
				
				//next thing in file must be number of programs
				numprog = Integer.parseInt(fileInput.next());
				
				//go through all programs
				for (int i = 0; i < (numprog); i++) {
					// update the relative addresses
					// resolve external references
					// do all error checking
					
					String type = fileInput.next();
					loc = Integer.parseInt(fileInput.next());
	
					if (type.equals("R")) {
						//add to loc the globalloc and print out
						int rel_add = loc % 1000;
						//check if rel address exceeds module size
						if (rel_add > numprog) {
							
							System.out.println(mem_num + ": " + (loc - rel_add) + " Error: Relative address exceeds module size; zero used.");

						}
						else {
							System.out.println(mem_num + ": " + (loc + globalloc));
						}
					}
					else if (type.equals("E")) {
						int which_use = loc % 100;
						
						if(which_use > (use_list.size()-1)) {
							// want item not in use list
							System.out.println(mem_num + ": " + loc + " Error: External address exceeds length of use list; treated as immediate."); 
							
						}
						else {
							//if want item in range of use list, get it
							String sym = use_list.get(which_use);

							//replace address field with actual
							//address of the symbol from table
							if(symbolTable.containsKey(sym)) {
								//get the actual address of the symbol
								int address = symbolTable.get(sym);
								
								//change E address
								loc = loc - (loc % 1000);
								loc = loc + address;
								System.out.println(mem_num + ": " + loc); 
								
								//update used arraylist to indicate that symbol was used from symbol table
								if(used.contains(sym)) { used.remove(sym); }
								//update use list used arraylist to indicate that symbol was used from use list
								if(use_list_used.contains(sym)) { use_list_used.remove(sym); }
								
							}
							else {
								//trying to use symbol not in table
								System.out.printf("%d: %d Error: %s, is not defined; zero used.\n",mem_num,loc,sym);
							}
						}
	
					}
					
					else if(type.equals("I")) {
						//just print it out
						System.out.println(mem_num + ": " + loc);
					}
					
					else {
						//in this case, need to check that in memory space
						int mem = loc % 1000;
						if (mem > 600) {
							System.out.print(mem_num + ": "  + (loc-mem));
							System.out.println(" Error: Absolute address exceeds machine size; zero used.");
						}
						else {
							System.out.println(mem_num + ": " + loc);
						}
					}
					
					mem_num++;
				}
				
				use_list.clear();
				globalloc += numprog;
			}
			
			//print out if defined symbols but didn't use
			for(String item : used) {
				System.out.printf("Warning: %s was defined but never used.\n",item);
			}
			for(String item : use_list_used) {
				System.out.printf("Warning: %s was in use list but never used.\n",item);
			}
		}
		
		else {
			System.out.println("Error: No file given. Exiting.");
			System.exit(0);
		}

	}
}
