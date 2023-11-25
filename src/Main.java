import processing.core.PApplet;

public class Main extends PApplet {
  // nb: if something is a constant and should never be changed, it's convention to declare it as "static final" (unless
  // it can't be static, in which case just make it final) and format its name in SCREAMING_SNAKE_CASE
  /* graphics constants */
  public static boolean FULLSCREEN = true; // overrides WINDOW_WIDTH and WINDOW_HEIGHT if true
  public static final int WINDOW_WIDTH = 1280;
  public static final int WINDOW_HEIGHT = 720;
  public static final int TARGET_FRAME_RATE = 60; // set to -1 to uncap
  public static final boolean SHOW_BACKGROUND_GRID = true;

  /* debug stuff */
  public static boolean VERBOSE = false;

  /* engine/world constants */
  public static final int WORLD_WIDTH = 2000;
  public static final int WORLD_HEIGHT = 2000;
  public static final int BORDER_WALL_THICKNESS = 100;

  /* graphics constants */
  // colors are actually just normal ints, but Processing gives them their own datatype because very weird things happen
  // if you try to use them like ints
  public static final int GAMEPLAY_BACKGROUND_COLOR = Colors.BLACK.getCode();
  public static final int BACKGROUND_GRID_COLOR_1 = Colors.WHITE.getCode();
  public static final int BACKGROUND_GRID_COLOR_2 = Colors.LIGHTER_TEAL.getCode();
  public static final int BACKGROUND_GRID_SIZE = 250;
  public static final int MENU_BACKGROUND_COLOR = Colors.WHITE.getCode();
  public static Player player;
  public static boolean paused = false;
  public static Engine engine;
  public static GameState gameState = GameState.MAIN_MENU;

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
    Input.addInput("move up", new Key[]{Key.W, Key.UP});
    Input.addInput("move down", new Key[]{Key.S, Key.DOWN});
    Input.addInput("move left", new Key[]{Key.A, Key.LEFT});
    Input.addInput("move right", new Key[]{Key.D, Key.RIGHT});
    Input.addInput("fire semi", Key.LEFT_MOUSE, Input.BindMode.PRESS_ONLY);
    Input.addInput("fire auto", Key.LEFT_MOUSE);
    Input.addInput("pause", Key.ESCAPE, Input.BindMode.PRESS_ONLY);
    Input.addInput("dash", Key.SPACEBAR, Input.BindMode.PRESS_ONLY);
    Input.addInput("slow time", Key.SHIFT);
    if (VERBOSE) System.out.println("done");

    if (VERBOSE) {
      System.out.print("initializing engine...");
      System.out.flush();
    }
    // give Sprite a reference to load images with
    Sprite.app = this;
    // set up engine
    engine = new Engine(this);
    engine.setCameraEnabled(true);
    engine.setCameraOffset(width / 2f, height / 2f);
    engine.setCameraTightness(0.1f);

    // add enemy manager - this will hang out for all of runtime and will never be deleted
    engine.addEntity(new EnemyManager());
    if (VERBOSE) System.out.println("done");

    if (VERBOSE) {
      System.out.print("performing final setup...");
      System.out.flush();
    }
    Hud.init(this);
    setState(GameState.MAIN_MENU);
    if (VERBOSE) System.out.println("done");

    if (VERBOSE) System.out.println("setup complete\n\nstarting draw loop...");
  }

  /* draw runs once at the beginning of every frame */
  @Override
  public void draw() {
    Input.update(mouseX, mouseY);
    Hud.update();

    // update and render everything in the engine during gameplay
    if (gameState == GameState.GAMEPLAY) {
      // check for pause input
      if (Input.isActive("pause")) {
        if (paused) {
          paused = false;
          Hud.setState(GameState.GAMEPLAY);
        }
        else {
          paused = true;
          Hud.setState(GameState.PAUSE_MENU);
        }
      }

      // update entities if not paused
      if (!paused) engine.update();
      else engine.updateDeltaTime();
    }

    // render based on game state
    switch (gameState) {
      case GAMEPLAY:
        background(GAMEPLAY_BACKGROUND_COLOR);
        engine.render();
        break;
      case MAIN_MENU:
        background(MENU_BACKGROUND_COLOR);
    }

    // hud is always rendered regardless of state
    Hud.render();
  }

  /* switches between game states */
  public void setState(GameState newState) {
    paused = false;
    gameState = newState;
    Hud.setState(newState);

    if (gameState == GameState.GAMEPLAY) {
      engine.purge(); // clear out any old entities
      player = null; // delete the player from the engine so it doesn't end up still hanging out in the engine

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

      // add player
      player = engine.addEntity(new Player(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f));
      engine.setCameraPos(player.position.x, player.position.y);

      // give the player a weapon
      player.equipWeapon(Weapon.DEVGUN);
    }
  }

  /* override exit() to run stuff before exiting */
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
    Input.pressKey(keyCode);
  }
  @Override
  public void keyReleased() {
    Input.releaseKey(keyCode);
  }
  @Override
  public void mousePressed() {
    Input.pressMouse(mouseButton);
  }
  @Override
  public void mouseReleased() {
    Input.releaseMouse(mouseButton);
  }

  /* called by java when the code is run - processing does this automatically when it compiles a sketch */
  public static void main(String[] args) {
    // parse command line args
    for (String arg : args) {
      switch (arg) {
        case "-v", "--verbose":
          VERBOSE = true;
          break;
        case "--showcolliders":
          GameEntity.SHOW_COLLIDERS = true;
          break;
        case "-w", "--windowed":
          FULLSCREEN = false;
      }
    }

    PApplet.main("Main");
  }

  public enum GameState {
    GAMEPLAY,
    PAUSE_MENU,
    MAIN_MENU
  }
}