import processing.core.PApplet;

public class Main extends PApplet {
  // nb: if something is a constant and should never be changed, it's convention to declare it as "static final" (unless
  // it can't be static, in which case just make it final) and format its name in SCREAMING_SNAKE_CASE
  /* graphics constants */
  public static final boolean FULLSCREEN = false; // overrides WINDOW_WIDTH and WINDOW_HEIGHT if true
  public static final int WINDOW_WIDTH = 1280;
  public static final int WINDOW_HEIGHT = 720;
  public static final int TARGET_FRAME_RATE = 60; // set to -1 to uncap
  public static final boolean SHOW_BACKGROUND_GRID = true;

  /* debug stuff */
  public static final boolean VERBOSE = true;

  /* engine/world constants */
  public static final int WORLD_WIDTH = 2000;
  public static final int WORLD_HEIGHT = 2000;
  public static final int BORDER_WALL_THICKNESS = 100;

  /* background grid constants */
  // colors are actually just normal ints, but Processing gives them their own datatype because very weird things happen
  // if you try to use them like ints
  public static final int BACKGROUND_COLOR = Colors.BLACK.getCode();
  public static final int BACKGROUND_GRID_COLOR_1 = Colors.WHITE.getCode();
  public static final int BACKGROUND_GRID_COLOR_2 = Colors.LIGHTER_TEAL.getCode();
  public static final int BACKGROUND_GRID_SIZE = 250;
  public static Player player;

  public static boolean paused = false;
  KEngine engine;

  /* anything run from outside the processing editor has to call size() in settings() because...reasons? */
  @Override
  public void settings() {
    if (FULLSCREEN) {
      fullScreen();
    }
    else {
      size(WINDOW_WIDTH, WINDOW_HEIGHT);
    }
  }

  /* all other setup stuff runs in setup() as normal */
  @Override
  public void setup() {
    if (VERBOSE) System.out.println("starting setup...");

    // set target framerate
    frameRate(TARGET_FRAME_RATE);

    if (VERBOSE) {
      // things printed with print() won't appear in the console until println() or flush() is called. surprisingly,
      // this is probably the most annoying language "feature" i've had to deal with today (not that i'm complaining)
      System.out.print("setting up inputs...");
      System.out.flush();
    }
    // set up inputs
    KInput.addInput("move up", new Key[]{Key.W, Key.UP});
    KInput.addInput("move down", new Key[]{Key.S, Key.DOWN});
    KInput.addInput("move left", new Key[]{Key.A, Key.LEFT});
    KInput.addInput("move right", new Key[]{Key.D, Key.RIGHT});
    KInput.addInput("fire semi", Key.LEFT_MOUSE, KInput.BindMode.PRESS_ONLY);
    KInput.addInput("fire auto", Key.LEFT_MOUSE);
    KInput.addInput("pause", Key.ESCAPE, KInput.BindMode.PRESS_ONLY);
    KInput.addInput("dash", Key.SPACEBAR, KInput.BindMode.PRESS_ONLY);
    KInput.addInput("slow time", Key.SHIFT);
    if (VERBOSE) System.out.println("done");

    if (VERBOSE) {
      System.out.print("initializing engine...");
      System.out.flush();
    }
    // set up engine
    engine = new KEngine(this);
    engine.setCameraEnabled(true);
    engine.setCameraOffset(width / 2f, height / 2f);
    engine.setCameraTightness(0.1f);
    if (VERBOSE) System.out.println("done");

    if (VERBOSE) {
      System.out.print("adding initial entities...");
      System.out.flush();
    }
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

    player = engine.addEntity(new Player(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f)); // add player
    engine.setCameraPos(player.position.x, player.position.y);

    engine.addEntity(new TargetEnemy(WORLD_WIDTH / 3f, WORLD_HEIGHT / 2f));
    if (VERBOSE) System.out.println("done");

    // setup weapons
    if (VERBOSE) {
      System.out.print("doing final weapon setup...");
      System.out.flush();
    }
    for (Weapon weapon : Weapon.values()) {
      weapon.setEngine(engine);
    }
    if (VERBOSE) System.out.println("done");

    // give the player a weapon
    player.equipWeapon(Weapon.DEVGUN);

    if (VERBOSE) {
      System.out.print("setting up hud/ui...");
      System.out.flush();
    }
    // setup hud
    Hud.init(this);
    Hud.setState(Hud.State.GAMEPLAY);
    if (VERBOSE) System.out.println("done");

    if (VERBOSE) System.out.println("setup complete\n\nstarting draw loop...");
  }

  /* draw runs once at the beginning of every frame */
  @Override
  public void draw() {
    KInput.update(mouseX, mouseY);
    Hud.update();

    // check for pause input
    if (KInput.isActive("pause")) {
      if (paused) {
        paused = false;
        Hud.setState(Hud.State.GAMEPLAY);
      }
      else {
        paused = true;
        Hud.setState(Hud.State.PAUSE_MENU);
      }
    }

    // update entities if not paused
    if (!paused) engine.update();
    else engine.updateDeltaTime();

    // render entities
    background(BACKGROUND_COLOR);
    engine.render();

    // render the hud
    Hud.render();
  }

  // override exit() to run stuff before exiting
  @Override
  public void exit() {
    if (VERBOSE) System.out.println("draw loop complete\n\nshutting down...");
    super.exit();
  }

  /* input listeners */
  @Override
  public void keyPressed() {
    // pressing escape normally just closes the program, but we can intercept it and change the key to prevent the
    // auto-close and open a pause menu instead
    if (key == ESC) key = 0;
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

  /* java boilerplate that runs settings() and setup(), then starts the draw() loop */
  public static void main(String[] args) {
    PApplet.main("Main");
  }
}