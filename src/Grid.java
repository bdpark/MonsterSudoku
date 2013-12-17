import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;

//squares are ints 0-(N^2-1)
//values are ints 1-N
//values translated into strings when printing out
public class Grid implements CSP
{
	int p;//# of rows/block
	int q;//# of cols/block
	int N;//edge length of an NxN grid.

	private int numAssigned;//# variables currently assigned

	boolean fc, mrv, degree, lcv;
	boolean arcPre, arcPost;

	IntNode[][] grid;
	HashSet<Integer> unassigned;
	ArrayList<Integer> domain;

	//maps each variable to the set of the peers it can eliminate values from
	HashMap<Integer, HashSet<Integer>> peers;

	HashMap<String, Integer> inDomain;
	HashMap<Integer, String> outDomain;

	public Grid()
	{
	}
	public Grid(int p, int q)
	{
		this.p = p;
		this.q = q;
		this.N = p * q;
		initialize();
	}
	private void initialize()
	{
		numAssigned = 0;
		grid = new IntNode[N][N];
		unassigned = new HashSet<Integer>();
		peers = new HashMap<Integer, HashSet<Integer>>();
		domain = new ArrayList<Integer>();
		inDomain = new HashMap<String, Integer>();
		outDomain = new HashMap<Integer, String>();
		initGrid();
		initPeers();
		initDomain();
	}
	private void initDomain()
	{
		for(int i = 1; i <= N; i++)
			domain.add(i);

		for(int i = 0; i < symbols.length; i++)
		{
			inDomain.put(""+symbols[i], i+1);
			outDomain.put(i+1, ""+symbols[i]);

		}
		if(N > symbols.length)
			for(int i = symbols.length + 1; i < N; i++)
			{
				String symbol = generateSymbol(i);
				inDomain.put(symbol, i+1);
				outDomain.put(i+1, symbol);
			}
	}
	public Collection<Integer> getDomain()
	{
		return domain;
	}
	private final char[] symbols = 
		{'1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
			'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	//generate symbols to fill the board odometer style
	private String generateSymbol(int n)
	{
		if(n == 0)
			return "";
		if(n <= symbols.length)
			return ""+symbols[n - 1];
		else if(outDomain.get(n) != null)
			return outDomain.get(n);
		else
			if(n % symbols.length == 0)
				return generateSymbol(n / symbols.length - 1) + symbols[symbols.length-1];
			else
				return generateSymbol(n / symbols.length) + symbols[n % symbols.length - 1];
	}
	private void initGrid()
	{
		for(int row = 0; row < N; row++)
			for(int col = 0; col < N; col++)
				grid[row][col] = new IntNode(N);
	}
	private void initPeers()
	{
		for(int square = 0; square < N*N; square++)
		{
			peers.put(square, new HashSet<Integer>());

			int row = square / N;
			int col = square % N;

			int rowStart = row * N;
			int colStart = col;

			//add row and col peers
			for(int k = 0; k < N; k++)
			{
				peers.get(square).add((rowStart + k));
				peers.get(square).add(colStart + k * N);
			}

			//add block peers
			int blockRow = (row / p) * p;
			int blockCol = (col / q) * q;
			int blockStart = blockRow * N + blockCol;

			for(int k = 0; k < q; k++)
				for(int j = 0; j < p; j++)
				{
					int peer = blockStart + k + j*N;
					peers.get(square).add(peer);
				}

			peers.get(square).remove(square);
		}
	}
	private IntNode getSquare(int square)
	{
		return grid[rowOf(square)][colOf(square)];
	}
	private int rowOf(int square)
	{
		return square / N;
	}
	private int colOf(int square)
	{
		return square % N;
	}


	private void arcProcess(int variable, int value)
	{
		Queue<Integer> arcs = new LinkedList<Integer>(peers.get(variable));
		for(int peer: arcs)
		{
			if(getSquare(peer).canEliminateConsidering(value))
			{
				getSquare(peer).removeConsidering(value);
				getSquare(variable).addEliminated(peer, value);
			}
		}
		
		HashSet<Integer> alreadyArced = new HashSet<Integer>();
		while(!arcs.isEmpty())
		{
			int current = arcs.remove();
			value = getSquare(current).getValue();
			if(value != 0)
			{
				alreadyArced.add(current);
				for(int peer: peers.get(current))
				{
					if(getSquare(peer).canEliminateConsidering(value))
					{
						getSquare(peer).removeConsidering(value);
						getSquare(variable).addEliminated(peer, value);
					}
					if(getSquare(peer).numConsidering() == 1)
						if(!alreadyArced.contains(peer))
							arcs.add(peer);
				}
			}
		}
	}
	private void forwardCheck(int variable, int value)
	{
		if(value != 0)//dont forward check unassigned variables
			for(int peer: peers.get(variable))
				if(getSquare(peer).canEliminateConsidering(value))
				{
					getSquare(peer).removeConsidering(value);
					getSquare(variable).addEliminated(peer, value);
				}
	}


	public boolean isComplete() 
	{
		return numAssigned == N*N;
	}
	public void assign(int variable, int value) 
	{
		getSquare(variable).assignValue(value);
		numAssigned++;
		if(arcPost)
			arcProcess(variable, value);
		else if(fc)
			forwardCheck(variable, value);
		unassigned.remove(variable);
	}

	public void unassign(int variable) 
	{
		getSquare(variable).assignValue(0);
		numAssigned--;
		if(fc || arcPost)
			undoEliminated(variable);
		unassigned.add(variable);
	}
	private void undoEliminated(int variable)
	{
		IntNode current = getSquare(variable);
		HashMap<Integer, Integer> eliminated = current.getEliminated();
		for(int square: eliminated.keySet())
			if(!getSquare(square).isAssigned())
			{
				int value = eliminated.get(square);
				getSquare(square).addConsidering(value);
			}
		current.clearEliminated();
	}
	public Collection<Integer> getConsidering(final int variable) 
	{
		if(lcv)
		{
			TreeSet<Integer> lcvSet = new TreeSet<Integer>(new Comparator<Integer>(){
				public int compare(Integer value1, Integer value2) 
				{
					if(numConstrained(variable, value1) == numConstrained(variable, value2))
						return 1;
					else if(numConstrained(variable, value1) < numConstrained(variable, value2))
						return -1;
					else
						return 1;
				}});
			
			for(int value: getSquare(variable).getConsidering())
				lcvSet.add(value);
			return lcvSet;
		}
		else
			return getSquare(variable).getConsidering();
	}
	private int numConstrained(int variable, int value)
	{
		int constrained = 0;
		for(int peer: peers.get(variable))
			if(getSquare(peer).isAssigned() && getSquare(peer).getValue() == value)
				constrained++;
		return constrained;
	}
	public int nextUnassignedVariable() 
	{
		if(config1() || config2())
			if(unassigned.size() > 0)
				return unassigned.iterator().next();
			else
				return -1;
		else
			return minRemainingVariable();
	}
	private int minRemainingVariable()
	{
		ArrayList<Integer> variables = new ArrayList<Integer>();
		int min = N;
		for(int available: unassigned)
		{
			if(getSquare(available).numConsidering() == 0)
				return -1;
			else
			{
				if(getSquare(available).numConsidering() < min)
				{
					min = getSquare(available).numConsidering();
					variables.clear();
					variables.add(available);
				}
				else if(getSquare(available).numConsidering() == min)
					variables.add(available);
			}
		}

		if(degree)
			return highestDegreeVariable(variables);
		else
			return variables.get(0);
	}
	private int highestDegreeVariable(ArrayList<Integer> vars)
	{
		int highestDegree = 0;
		int out = vars.get(0);
		for(int variable: vars)
		{
			int degree = degreeOf(variable);
			if(degree > highestDegree)
			{
				highestDegree = degree;
				out = variable;
			}
		}
		return out;
	}
	private int degreeOf(int variable)
	{
		int degree = 0;
		for(int peer: peers.get(variable))
			if(!getSquare(peer).isAssigned())
				degree++;
		return degree;
	}
	private boolean config1()
	{
		return !fc && !mrv && !degree && !lcv && !arcPre && !arcPost;
	}
	private boolean config2()
	{
		return fc && !mrv && !degree && !lcv && !arcPre && !arcPost;
	}
	public void setConfig(int num)
	{
		if(num == 1)
		{
			fc = false;
			setHeuristics(false, false, false);
			setArc(false, false);
		}	
		else if(num == 2)
		{
			fc = true;
			setHeuristics(false, false, false);
			setArc(false, false);
		}
		else if(num == 3)
		{
			fc = true;
			setHeuristics(true, false, false);
			setArc(false, false);
		}
		else if(num == 4)
		{
			fc = true;
			setHeuristics(true, true, false);
			setArc(false, false);
		}
		else if(num == 5)
		{
			fc = true;
			setHeuristics(true, true, true);
			setArc(false, false);
		}
		else if(num == 6)
		{
			fc = true;
			setHeuristics(true, true, true);
			setArc(true, false);
		}
		else if(num == 7)
		{
			fc = false;
			setHeuristics(true, true, true);
			setArc(true, true);
		}
	}
	private void setHeuristics(boolean mrv, boolean degree, boolean lcv) 
	{
		this.mrv = mrv;
		this.degree = degree;
		this.lcv = lcv;
	}
	private void setArc(boolean pre, boolean post)
	{
		arcPre = pre;
		arcPost = post;
	}
	
	
	
	public boolean isValidFile(String filename) throws FileNotFoundException
	{
		return validParams(filename);
	}
	public void readFile(String filename) 
	{
		initialize();
		readGrid(filename);
	}
	private void readGrid(String filename)
	{
		File file = new File(filename);
		try 
		{
			Scanner scan = new Scanner(file);
			scan.nextLine();//skip first line
			int square = 0;
			for(int i = 0; i < N; i++)
			{
				StringTokenizer tokens = new StringTokenizer(scan.nextLine());
				for(int k = 0; k < N; k++)
				{
					String tokenvalue = tokens.nextToken();
					int value = gridValue(tokenvalue);
					getSquare(square).assignValue(value);
					if(value == 0)
						unassigned.add(square);
					else
					{
						if(arcPre)
							arcProcess(square, value);
						else
							forwardCheck(square, value);
						numAssigned++;
					}
					square++;
				}
			}
			scan.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	private int gridValue(String value)
	{
		if(value.equals("0"))
			return 0;
		return inDomain.get(value);
	}
	private String printValue(int value)
	{
		if(value == 0)
			return "0";
		return outDomain.get(value);
	}
	private boolean validParams(String filename) throws FileNotFoundException
	{
		File file = new File(filename);


		Scanner scan = new Scanner(file);
		String line = null;
		if(scan.hasNextLine())
		{
			line = scan.nextLine();
			StringTokenizer tokens = new StringTokenizer(line, " ");
			scan.close();
			if(tokens.countTokens() == 3)
			{
				int N = Integer.parseInt(tokens.nextToken());
				int p = Integer.parseInt(tokens.nextToken());
				int q = Integer.parseInt(tokens.nextToken());
				if(N == p*q)
				{
					this.p = p;
					this.q = q;
					this.N = this.p * this.q;
					return true;
				}
				else
				{
					return false;
				}

			}
			else
			{
				return false;
			}
		}
		else
		{
			scan.close();
			return false;
		}
	}
	public boolean doubleCheck() 
	{
		for(int i = 0; i < N*N; i++)
			if(!canAssign(i, getSquare(i).getValue()))
				return false;
		return true;
	}
	public boolean canAssign(int variable, int value) 
	{
		for(int peer: peers.get(variable))
			if(getSquare(peer).isAssigned() && getSquare(peer).getValue() == value)
				return false;
		return true;
	}

	public String toString()
	{
		String out = "";

		int longestSymbol = outDomain.get(outDomain.size()).length();
		String padding = "";
		for(int i = 0; i < longestSymbol; i++)
			padding += " ";

		for(int row = 0; row < N; row++)
		{
			for(int col = 0; col < N; col++)
			{
				IntNode outNode = grid[row][col];
				String outValue = printValue(outNode.getValue());
				out += outValue;
				if(col < N-1)
					out += padding;
			}
			if(row < N-1)
				out += "\n";
		}	
		return out;
	}

	public boolean writeRandom(String inputFile, String outputFile, int timelimit)
	{
		File file = new File(inputFile);
		int M;
		Scanner scan;
		try 
		{
			scan = new Scanner(file);
			StringTokenizer params = new StringTokenizer(scan.nextLine(), " ");
			this.N = Integer.parseInt(params.nextToken());
			this.p = Integer.parseInt(params.nextToken());
			this.q = Integer.parseInt(params.nextToken());
			M = Integer.parseInt(params.nextToken());
			initialize();
			scan.close();
			if(randomizeGrid(M, timelimit))
			{
				writeGrid(outputFile);
				return true;
			}
			
			return false;
				
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		return false;
	}
	private void writeGrid(String outputFile)
	{
		try
		{

			String content = ""+N+" "+p+" "+q+"\n"+toString();

			File file = new File(outputFile);

			if (!file.exists()) 
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			System.out.println(content);
			System.out.println("written to " + outputFile);
			bw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	private boolean randomizeGrid(int M, int timelimit)
	{
		int count = 0;
		Random rand = new Random();
		fc = true;
		for(int i = 0; i < N*N; i++)
			unassigned.add(i);
		long startTime = System.currentTimeMillis();
		while(count < M)
		{
			int randVariable = rand.nextInt(N*N);
			if(!getSquare(randVariable).isAssigned())
			{
				ArrayList<Integer> considering = new ArrayList<Integer>(getSquare(randVariable).getConsidering());
				Collections.shuffle(considering);
				int randValue = considering.get(0);
				assign(randVariable, randValue);
				count++;
			}
			
			long elapsedTime = (new Date()).getTime() - startTime;
			if(elapsedTime >= timelimit)
				return false;
			
			for(int available: unassigned)
			{
				if(getSquare(available).numConsidering() == 0)
				{
					startOver();
					count = 0;
					break;
				}
			}
		}
		return true;
	}
	private void startOver()
	{
		initGrid();
		unassigned = new HashSet<Integer>();
		for(int i = 0; i < N*N; i++)
			unassigned.add(i);
	}








}
