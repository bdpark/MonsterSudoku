import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



public class UnitTester 
{

	public static void main(String[] args) 
	{

		String inputFile = "randomInput.txt";
		String outputFile = "randomGrid.txt";
		//get lots of inputs
		Scanner scan = new Scanner(System.in);
		System.out.print("name of report to write to: ");
		String reportfilename =  scan.nextLine();
		System.out.print("# random grids to generate and solve for each value of M: ");
		int num = scan.nextInt();
		System.out.print("N: ");
		int N = scan.nextInt();
		System.out.print("p: ");
		int p = scan.nextInt();
		System.out.print("q: ");
		int q = scan.nextInt();
		System.out.print("generator time limit in min: ");
		int genMin = scan.nextInt();
		System.out.print("solver time limit in min: ");
		int minutes = scan.nextInt();
		System.out.print("start M: ");
		int startM = scan.nextInt();
		System.out.print("end M: " );
		int endM = scan.nextInt();
		System.out.print("start config: ");
		int startConfig = scan.nextInt();
		System.out.print("end config: " );
		int endConfig = scan.nextInt();
		
		int genLimit = genMin * 60 * 1000;//time limit for to randomly generate
		int timeLimit = minutes * 60 * 1000;//time limit to solve
		
		String bigReport = "";//what will be written out to the file in the end.
		for(int config = startConfig; config <= endConfig; config++)
		{
			String report = "CONFIG " + config + "\n";
			int hardestM = 1;
			double hardestMtime = 0.0;
			double hardestMsdev = 0.0;

			for(int M = startM; M <= endM; M++)
			{
				long sum = 0;//sum of the times for the current M
				int numSolved = 0;//# puzzles solved
				int timeouts = 0;//# times a puzzle timed out while trying to solve
				
				//stores the times to calculate the standard deviation later.
				ArrayList<Long> times = new ArrayList<Long>();
				
				for(int i = 0; i < num; i++)
				{
					write(inputFile, N, p, q, M);
					System.out.println("generating...");
					long genStartTime = System.currentTimeMillis();
					boolean g = generate(inputFile, outputFile, genLimit);
					long genEndTime = System.currentTimeMillis();
					long genTime = genEndTime - genStartTime;
					if(g)
					{
						System.out.println("M:" + M + " generated in " + genTime+"ms");
						CSP csp = new Grid();
						csp.setConfig(config);
						
						try {
							if(csp.isValidFile(outputFile))
								csp.readFile(outputFile);
							else
							{
								System.out.println("invalid file format");
								break;
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						System.out.println("solving...");

						NodeCount node = new NodeCount();

						long startTime = System.currentTimeMillis();
						long elapsedTime = 0L;
						Boolean a;
						if(config == 1)
							a = Solver.simpleBTS(csp, startTime, elapsedTime, timeLimit,node);
						else
							a = Solver.search(csp, startTime, elapsedTime, timeLimit,node);
						long solveEndTime = System.currentTimeMillis();
						long duration = solveEndTime - startTime;
						System.out.println("startTime: " + startTime);
						System.out.println("end time: " + solveEndTime);

						if(a != null)
							if(a && csp.doubleCheck())
							{
								numSolved++;
								sum += duration;
								System.out.println("Solved in " + duration + "ms");
								System.out.println(csp+"\n");
								times.add(duration);
							}
							else
								System.out.println("failed solve");		
						else
						{
							System.out.println("timed out");
							timeouts++;
						}
						if(timeouts >= 3)
							break;
					}
					else break;
				}

				double average = (1.0)*(sum) / numSolved;
				double variance = 0;
				for(long time : times)
					variance += Math.pow(time - average, 2);
				variance = variance / times.size();
				double sdev = Math.sqrt(variance);
				
				report += "\nFor M = " + M;
				report += " Solved "+numSolved+"/"+num+" in average time: "+average;
				report += " sdev: " + sdev;
				
				if(average > hardestMtime)
				{
					hardestMtime = average;
					hardestM = M;
					hardestMsdev = sdev;
				}	
			}
			
			
			report += "\n";
			report += "hardest M for config:" + config + " was " + hardestM;
			report += " with average time " + hardestMtime+"ms";
			report += " sdev: " + hardestMsdev;
			System.out.println(report);
			scan.close();
			bigReport += "\n" + report + "\n";
			
		}
		
		try
		{
			File file = new File(reportfilename);

			if (!file.exists()) 
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(bigReport);
			bw.close();
		}
		catch(IOException e)
		{
			System.out.println("idunno");
		}
	}
	private static void write(String inputFile, int N, int p, int q, int M)
	{

		String content = ""+N+" "+p+" "+q+" "+M;
		try
		{
			File file = new File(inputFile);

			if (!file.exists()) 
				file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}
		catch(IOException e)
		{
			System.out.println("idunno");
		}
	}
	private static boolean generate(String inputFile, String outputFile, int timeLimit)
	{
		CSP csp = new Grid();
		return csp.writeRandom(inputFile, outputFile, timeLimit);
	}

}
