import processing.core.PApplet;
import processing.data.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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
  private static final Random random = new Random();
  public static final int WORLD_WIDTH = 2500;
  public static final int WORLD_HEIGHT = 2500;
  public static final int BORDER_WALL_THICKNESS = 100;
  public static final List<Weapon> allWeapons = List.of(Weapon.values());

  /* graphics constants */
  // colors are actually just normal ints, but Processing gives them their own datatype because very weird things
  // happen if you try to use them like ints
  public static final int GAMEPLAY_BACKGROUND_COLOR = Colors.BLACK.getCode();
  public static final int BACKGROUND_GRID_COLOR_1 = Colors.WHITE.getCode();
  public static final int BACKGROUND_GRID_COLOR_2 = Colors.LIGHTER_TEAL.getCode();
  public static final int BACKGROUND_GRID_SIZE = 250;
  public static final int MENU_BACKGROUND_COLOR = Colors.WHITE.getCode();
  // WeakReferences reference an object while still allowing it to be destroyed - if we just referenced the player
  // directly, the engine wouldn't be able to remove it until any references to it were set to null
  public static WeakReference<Player> playerRef;
  // the enemy manager technically doesn't need a weak reference since it'll never be deleted, but it's a good habit
  // to have (it'll also prevent issues if something that deletes the enemy manager is added later)
  public static WeakReference<EnemyManager> enemyManager;
  public static boolean paused = false;
  public static boolean playerDead = false;
  public static Engine engine;
  public static int currentWave = 0;
  public static int numWaves;
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
    JSONArray waveJson = loadJSONArray("waves.json");
    numWaves = waveJson.size();
    enemyManager = new WeakReference<>(engine.addEntity(new EnemyManager(waveJson)));
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

      // check if the player is dead
      if (playerDead) setState(GameState.GAME_OVER);

      // check if the wave has been completed
      if (enemyManager.get().waveFinished() && !paused) {
        ++currentWave;
        if (currentWave < numWaves) {
          paused = true;
          engine.removeTagged("bullet");
          Hud.setState(GameState.WAVE_COMPLETE);
        }
        else {
          setState(GameState.RUN_COMPLETE);
        }
      }
    }

    // render based on game state
    if (Objects.requireNonNull(gameState) == GameState.GAMEPLAY) {
      background(GAMEPLAY_BACKGROUND_COLOR);
      engine.render();
    }
    else {
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
      playerDead = false;
      playerRef = new WeakReference<>(engine.addEntity(new Player(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f)));
      engine.setCameraPos(playerRef.get().position.x, playerRef.get().position.y);

      // give the player a weapon
      playerRef.get().equipWeapon(Weapon.DEVGUN);

      // return to and load the first wave
      currentWave = 0;
      enemyManager.get().loadWave(currentWave);
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
    MAIN_MENU,
    WAVE_COMPLETE,
    RUN_COMPLETE,
    GAME_OVER
  }

  /* returns a random weapon that isn't the weapon equipped on the player */
  public static Weapon randomWeapon() {
    Weapon weapon;
    do {
      weapon = allWeapons.get(random.nextInt(allWeapons.size()));
    } while (weapon == playerRef.get().getWeapon());
    return weapon;
  }

  // why did no one think to add a "random in range" function? if c++ has one then java has no excuse
  public static int randInt(int min, int max) {
    return random.nextInt(max - min) + min;
  }
}