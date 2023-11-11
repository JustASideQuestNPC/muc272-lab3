import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.pow;

/* does pathfinding between arbitrary points on a grid (with some limitations) */
public class NavMesh {
  // state of all nodes avaliable for pathfinding
  private final boolean[][] nodeGraph;
  private final int gridWidth, gridHeight;

  // used in the pathfinding algorithm
  private static final PVector[] offsets = {
      new PVector( 0, -1),
      new PVector( 1, -1),
      new PVector( 1,  0),
      new PVector( 1,  1),
      new PVector( 0,  1),
      new PVector(-1,  1),
      new PVector(-1,  0),
      new PVector(-1, -1)
  };

  // initializes node graph to the specified width and height and fills it with unoccupied nodes. nodeSize is how large
  // each grid square is, and is required for pathfinding between arbitrary points.
  NavMesh(int width, int height) {
    nodeGraph = new boolean[width][height];
    gridWidth = width;
    gridHeight = height;
  }

  // constructs a path between start and end. mainly uses the a* algorithm, with a few modifications to create a shorter
  // node list. returns an empty list if end is unreachable
  public ArrayList<PVector> getPath(PVector start, PVector end) {
    // initialize open and closed lists
    ArrayList<PathNode> openList = new ArrayList<>();
    ArrayList<PathNode> closedList = new ArrayList<>();

    // create start and end nodes
    PathNode startNode = new PathNode(null, start);
    startNode.g = 0;
    startNode.h = 0;
    startNode.f = 0;

    PathNode endNode = new PathNode(null, end);
    endNode.g = 0;
    endNode.h = 0;
    endNode.f = 0;

    // add start node to the open list
    openList.add(startNode);

    // initialie currentNode to a dummy node to appease the compiler
    PathNode currentNode = new PathNode(null, new PVector(0, 0));

    // loop until a path is found or all reachable nodes have been searched
    boolean pathFound = false;
    while (!openList.isEmpty()) {
      // set the current node to the node in the open list with the smallest cost (f)
      currentNode = null;
      float lowestCost = Float.POSITIVE_INFINITY;
      int currentIndex = 0;
      for (int i = 0; i < openList.size(); ++i) {
        if (openList.get(i).f < lowestCost) {
          currentNode = openList.get(i);
          lowestCost = currentNode.f;
          currentIndex = i;
        }
      }

      // remove the curent node from the open list and add it to the closed list
      openList.remove(currentIndex);
      closedList.add(currentNode);

      // if the current node is the end node, the goal has been found
      assert currentNode != null;
      if (currentNode.equals(endNode)) {
        pathFound = true;
        break;
      }

      // generate child nodes
      ArrayList<PathNode> childNodes = new ArrayList<>();
      for (PVector offset : offsets) {
        // get position in the node graph
        PVector nodePos = PVector.add(currentNode.position, offset);

        // ensure the node is inside the graph
        if (nodePos.x < 0 || nodePos.x >= gridWidth || nodePos.y < 0 || nodePos.y >= gridHeight) continue;
        // ensure the node is accessible
        if (nodeGraph[(int)nodePos.x][(int)nodePos.y]) continue;

        // add a new node to the child list
        childNodes.add(new PathNode(currentNode, nodePos));
      }

      // loop through children and add valid ones to the open list
      for (PathNode child : childNodes) {
        // skip the node if it is already on the closed list
        if (closedList.contains(child)) continue;

        // calculate g, h, and f (don't ask me why they're called that, i have no idea)
        // distance from the start node
        child.g = currentNode.g + 1;
        // estimated distance from the end node
        child.h = (float)(pow(child.position.x - currentNode.position.x, 2) +
                  pow(child.position.y - currentNode.position.y, 2));
        // total "cost" of the node
        child.f = child.g + child.h;

        // if the child is already in the open list with a lower g-value, don't add it
        boolean nodeIsDuplicate = false;
        for (PathNode n : openList) {
          if (n.equals(child) && child.g >= n.g) {
            nodeIsDuplicate = true;
            break;
          }
        }
        if (nodeIsDuplicate) continue;
        openList.add(child);
      }
    }

    // if the end is unreachable, return an empty list
    if (!pathFound) return new ArrayList<>();

    // otherwise, loop backwards from the current node (which is the goal) and find the path from the end to the start
    ArrayList<PVector> pathRaw = new ArrayList<>();
    while (currentNode != null) {
      pathRaw.add(currentNode.position);
      currentNode = currentNode.parent;
    }

    // reverse to get the path from the start to the end
    Collections.reverse(pathRaw);

    // convert the raw path (which contains all nodes) into a path containing only nodes at the corners of the path
    ArrayList<PVector> path = new ArrayList<>();
    path.add(start);
    PVector last = start;
    PVector current = start;
    float slope = (current.y - last.y) / (current.x - last.x);
    for (int i = 1; i < pathRaw.size(); ++i) {
      last = current;
      current = pathRaw.get(i);
      float newSlope = (current.y - last.y) / (current.x - last.x);
      if (newSlope != slope) {
        slope = newSlope;
        path.add(last);
      }
    }
    path.add(end);

    return path;
  }

  // sets whether a node is occupied. has no effect until the node graph is rebuilt!
  public void setNodeState(int x, int y, boolean state) {
    nodeGraph[x][y]= state;
  }

  // returns whether a node is occupied
  public boolean getNodeState(int x, int y) {
    return nodeGraph[x][y];
  }

  // used for a* pathfinding
  private static class PathNode {
    public PathNode parent;
    public PVector position;
    public float g, h, f;

    PathNode(PathNode parent, PVector position) {
      this.parent = parent;
      this.position = position;
    }

    // imagine not having operator overloading *laughs in c++ supremacy*
    @Override
    public boolean equals(Object other) {
      if (other == null) return false;
      if (other == this) return true;
      if (!(other instanceof PathNode node)) return false;
      return this.position.equals(node.position);
    }
  }
}