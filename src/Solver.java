import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Solver 
{

	public static Boolean simpleBTS(CSP csp, long startTime, long elapsedTime, int timeLimit, NodeCount node)
	{
		if(elapsedTime >= timeLimit)//timeout
			return null;
		elapsedTime = (new Date()).getTime() - startTime;

		if(csp.isComplete())
			return true;
		
		int variable = csp.nextUnassignedVariable();
		if(variable == -1)
			return false;
		
		Collection<Integer> considering = csp.getDomain();
		for(int possibleValue: considering)
		{
			if(csp.canAssign(variable, possibleValue))
			{
				csp.assign(variable, possibleValue);
				node.count++;
				
				Boolean solved = simpleBTS(csp, startTime, elapsedTime, timeLimit, node);

				if(solved == null)
					return null;
				if(solved)
					return true;
				
				csp.unassign(variable);
			}
		}
		return false;
	}
	public static Boolean search(CSP csp,  long startTime, long elapsedTime, int timeLimit, NodeCount node)
	{
		if(elapsedTime >= timeLimit)//timeout
			return null;
		elapsedTime = (new Date()).getTime() - startTime;
		
		if(csp.isComplete())
			return true;
		
		int variable = csp.nextUnassignedVariable();
		if(variable == -1)
			return false;

		Collection<Integer> considering = csp.getConsidering(variable);
		for(int possibleValue: considering)
		{
			csp.assign(variable, possibleValue);
			node.count++;

			Boolean solved = search(csp, startTime, elapsedTime, timeLimit, node);

			if(solved == null)
				return null;
			if(solved)
				return true;

			csp.unassign(variable);
		}
		return false;
	}

}
