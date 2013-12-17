import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


public class IntNode
{
	private int value;
	
	private HashMap<Integer, Integer> eliminated;
	private Collection<Integer> considering;
	
	public IntNode(int N)
	{
		this.value = 0;
		eliminated = new HashMap<Integer, Integer>();
		considering = new HashSet<Integer>();
		for(int i = 1; i <= N; i++)
			considering.add(i);
	}
	
	
	public int getValue()
	{
		return value;
	}
	public void assignValue(int value)
	{
		this.value = value;
	}
	public boolean isAssigned()
	{
		return value != 0;
	}
	
	public Collection<Integer> getConsidering()
	{
		return considering;
	}
	public boolean canEliminateConsidering(int value)
	{
		return !isAssigned() && getConsidering().contains(value);
	}
	public void addConsidering(int value)
	{
		considering.add(value);
	}
	public void removeConsidering(int value)
	{
		considering.remove(value);
	}
	public int numConsidering()
	{
		return considering.size();
	}
	
	public void addEliminated(int square, int value)
	{
		eliminated.put(square, value);
	}
	public HashMap<Integer, Integer> getEliminated()
	{
		return eliminated;
	}
	public void clearEliminated()
	{
		eliminated.clear();
	}
}
