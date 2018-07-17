package mouserun.mouse;
import mouserun.game.*;
import java.util.*;

public class TheExplorer
	extends Mouse
{
	private HashMap<String, Grid> grids;
	private LinkedList<Integer> rushMoves;
	private boolean isDiscovering;

	public TheExplorer()
	{
		super("The Explorer");
		
		grids = new HashMap<String, Grid>();
		rushMoves = new LinkedList<Integer>();
		isDiscovering = true;
	}
	
	public int move(Grid currentGrid, Cheese cheese)
	{
		registerGrid(currentGrid);
		
		if (isDiscovering)
		{
			if (cheeseOnGrid(cheese))
			{
				// find shortest path to cheese
				Grid cheeseGrid = getGrid(cheese.getX(), cheese.getY());
				createPath(currentGrid, cheeseGrid);
				
				if (rushMoves.size() > 0)
				{
					isDiscovering = false;
					return rushMoves.poll();
				}
				else
				{
					Grid newDestination = findClosestGridWithUnvisitedNeighbour(currentGrid.getX(), currentGrid.getY());
					createPath(currentGrid, newDestination);
					
					if (rushMoves.size() > 0)
					{
						isDiscovering = false;
						return rushMoves.poll();
					}
					else
					{
						return Mouse.BOMB;
					}
				}
				
			}
			else
			{
				
				int nextMove = getNextDirection(currentGrid, cheese);
				if (nextMove != -100)
				{
					
					return nextMove;
				}
				else
				{
					// back track to any grid that is closest to the current grid that has neighbours
					Grid destination = findClosestGridWithUnvisitedNeighbour(currentGrid.getX(), currentGrid.getY());
					if (destination == null) destination = findClosestGrid(cheese.getX(), cheese.getY());
					createPath(currentGrid, destination);
					
					if (rushMoves.size() > 0)
					{
						
						isDiscovering = false;
						return rushMoves.poll();
					}
					else
					{
						
						return Mouse.BOMB;
					}
				}
			}
		}
		else
		{
			if (rushMoves.size() > 0)
			{
				
				return rushMoves.poll();
			}
			else
			{
				
				isDiscovering = true;
				return Mouse.BOMB;
			}
		}
	}
	
	public void newCheese()
	{
		rushMoves.clear();
		isDiscovering = true;
	}
	
	public void respawned()
	{
		rushMoves.clear();
		isDiscovering = true;
		grids.clear();
	}
	
	private Grid findClosestGrid(int x, int y)
	{
		Grid bestGrid = null;
		double closestDistance = Double.MAX_VALUE;
		double distance = 0;
		
		for (Grid g : grids.values())
		{
			distance = getDistance(x, y, g.getX(), g.getY());
			if (distance < closestDistance)
			{
				closestDistance = distance;
				bestGrid = g;
			}
		}
		
		return bestGrid;
	}
	
	private Grid findClosestGridWithUnvisitedNeighbour(int x, int y)
	{
		Grid bestGrid = null;
		double closestDistance = Double.MAX_VALUE;
		double distance = 0;
		
		for (Grid g : grids.values())
		{
			if (hasUnvisitedNeighbours(g))
			{
				distance = getDistance(x, y, g.getX(), g.getY());
				if (distance < closestDistance)
				{
					closestDistance = distance;
					bestGrid = g;
				}
			}
		}
		
		return bestGrid;
		
	}
	
	private void createPath(Grid currentGrid, Grid destGrid)
	{
		Stack<Grid> path = new Stack<Grid>();
		findPath(currentGrid, destGrid, path);
		
		ArrayList<Grid> results = new ArrayList<Grid>();
		while (path.size() > 0)
		{
			results.add(path.pop());
		}
		
		Collections.reverse(results);
		LinkedList<Integer> moves = new LinkedList<Integer>();
		
		if (results.size() > 0)
		{
			Grid refGrid = results.get(0);
			results.remove(refGrid);
			
			for (Grid cGrid : results)
			{
				int x = cGrid.getX() - refGrid.getX();
				int y = cGrid.getY() - refGrid.getY();
				
				if (x == 1)
				{
					moves.add(Mouse.RIGHT);
				}
				else if (x == -1)
				{
					moves.add(Mouse.LEFT);
				}
				else if (y == 1)
				{
					moves.add(Mouse.UP);
				}
				else if (y == -1)
				{
					moves.add(Mouse.DOWN);
				}
				
				refGrid = cGrid;
			}
			
			rushMoves = moves;
		}
	}
	
	private boolean findPath(Grid currentGrid, Grid destGrid, Stack<Grid> path)
	{
		path.push(currentGrid);
		if (currentGrid == destGrid)
		{
			return true;
		}
	
		ArrayList<Grid> possibleMoves = new ArrayList<Grid>();
		
		if (currentGrid.canGoLeft())
		{
			Grid grid = leftGrid(currentGrid);
			if (grid != null)
			{
				if (!path.contains(grid))
				{
					possibleMoves.add(grid);
				}
			}
		}
		
		if (currentGrid.canGoRight())
		{
			Grid grid = rightGrid(currentGrid);
			if (grid != null)
			{
				if (!path.contains(grid))
				{
					possibleMoves.add(grid);
				}
			}
		}
		
		if (currentGrid.canGoUp())
		{
			Grid grid = upGrid(currentGrid);
			if (grid != null)
			{
				if (!path.contains(grid))
				{
					possibleMoves.add(grid);
				}
			}
		}
		
		if (currentGrid.canGoDown())
		{
			Grid grid = downGrid(currentGrid);
			if (grid != null)
			{
				if (!path.contains(grid))
				{
					possibleMoves.add(grid);
				}
			}
		}
		
		
		while (possibleMoves.size() > 0)
		{
			Grid bestGrid = null;
			double closestDistance = Double.MAX_VALUE;
			double distance = 0;
			
			for (Grid g : possibleMoves)
			{
				distance = getDistance(g.getX(), g.getY(), destGrid.getX(), destGrid.getY());
				if (distance < closestDistance)
				{
					closestDistance = distance;
					bestGrid = g;
				}
			}
			
			possibleMoves.remove(bestGrid);
			if (findPath(bestGrid, destGrid, path))
			{
				return true;
			}
		}
		
		path.pop();
		return false;
	}
	
	private boolean cheeseOnGrid(Cheese cheese)
	{
		return getGrid(cheese.getX(), cheese.getY()) != null;
	}
	
	private int getNextDirection(Grid grid, Cheese cheese)
	{
		ArrayList<Integer> possibleMoves = new ArrayList<Integer>();
		double closestDistance = Double.MAX_VALUE;
		double distance = 0;
		
		int cX = cheese.getX();
		int cY = cheese.getY();
					
		if (grid.canGoUp())
		{
			Grid up = upGrid(grid);
			if (up == null)
			{
				distance = getDistance(grid.getX(), grid.getY() + 1, cX, cY);
				if (distance < closestDistance)
				{
					possibleMoves.clear();
					possibleMoves.add(Mouse.UP);
					closestDistance = distance;
				}
				else if (distance == closestDistance)
				{
					possibleMoves.add(Mouse.UP);
				}
			}
		}
		
		if (grid.canGoDown())
		{
			Grid down = downGrid(grid);
			
			if (down == null)
			{
				distance = getDistance(grid.getX(), grid.getY() - 1, cX, cY);
				if (distance < closestDistance)
				{
					possibleMoves.clear();
					possibleMoves.add(Mouse.DOWN);
					closestDistance = distance;
				}
				else if (distance == closestDistance)
				{
					possibleMoves.add(Mouse.DOWN);
				}
			}
		}
		
		if (grid.canGoLeft())
		{
			Grid left = leftGrid(grid);
			
			if (left == null)
			{
				distance = getDistance(grid.getX() - 1, grid.getY(), cX, cY);
				if (distance < closestDistance)
				{
					possibleMoves.clear();
					possibleMoves.add(Mouse.LEFT);
					closestDistance = distance;
				}
				else if (distance == closestDistance)
				{
					possibleMoves.add(Mouse.LEFT);
				}
			}
		}
		
		
		if (grid.canGoRight())
		{
			Grid right = rightGrid(grid);
			
			if (right == null)
			{
				distance = getDistance(grid.getX() + 1, grid.getY(), cX, cY);
				if (distance < closestDistance)
				{
					possibleMoves.clear();
					possibleMoves.add(Mouse.RIGHT);
					closestDistance = distance;
				}
				else if (distance == closestDistance)
				{
					possibleMoves.add(Mouse.RIGHT);
				}
			}
		}
			
		if (possibleMoves.size() == 0)
		{
			return -100;
		}
		
		Random random = new Random();
		return possibleMoves.get(random.nextInt(possibleMoves.size()));
		
	}
	
	private boolean hasUnvisitedNeighbours(Grid grid)
	{
		if (grid.canGoLeft()) if (leftGrid(grid) == null) return true;
		if (grid.canGoRight()) if (rightGrid(grid) == null) return true;
		if (grid.canGoUp()) if (upGrid(grid) == null) return true;
		if (grid.canGoDown()) if (downGrid(grid) == null) return true;
		
		return false;
	}
	
	private Grid leftGrid(Grid grid)
	{
		return getGrid(grid.getX() - 1, grid.getY());
	}
	
	private Grid rightGrid(Grid grid)
	{
		return getGrid(grid.getX() + 1, grid.getY());
	}
	
	private Grid upGrid(Grid grid)
	{
		return getGrid(grid.getX(), grid.getY() + 1);
	}
	
	private Grid downGrid(Grid grid)
	{
		return getGrid(grid.getX(), grid.getY() - 1);
	}
	
	private Grid getGrid(int x, int y)
	{
		String key = getGridKey(x, y);
		if (grids.containsKey(key))
		{
			
			return grids.get(key);
		}
	
		return null;
	}	
	
	private void registerGrid(Grid grid)
	{
		if (!grids.containsValue(grid))
		{
			grids.put(getGridKey(grid), grid);
		}
	}
	
	private String getGridKey(Grid grid)
	{
		return getGridKey(grid.getX(), grid.getY());
	}
	
	private String getGridKey(int x, int y)
	{
		return x + "," + y;
	}
	
	private double getDistance(int x1, int y1, int x2, int y2)
	{
		return Math.sqrt(Math.pow((double)(x1 - x2), 2) + Math.pow((double)(y1 - y2), 2));
	}

}