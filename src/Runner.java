import java.util.Scanner;

//Brandon Park
//94741191
public class Runner
{


	public static void main(String[] args) 
	{
		Scanner scan = new Scanner(System.in);
		Parser parser = new Parser(scan);
		String prompt = "enter 's' or 'solve' to run the solver";
		prompt += "\nenter 'r' or 'random' to test the generator";
		prompt += "\n'q' or 'quit' to exit";
		for(;;)
		{
			System.out.println(prompt);
			String command = scan.nextLine().toLowerCase();
			if(command.equals("q") || command.equals("quit"))
			{
				System.out.println("GOODBYE");
				break;
			}
			parser.executeCommand(command);
		}
		scan.close();
	}
}
