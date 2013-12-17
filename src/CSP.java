import java.io.FileNotFoundException;
import java.util.Collection;


public interface CSP
{
	public boolean isComplete();

	public void assign(int variable, int value);
	public void unassign(int variable);

	public Collection<Integer> getConsidering(int variable);
	public Collection<Integer> getDomain();
	public int nextUnassignedVariable();
	
	public void setConfig(int num);
	
	public String toString();
	
	public boolean isValidFile(String filename) throws FileNotFoundException;
	public void readFile(String filename);
	
	public boolean writeRandom(String inputFile, String outputFile, int timelimit);


	boolean doubleCheck();
	boolean canAssign(int variable, int value);
}
