import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Main extends PApplet {
  // nb: if something is a constant and will never be changed, declare it as "static final" (unless it can't be static,
  // in which case just make it "final")
  public static final int GRID_WIDTH = 32;
  public static final int GRID_HEIGHT = 18;
  public static final int NODE_SIZE = 30;
  public static final int[][] BLOCKS = {
      { 4, 3, 5, 15},
      {23, 0, 5, 15},
      {12, 6, 6,  6}
  };
  public static final PVector START = new PVector(1, 8);
  public static final PVector END = new PVector(31, 9);
  public ArrayList<PVector> path = new ArrayList<>();
  public NavMesh navMesh;

  // anything run from outside the processing editor has to call size() in settings() because...reasons?
  public void settings() {
    size(GRID_WIDTH * NODE_SIZE, GRID_HEIGHT * NODE_SIZE);
  }

  // all other setup stuff runs in setup() as normal
  public void setup() {
    navMesh = new NavMesh(GRID_WIDTH, GRID_HEIGHT);
    for (int[] block : BLOCKS) {
      for (int x = 0; x < block[2]; ++x) {
        for (int y = 0; y < block[3]; ++y) {
          navMesh.setNodeState(x + block[0], y + block[1], true);
        }
      }
    }

    path = navMesh.getPath(START, END);
  }

  // draw runs once at the beginning of every frame
  public void draw() {
    background(255);

    // draw blocks
    noStroke();
    fill(0xff000000);
    for (int[] block : BLOCKS) {
      rect(block[0] * NODE_SIZE, block[1] * NODE_SIZE, block[2] * NODE_SIZE, block[3] * NODE_SIZE);
    }

    // draw all nodes
    for (int x = 0; x < GRID_WIDTH; ++x) {
      for (int y = 0; y < GRID_HEIGHT; ++y) {
        if (navMesh.getNodeState(x, y)) fill(0xffff0000);
        else fill(0xff00ff00);
        ellipse(x * NODE_SIZE + NODE_SIZE / 2F, y * NODE_SIZE + NODE_SIZE / 2F,
                NODE_SIZE / 2F, NODE_SIZE /  2F);
      }
    }

    // draw path
    fill(0xff0000ff);
    for (int i = 0; i < path.size(); ++i) {
      noStroke();
      ellipse(path.get(i).x * NODE_SIZE + NODE_SIZE / 2F, path.get(i).y * NODE_SIZE + NODE_SIZE / 2F,
              NODE_SIZE / 2F, NODE_SIZE / 2F);
      if (i < path.size() - 1) {
        stroke(0xff0000ff);
        strokeWeight(3);
        line(path.get(i).x * NODE_SIZE + NODE_SIZE / 2F, path.get(i).y * NODE_SIZE + NODE_SIZE / 2F,
             path.get(i + 1).x * NODE_SIZE + NODE_SIZE / 2F, path.get(i + 1).y * NODE_SIZE + NODE_SIZE / 2F);
      }
    }
  }

  // java boilerplate that runs settings() and setup(), then starts the draw() loop
  public static void main(String[] args) {
    PApplet.main("Main");
  }
}