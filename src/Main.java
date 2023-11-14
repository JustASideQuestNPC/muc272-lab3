import processing.core.PApplet;

public class Main extends PApplet {
  // nb: if something is a constant and should never be changed, it's convention to declare it as "static final" (unless
  // it can't be static, in which case just make it final) and format its name in SCREAMING_SNAKE_CASE
  public static final int WORLD_WIDTH = 1000;
  public static final int WORLD_HEIGHT = 600;
  public static final int BORDER_WALL_THICKNESS = 100;

  KEngine engine;

  // anything run from outside the processing editor has to call size() in settings() because...reasons?
  @Override
  public void settings() {
    size(1280, 720);
  }

  // all other setup stuff runs in setup() as normal
  @Override
  public void setup() {
    // set up inputs
    KInput.addInput("move up", new Key[]{Key.W, Key.UP});
    KInput.addInput("move down", new Key[]{Key.S, Key.DOWN});
    KInput.addInput("move left", new Key[]{Key.A, Key.LEFT});
    KInput.addInput("move right", new Key[]{Key.D, Key.RIGHT});

    // set up engine
    engine = new KEngine(this);
    engine.setCameraOffset(width / 2f, height / 2f);
    engine.setCameraPos(width / 2f, height / 2f);
    engine.setCameraTightness(0.01f);

    // add world border
    // top wall
    engine.addEntity(new Wall(-BORDER_WALL_THICKNESS, -BORDER_WALL_THICKNESS,
                              WORLD_WIDTH + BORDER_WALL_THICKNESS * 2, BORDER_WALL_THICKNESS));
    // bottom wall
    engine.addEntity(new Wall(-BORDER_WALL_THICKNESS, WORLD_HEIGHT,
                              WORLD_WIDTH + BORDER_WALL_THICKNESS * 2, BORDER_WALL_THICKNESS));
    // left wall
    engine.addEntity(new Wall(-BORDER_WALL_THICKNESS, 0, BORDER_WALL_THICKNESS, WORLD_HEIGHT));
    // right wall
    engine.addEntity(new Wall(WORLD_WIDTH, 0, BORDER_WALL_THICKNESS, WORLD_HEIGHT));

    engine.addEntity(new Player(width / 2f, height / 2f)); // add player
  }

  // draw runs once at the beginning of every frame
  @Override
  public void draw() {
    // update everything
    KInput.update();
    engine.update();

    background(255);
    engine.render();
  }

  // input listeners
  @Override
  public void keyPressed() {
    KInput.pressKey(keyCode);
  }
  @Override
  public void keyReleased() {
    KInput.releaseKey(keyCode);
  }
  @Override
  public void mousePressed() {
    KInput.pressMouse(mouseButton);
  }
  @Override
  public void mouseReleased() {
    KInput.releaseMouse(mouseButton);
  }

  // java boilerplate that runs settings() and setup(), then starts the draw() loop
  public static void main(String[] args) {
    PApplet.main("Main");
  }
}