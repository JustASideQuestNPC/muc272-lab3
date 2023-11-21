import processing.core.PFont;
import processing.core.PGraphics;

import java.util.HashMap;

import static processing.core.PConstants.*;

/* displays and updates hud and ui */
public class Hud {
  public static boolean ENABLE_DEBUG_OVERLAY = true; // toggles all debug overlays, overrides specific toggles
  public static boolean SHOW_FRAMERATE = false;
  private static int width, height; // for conviencence to keep names cleaner
  private static Main app;
  private static PGraphics pg; // where to draw the hud to
  private static State state = State.NONE; // what hud to draw

  // buttons...so many buttons
  private static final HashMap<String, Button> buttons = new HashMap<>();
  private static final HashMap<State, String[]> buttonGroups = new HashMap<>();
  private static String[] activeButtons = new String[]{};

  /* font stuff */
  // font objects - having a font object for each text size is generally preferred over using textSize()
  public static PFont UAV_OSD_SANS_MONO_28, UAV_OSD_SANS_MONO_14;
  // file paths
  public static final String UAV_OSD_SANS_MONO_PATH = "fonts/UAV-OSD-Sans-Mono.ttf";

  /* sets up everything */
  public static void init(Main app) {
    // set properties taken from the parent app
    Hud.app = app;
    pg = app.getGraphics();
    width = app.width;
    height = app.height;

    // load fonts
    UAV_OSD_SANS_MONO_28 = app.createFont(UAV_OSD_SANS_MONO_PATH, 28);
    UAV_OSD_SANS_MONO_14 = app.createFont(UAV_OSD_SANS_MONO_PATH, 14);

    // haha builder pattern go brrr
    buttons.put("pause menu resume", new Button(pg)
        .setPos(width / 2 - 225, height / 2 - 100)
        .setSize(450, 60)
        .setText("Resume game")
        .setFont(UAV_OSD_SANS_MONO_28)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("pause menu exit to menu", new Button(pg)
        .setPos(width / 2 - 225, height / 2 - 30)
        .setSize(450, 60)
        .setText("Exit to main menu")
        .setFont(UAV_OSD_SANS_MONO_28)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("pause menu exit to desktop", new Button(pg)
        .setPos(width / 2 - 225, height / 2 + 40)
        .setSize(450, 60)
        .setText("Exit to desktop")
        .setFont(UAV_OSD_SANS_MONO_28)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    // create button groups
    buttonGroups.put(State.NONE, new String[]{});
    buttonGroups.put(State.PAUSE_MENU, new String[]{
        "pause menu resume",
        "pause menu exit to menu",
        "pause menu exit to desktop"
    });
  }

  /* updates display state */
  public static void setState(State state) {
    Hud.state = state;
    activeButtons = buttonGroups.getOrDefault(state, new String[]{});

    // un-hover all buttons
    for (Button b : buttons.values()) {
      b.setHovered(false);
    }
  }

  /* updates active buttons */
  public static void update() {
    for (String buttonName : activeButtons) {
      buttons.get(buttonName).update();
    }

    // check for button presses based on state
    if (state == State.PAUSE_MENU) {
      if (buttons.get("pause menu resume").isPressed()) {
        Main.paused = false;
        setState(State.GAMEPLAY);
      }
      if (buttons.get("pause menu exit to desktop").isPressed()) {
        app.exit();
      }
    }
  }

  /* renders to the canvas based on the current state */
  public static void render() {
    // render active buttons
    for (String buttonName : activeButtons) {
      buttons.get(buttonName).render();
    }

    // render special stuff based on state
    if (state == State.GAMEPLAY) {
      // display player stamina
      int staminaBarXPos = width / 2 - 200;
      int staminaBarYPos = height * 4 / 5;
      int staminaBarWidth = 400;
      int staminaBarHeight = 10;

      pg.noStroke();
      pg.fill(Colors.TRANS_MEDIUM_TEAL.getCode());
      pg.rect(staminaBarXPos, staminaBarYPos, staminaBarWidth, staminaBarHeight);
      pg.fill(Colors.MEDIUM_TEAL.getCode());
      pg.rect(staminaBarXPos, staminaBarYPos, (int)((float)staminaBarWidth / Player.MAX_STAMINA *
          Main.player.getCurrentStamina() + 0.5), staminaBarHeight);
    }

    // draw debug overlays (if enabled)
    if (ENABLE_DEBUG_OVERLAY) {
      if (SHOW_FRAMERATE) {
        pg.noStroke();
        pg.fill(0xa0000000);
        pg.rect(0, 0, 95, 22);
        pg.fill(0xff00ff00);
        pg.textFont(UAV_OSD_SANS_MONO_14);
        pg.textAlign(LEFT, TOP);
        pg.text(String.format("%03d FPS", (int)app.frameRate), 4, 3);
      }
    }
  }

  /* determines what is currently being displayed */
  public enum State {
    NONE, // disables the hud except for debug info
    PAUSE_MENU,
    GAMEPLAY
  }
}