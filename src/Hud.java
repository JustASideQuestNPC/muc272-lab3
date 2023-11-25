import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.HashMap;

import static processing.core.PConstants.*;

/* displays and updates hud and ui */
public class Hud {
  public static boolean SHOW_FRAMERATE = false;
  private static int width, height; // for conviencence to keep names cleaner
  private static Main app;
  private static PGraphics pg; // where to draw the hud to
  private static Main.GameState state;
  private static final int CLICK_DELAY_FRAMES = 10;
  private static int clickDelayTimer = CLICK_DELAY_FRAMES;

  /* hashmaps to store hud elements */
  private static final HashMap<String, Button> buttons = new HashMap<>();
  private static final HashMap<Main.GameState, String[]> buttonGroups = new HashMap<>();
  private static String[] activeButtons = new String[]{};
  private static final HashMap<String, Sprite> sprites = new HashMap<>();

  /* font stuff */
  // font objects - having a font object for each text size is generally preferred over using textSize()
  public static PFont UAV_OSD_SANS_MONO_64, UAV_OSD_SANS_MONO_48, UAV_OSD_SANS_MONO_28, UAV_OSD_SANS_MONO_14;
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
    UAV_OSD_SANS_MONO_64 = app.createFont(UAV_OSD_SANS_MONO_PATH, 64);
    UAV_OSD_SANS_MONO_48 = app.createFont(UAV_OSD_SANS_MONO_PATH, 48);
    UAV_OSD_SANS_MONO_28 = app.createFont(UAV_OSD_SANS_MONO_PATH, 28);
    UAV_OSD_SANS_MONO_14 = app.createFont(UAV_OSD_SANS_MONO_PATH, 14);

    // buttons...so many buttons
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

    buttons.put("main menu start game", new Button(pg)
        .setPos(width / 2 - 350, height / 2 - 60)
        .setSize(700, 100)
        .setText("Start game")
        .setFont(UAV_OSD_SANS_MONO_48)
        .setTextAlign(CENTER, CENTER)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(5)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("main menu exit to desktop", new Button(pg)
        .setPos(width / 2 - 350, height / 2 + 60)
        .setSize(700, 100)
        .setText("Exit to desktop")
        .setFont(UAV_OSD_SANS_MONO_48)
        .setTextAlign(CENTER, CENTER)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(5)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    // create button groups
    buttonGroups.put(Main.GameState.PAUSE_MENU, new String[]{
        "pause menu resume",
        "pause menu exit to menu",
        "pause menu exit to desktop"
    });
    buttonGroups.put(Main.GameState.MAIN_MENU, new String[]{
        "main menu start game",
        "main menu exit to desktop"
    });

    // add sprites for hud elements
    sprites.put("stamina bar icon",
        new Sprite("sprites/stamina-icon-2x.png")
            .setDisplayAnchor(Sprite.DisplayAnchor.BOTTOM_LEFT)
            .setPos(10, height - 5)
            .setScale(0.5)
    );
    sprites.put("hp bar icon",
        new Sprite("sprites/hp-icon-2x.png")
            .setDisplayAnchor(Sprite.DisplayAnchor.BOTTOM_LEFT)
            .setPos(10, height - 60)
            .setScale(0.41)
    );
  }

  /* updates display state */
  public static void setState(Main.GameState state) {
    Hud.state = state;
    activeButtons = buttonGroups.getOrDefault(state, new String[]{});
    clickDelayTimer = CLICK_DELAY_FRAMES;

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

    // decrement the click delay timer if it is still active, otherwise check for button presses
    if (clickDelayTimer > 0) {
      --clickDelayTimer;
    }
    else {
      switch (state) {
        case PAUSE_MENU:
          if (buttons.get("pause menu resume").isPressed()) {
            Main.paused = false;
            Hud.setState(Main.GameState.GAMEPLAY);
          }
          if (buttons.get("pause menu exit to menu").isPressed()) {
            app.setState(Main.GameState.MAIN_MENU);
          }
          if (buttons.get("pause menu exit to desktop").isPressed()) {
            app.exit();
          }
          break;
        case MAIN_MENU:
          if (buttons.get("main menu start game").isPressed()) {
            app.setState(Main.GameState.GAMEPLAY);
          }
          if (buttons.get("main menu exit to desktop").isPressed()) {
            app.exit();
          }
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
    switch(state) {
      case Main.GameState.GAMEPLAY:
        // display player stamina
        sprites.get("stamina bar icon").render(pg);
        int staminaBarXPos = 60;
        int staminaBarYPos = height - 40;
        int staminaBarWidth = 400;
        int staminaBarHeight = 15;

        pg.noStroke();
        pg.fill(Colors.TRANS_MEDIUM_TEAL.getCode());
        pg.rect(staminaBarXPos, staminaBarYPos, staminaBarWidth, staminaBarHeight);
        pg.fill(Colors.MEDIUM_TEAL.getCode());
        pg.rect(staminaBarXPos, staminaBarYPos, (int)((float)staminaBarWidth / Player.MAX_STAMINA *
            Main.player.getCurrentStamina() + 0.5), staminaBarHeight);

        // display player hp
        sprites.get("hp bar icon").render(pg);
        int hpBarXPos = 60;
        int hpBarYPos = height - 88;
        int hpBarWidth = 200;
        int hpBarHeight = 15;

        pg.noStroke();
        pg.fill(Colors.TRANS_RED.getCode());
        pg.rect(hpBarXPos, hpBarYPos, hpBarWidth, hpBarHeight);
        pg.fill(Colors.RED.getCode());
        pg.rect(hpBarXPos, hpBarYPos, (int)((float)hpBarWidth / Player.MAX_HEALTH *
            Main.player.getCurrentHealth() + 0.5), hpBarHeight);


        // draw indicators pointing to certain enemies
        int enemyIndicatorDistance = 100;
        float enemyIndicatorSize = 50;
        pg.pushMatrix();
        pg.translate(Main.player.onscreenPos.x, Main.player.onscreenPos.y);
        pg.noStroke();
        pg.fill(Colors.TRANS_RED.getCode());

        for (GameEntity ent : Main.engine.getTagged("has hud direction indicator")) {
          if (!ent.isOnscreen()) {
            PVector dir = PVector.sub(ent.position, Main.player.position);
            pg.pushMatrix();
            pg.rotate(dir.heading() + PI / 2);
            pg.triangle(0, -enemyIndicatorDistance - enemyIndicatorSize / 2,
                        -enemyIndicatorSize / 2, -enemyIndicatorDistance,
                        enemyIndicatorSize / 2, -enemyIndicatorDistance);
            pg.popMatrix();
          }
        }
        pg.popMatrix();
        break;
      case Main.GameState.MAIN_MENU:
        pg.textFont(UAV_OSD_SANS_MONO_64);
        pg.textAlign(CENTER, CENTER);
        pg.noStroke();
        pg.fill(Colors.BLACK.getCode());
        pg.text("PLACEHOLDER TITLE", width / 2f, height / 5f);
    }

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