import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.PriorityQueue;

public class Parser 
{
	Scanner scanner;
	public Parser(Scanner scanner)
	{
		this.scanner = scanner;
	}
	public void executeCommand(String command)
	{
		if(command.equals("s") || command.equals("solve"))
			runSolver();
		else if(command.equals("r") || command.equals("random"))
			runGenerator();
		else
			System.out.println("invalid command");
	}
	private void runGenerator()
	{
		System.out.print("input file name: ");
		String inputFile = scanner.nextLine();
		try
		{
			if(isValidRandomFile(inputFile))
			{
				System.out.print("output file name: ");
				String outputFile = scanner.nextLine();
				System.out.print("time limit in min for generating grid: ");
				int min = scanner.nextInt();
				int timeLimit = min * 60 * 1000;
				CSP csp = new Grid();
				if(!csp.writeRandom(inputFile, outputFile, timeLimit))
					System.out.println("timed out trying to generate grid");
				scanner.nextLine();
			}
			else
				System.out.println("file did not have valid parameters");
		}
		catch(FileNotFoundException e)
		{
			System.out.println("invalid filename");
		}
	}
	private void runSolver()
	{
		System.out.print("input filename: ");
		String filename = scanner.nextLine();
		CSP csp = new Grid();
		try
		{
			if(csp.isValidFile(filename))
			{	
				System.out.print("output filename: " );
				String outputfile = scanner.nextLine();
				System.out.print("config: ");
				int config = scanner.nextInt();
				System.out.print("time limit in minutes: " );
				int minutes = scanner.nextInt();
				
				long totalTimestart = System.currentTimeMillis();
				csp.setConfig(config);
				csp.readFile(filename);

				NodeCount node = new NodeCount();
				int timeLimit = minutes * 60 * 1000;
				long startTime = System.currentTimeMillis();
				long elapsedTime = 0L;
				Boolean a;
				long solveStartTime = System.currentTimeMillis();
				if(config == 1)
					a = Solver.simpleBTS(csp, startTime, elapsedTime, timeLimit,node);
				else
					a = Solver.search(csp, startTime, elapsedTime, timeLimit,node);
				long solveEndTime = System.currentTimeMillis();
				long duration = solveEndTime - solveStartTime;
				long totalTime = System.currentTimeMillis() - totalTimestart;
				long initTime = totalTime - duration;
				System.out.println("Preprocessing time: " + initTime + "ms");
				
				if(a == null)
				{
					System.out.println("Search Time: " + timeLimit + "ms");
					System.out.println("Assignments: " + node.count);
					System.out.println("Solution: No");
					System.out.println("Timeout: Yes");
				}
				else
					if(a)
					{
						if(csp.doubleCheck())
						{
							System.out.println("Search Time: " + duration + "ms");
							System.out.println("Assignments: " + node.count);
							System.out.println("Solution: Yes");
							System.out.println("Timeout: No");
							writeGrid(csp, outputfile);
						}
						else
						{
							System.out.println("Search Time: " + duration + "ms");
							System.out.println("Assignments: " + node.count);
							System.out.println("Solution: No");
							System.out.println("Timeout: No");
						}
					}
					else
					{
						System.out.println("Search Time:" + duration + "ms");
						System.out.println("Assignments: " + node.count);
						System.out.println("Solution: No");
						System.out.println("Timeout: No");
					}

				scanner.nextLine();
			}
			else
				System.out.println("invalid file format");
		}
		catch(FileNotFoundException e)
		{
			System.out.println("invalid filename");
		}
	}
	private boolean isValidRandomFile(String filename) throws FileNotFoundException
	{
		File file = new File(filename);
		Scanner scan = new Scanner(file);
		StringTokenizer params;
		if(scan.hasNextLine())
		{
			params = new StringTokenizer(scan.nextLine(), " ");
			if(params.countTokens() == 4)
			{
				int N = Integer.parseInt(params.nextToken());
				int p = Integer.parseInt(params.nextToken());
				int q = Integer.parseInt(params.nextToken());
				int M = Integer.parseInt(params.nextToken());
				if(N == p*q && M < N*N)
				{
					scan.close();
					return true;
				}
			}
		}
		scan.close();
		return false;
	}
	private void writeGrid(CSP csp, String filename)
	{
		try
		{
			File file = new File(filename);

			if (!file.exists()) 
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(""+csp);
			bw.close();
		}
		catch(IOException e)
		{
			System.out.println("idunno");
		}
	}
}
