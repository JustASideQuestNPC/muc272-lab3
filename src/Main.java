import processing.core.PApplet;

public class Main extends PApplet {
  KEngine engine;

  // anything run from outside the processing editor has to call size() in settings() because...reasons?
  @Override public void settings() {
    size(1280, 720);
  }

  // all other setup stuff runs in setup() as normal
  @Override public void setup() {
    // set up inputs
    KInput.addInput("move up", new Key[]{Key.W, Key.UP});
    KInput.addInput("move down", new Key[]{Key.S, Key.DOWN});
    KInput.addInput("move left", new Key[]{Key.A, Key.LEFT});
    KInput.addInput("move right", new Key[]{Key.D, Key.RIGHT});

    // set up engine
    engine = new KEngine(this);
    engine.addEntity(new Player(width / 2f, height / 2f));
  }

  // draw runs once at the beginning of every frame
  @Override public void draw() {
    // update everything
    KInput.update();
    engine.update();

    background(255);
    engine.render();
  }

  // input listeners
  @Override public void keyPressed() {
    KInput.pressKey(keyCode);
  }
  @Override public void keyReleased() {
    KInput.releaseKey(keyCode);
  }
  @Override public void mousePressed() {
    KInput.pressMouse(mouseButton);
  }
  @Override public void mouseReleased() {
    KInput.releaseMouse(mouseButton);
  }

  // java boilerplate that runs settings() and setup(), then starts the draw() loop
  public static void main(String[] args) {
    PApplet.main("Main");
  }
}