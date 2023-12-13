import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static java.lang.Math.tan;
import static java.lang.Math.round;
import static processing.core.PConstants.*;

/* displays and updates hud and ui */
public class Hud {
  private static int width, height; // for conviencence to keep names cleaner
  private static Main app;
  private static PGraphics pg; // where to draw the hud to
  private static Main.GameState state;
  private static final int CLICK_DELAY_FRAMES = 0;
  private static int clickDelayTimer = CLICK_DELAY_FRAMES;
  private static ArrayList<Weapon> availableWeapons;
  private static ArrayList<Item> availableItems;

  /* hashmaps to store hud elements */
  private static final HashMap<String, Button> buttons = new HashMap<>();
  private static final HashMap<Main.GameState, String[]> buttonGroups = new HashMap<>();
  private static String[] activeButtons = new String[]{};
  private static final HashMap<String, Sprite> sprites = new HashMap<>();

  /* containers for end-of-wave upgrades */
  private static final UpgradeHolder[] upgrades = new UpgradeHolder[3];

  /* font stuff */
  // font objects - having a font object for each text size is generally preferred over using textSize()
  public static PFont UAV_OSD_SANS_MONO_64, UAV_OSD_SANS_MONO_48, UAV_OSD_SANS_MONO_28, UAV_OSD_SANS_MONO_24,
      UAV_OSD_SANS_MONO_20, OLNEY_LIGHT_16;
  // file paths
  public static final String UAV_OSD_SANS_MONO_PATH = "fonts/UAV-OSD-Sans-Mono.ttf";
  public static final String OLNEY_LIGHT_PATH = "fonts/olney_light.otf";

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
    UAV_OSD_SANS_MONO_24 = app.createFont(UAV_OSD_SANS_MONO_PATH, 24);
    UAV_OSD_SANS_MONO_20 = app.createFont(UAV_OSD_SANS_MONO_PATH, 20);
    OLNEY_LIGHT_16 = app.createFont(OLNEY_LIGHT_PATH, 16);

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
        .setPos(width / 2 - 350, height / 2 - 120)
        .setSize(700, 100)
        .setText("Start game")
        .setFont(UAV_OSD_SANS_MONO_48)
        .setTextAlign(CENTER, CENTER)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(5)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("main menu how to play", new Button(pg)
        .setPos(width / 2 - 350, height / 2)
        .setSize(700, 100)
        .setText("How to play")
        .setFont(UAV_OSD_SANS_MONO_48)
        .setTextAlign(CENTER, CENTER)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(5)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("main menu exit to desktop", new Button(pg)
        .setPos(width / 2 - 350, height / 2 + 120)
        .setSize(700, 100)
        .setText("Exit to desktop")
        .setFont(UAV_OSD_SANS_MONO_48)
        .setTextAlign(CENTER, CENTER)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(5)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("wave complete restart run", new Button(pg)
        .setPos(width / 2 - 150, height / 2 + 100)
        .setSize(300, 60)
        .setText("Restart run")
        .setFont(UAV_OSD_SANS_MONO_28)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("wave complete exit to menu", new Button(pg)
        .setPos(width / 2 - 150, height / 2 + 170)
        .setSize(300, 60)
        .setText("End run")
        .setFont(UAV_OSD_SANS_MONO_28)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("game over restart run", new Button(pg)
        .setPos(width / 2 - 225, height / 2 - 100)
        .setSize(450, 60)
        .setText("Restart run")
        .setFont(UAV_OSD_SANS_MONO_28)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setHoveredFillColor(Colors.BLACK)
        .setHoveredTextColor(Colors.WHITE));

    buttons.put("game over exit to menu", new Button(pg)
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

    buttons.put("game over exit to desktop", new Button(pg)
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

    buttons.put("wave complete item 0", new Button(pg)
        .setPos(width / 2 - 250, height / 4 + 50)
        .setSize(150, 150)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setFont(UAV_OSD_SANS_MONO_20)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK));

    buttons.put("wave complete item 1", new Button(pg)
        .setPos(width / 2 - 75, height / 4 + 50)
        .setSize(150, 150)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setFont(UAV_OSD_SANS_MONO_20)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK));

    buttons.put("wave complete item 2", new Button(pg)
        .setPos(width / 2 + 100, height / 4 + 50)
        .setSize(150, 150)
        .setFillColor(Colors.WHITE)
        .setStrokeColor(Colors.BLACK)
        .setStrokeWeight(4)
        .setFont(UAV_OSD_SANS_MONO_20)
        .setTextAlign(CENTER, CENTER)
        .setTextColor(Colors.BLACK));

    buttons.put("how to play exit to menu", new Button(pg)
        .setPos(width / 2 - 425, height - 175)
        .setSize(850, 100)
        .setText("Return to main menu")
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
        "main menu exit to desktop",
        "main menu how to play"
    });
    buttonGroups.put(Main.GameState.WAVE_COMPLETE, new String[]{
        "wave complete exit to menu",
        "wave complete restart run",
        "wave complete item 0",
        "wave complete item 1",
        "wave complete item 2"});
    buttonGroups.put(Main.GameState.GAME_OVER, new String[]{
        "game over restart run",
        "game over exit to menu",
        "game over exit to desktop"
    });
    buttonGroups.put(Main.GameState.RUN_COMPLETE, new String[]{
        "game over restart run",
        "game over exit to menu",
        "game over exit to desktop"
    });
    buttonGroups.put(Main.GameState.HOW_TO_PLAY, new String[]{
       "how to play exit to menu"
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

    // disable the cursor during gameplay
    if (state == Main.GameState.GAMEPLAY) {
      app.noCursor();
      Objects.requireNonNull(Main.player.get()).fireLockoutTimer = Player.FIRE_LOCKOUT_DURATION;
    }
    else {
      app.cursor(ARROW);
    }

    // generate new items if a wave has been completed
    if (state == Main.GameState.WAVE_COMPLETE) {
      availableWeapons = new ArrayList<>(Arrays.asList(Weapon.values()));
      availableWeapons.remove(Objects.requireNonNull(Main.player.get()).getWeapon());
      availableItems = new ArrayList<>(Main.unequippedItems);
      for (int i = 0; i < upgrades.length; ++i) {
        upgrades[i] = new UpgradeHolder();
        String buttonName = String.format("wave complete item %d", i);
        if (upgrades[i].isWeapon) buttons.get(buttonName).setText("Weapon\nImage\nHere");
        else buttons.get(buttonName).setText("Upgrade\nImage\nHere");
      }
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
          if (buttons.get("main menu how to play").isPressed()) {
            Hud.setState(Main.GameState.HOW_TO_PLAY);
          }
          break;
        case WAVE_COMPLETE:
          if (buttons.get("wave complete restart run").isPressed()) {
            app.setState(Main.GameState.GAMEPLAY);
          }
          if (buttons.get("wave complete exit to menu").isPressed()) {
            app.setState(Main.GameState.MAIN_MENU);
          }
          // check if an upgrade has been selected
          for (int i = 0; i < upgrades.length; ++i) {
            String buttonName = String.format("wave complete item %d", i);
            UpgradeHolder upgrade = upgrades[i];
            if (buttons.get(buttonName).isPressed()) {
              if (upgrade.isWeapon) {
                Objects.requireNonNull(Main.player.get()).equipWeapon(upgrade.weapon);
              }
              else {
                Objects.requireNonNull(Main.player.get()).addItem(upgrade.item);
              }
              // start the next wave and unpause
              Objects.requireNonNull(Main.enemyManager.get()).loadWave(Main.currentWave);
              Main.paused = false;
              Objects.requireNonNull(Main.player.get()).currentHealth += 35;
              if (Objects.requireNonNull(Main.player.get()).currentHealth > Objects.requireNonNull(Main.player.get()).maxHealth) {
                Objects.requireNonNull(Main.player.get()).currentHealth = Objects.requireNonNull(Main.player.get()).maxHealth;
              }

              setState(Main.GameState.GAMEPLAY);
              SoundManager.play("get upgrade");
            }
          }
          break;
        case GAME_OVER, RUN_COMPLETE:
          if (buttons.get("game over restart run").isPressed()) {
            app.setState(Main.GameState.GAMEPLAY);
          }
          if (buttons.get("game over exit to menu").isPressed()) {
            app.setState(Main.GameState.MAIN_MENU);
          }
          if (buttons.get("game over exit to desktop").isPressed()) {
            app.exit();
          }
          break;
        case HOW_TO_PLAY:
          if (buttons.get("how to play exit to menu").isPressed()) {
            Hud.setState(Main.GameState.MAIN_MENU);
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
      case GAMEPLAY:
        // display player stamina
        sprites.get("stamina bar icon").render(pg);
        int staminaBarXPos = 60;
        int staminaBarYPos = height - 40;
        int staminaBarWidth = Objects.requireNonNull(Main.player.get()).maxStamina / 5;
        int staminaBarHeight = 15;

        pg.noStroke();
        if (Objects.requireNonNull(Main.player.get()).isStaminaPenaltyActive()) {
          pg.fill(Colors.TRANS_DARK_RED.getCode());
        }
        else {
          pg.fill(Colors.TRANS_MEDIUM_TEAL.getCode());
        }
        pg.rect(staminaBarXPos, staminaBarYPos, staminaBarWidth, staminaBarHeight);
        if (Objects.requireNonNull(Main.player.get()).isStaminaPenaltyActive()) {
          pg.fill(Colors.DARK_RED.getCode());
        }
        else {
          pg.fill(Colors.MEDIUM_TEAL.getCode());
        }
        pg.rect(staminaBarXPos, staminaBarYPos, (int)((float)staminaBarWidth / Objects.requireNonNull(Main.player.get()).maxStamina *
                                                      Objects.requireNonNull(Main.player.get()).currentStamina + 0.5), staminaBarHeight);

        // display player hp
        sprites.get("hp bar icon").render(pg);
        int hpBarXPos = 60;
        int hpBarYPos = height - 88;
        int hpBarWidth = Objects.requireNonNull(Main.player.get()).maxHealth * 2;
        int hpBarHeight = 15;

        pg.noStroke();
        pg.fill(Colors.TRANS_RED.getCode());
        pg.rect(hpBarXPos, hpBarYPos, hpBarWidth, hpBarHeight);
        pg.fill(Colors.RED.getCode());
        pg.rect(hpBarXPos, hpBarYPos, (int)((float)hpBarWidth / Objects.requireNonNull(Main.player.get()).maxHealth *
                                            Objects.requireNonNull(Main.player.get()).currentHealth + 0.5), hpBarHeight);

        // draw indicators pointing to certain enemies
        int enemyIndicatorDistance = 100;
        float enemyIndicatorSize = 50;
        pg.pushMatrix();
        pg.translate(Objects.requireNonNull(Main.player.get()).onscreenPos.x, Objects.requireNonNull(Main.player.get()).onscreenPos.y);
        pg.noStroke();
        pg.fill(Colors.TRANS_RED.getCode());

        for (GameEntity ent : Main.engine.getTagged("has hud direction indicator")) {
          if (!ent.isOnscreen()) {
            PVector dir = PVector.sub(ent.position, Objects.requireNonNull(Main.player.get()).position);
            pg.pushMatrix();
            pg.rotate(dir.heading() + PI / 2);
            pg.triangle(0, -enemyIndicatorDistance - enemyIndicatorSize / 2,
                        -enemyIndicatorSize / 2, -enemyIndicatorDistance,
                        enemyIndicatorSize / 2, -enemyIndicatorDistance);
            pg.popMatrix();
          }
        }
        pg.popMatrix();

        // draw wave number
        pg.noStroke();
        pg.fill(Colors.BLACK.getCode());
        pg.textAlign(LEFT, BOTTOM);
        pg.textFont(UAV_OSD_SANS_MONO_20);
        pg.text(String.format("Wave %d\n%d Enemies", Main.currentWave, Objects.requireNonNull(Main.enemyManager.get()).getRemainingEnemies() + 1),
                10, height - 110);

        // draw a reticle to show where the player is aiming
        pg.pushMatrix();
        pg.translate(Input.mousePos.x, Input.mousePos.y);
        pg.rotate(Objects.requireNonNull(Main.player.get()).aimDirection);

        pg.pushMatrix();
        pg.rotate(PI / 4);
        pg.noStroke();
        pg.fill(Colors.TRANS_RED.getCode());
        pg.rect(-5, -5, 10, 10);
        pg.popMatrix();

        // if the weapon isn't a 0-spread weapon, draw indicators to show the spread width at the cursor
        if (Objects.requireNonNull(Main.player.get()).getWeapon().getHalfSpreadRange() > 0) {
          float reticleSize = (float)(PVector.dist(Input.mousePos, Objects.requireNonNull(Main.player.get()).onscreenPos)
                        * tan(Objects.requireNonNull(Main.player.get()).getWeapon().getHalfSpreadRange()));

          pg.beginShape();
          pg.vertex(20, -reticleSize);
          pg.vertex(0, -reticleSize - 20);
          pg.vertex(-20, -reticleSize);
          pg.vertex(-10, -reticleSize);
          pg.vertex(0, -reticleSize - 10);
          pg.vertex(10, -reticleSize);
          pg.endShape(CLOSE);

          pg.beginShape();
          pg.vertex(20, reticleSize);
          pg.vertex(0, reticleSize + 20);
          pg.vertex(-20, reticleSize);
          pg.vertex(-10, reticleSize);
          pg.vertex(0, reticleSize + 10);
          pg.vertex(10, reticleSize);
          pg.endShape(CLOSE);
        }
        pg.popMatrix();
        break;
      case MAIN_MENU:
        pg.textFont(UAV_OSD_SANS_MONO_64);
        pg.textAlign(CENTER, CENTER);
        pg.noStroke();
        pg.fill(Colors.BLACK.getCode());
        pg.text("PLACEHOLDER TITLE", width / 2f, height / 5f);
        break;
      case WAVE_COMPLETE:
        pg.textFont(UAV_OSD_SANS_MONO_64);
        pg.textAlign(CENTER, CENTER);
        pg.noStroke();
        pg.fill(Colors.BLACK.getCode());
        pg.text("WAVE COMPLETE", width / 2f, height / 6f - 10);
        pg.textFont(UAV_OSD_SANS_MONO_28);
        pg.text("Choose an item to go the next wave", width / 2f, height / 4f);

        // draw tooltips for each item
        for (int i = 0; i < upgrades.length; ++i) {
          String buttonName = String.format("wave complete item %d", i);
          if (buttons.get(buttonName).isHovered()) {
            pg.fill(Colors.WHITE.getCode());
            pg.stroke(Colors.BLACK.getCode());
            pg.strokeWeight(4);
            pg.rect(Input.mousePos.x, Input.mousePos.y, 400, upgrades[i].tooltipHeight);
            pg.noStroke();
            pg.fill(Colors.BLACK.getCode());
            pg.textAlign(LEFT, TOP);
            pg.textFont(UAV_OSD_SANS_MONO_24);
            pg.text(upgrades[i].name, Input.mousePos.x + 10, Input.mousePos.y + 10);
            pg.fill(Colors.DARK_TEAL.getCode());
            pg.textFont(OLNEY_LIGHT_16);
            pg.text(upgrades[i].description, Input.mousePos.x + 10, Input.mousePos.y + 50, 380, 1000);
          }
        }
        break;
      case GAME_OVER:
        pg.textFont(UAV_OSD_SANS_MONO_64);
        pg.textAlign(CENTER, CENTER);
        pg.noStroke();
        pg.fill(Colors.BLACK.getCode());
        pg.text("YOU ARE DEAD", width / 2f, height / 5f);
        break;
      case RUN_COMPLETE:
        pg.textFont(UAV_OSD_SANS_MONO_64);
        pg.textAlign(CENTER, CENTER);
        pg.noStroke();
        pg.fill(Colors.BLACK.getCode());
        pg.text("RUN COMPLETE", width / 2f, height / 6f - 10);
        pg.textFont(UAV_OSD_SANS_MONO_28);
        pg.text("You win!", width / 2f, height / 4f);
        break;
      case HOW_TO_PLAY:
        pg.textFont(UAV_OSD_SANS_MONO_64);
        pg.textAlign(CENTER, CENTER);
        pg.noStroke();
        pg.fill(Colors.BLACK.getCode());
        pg.text("HOW TO PLAY", width / 2f, height / 6f);

        // draw main body text
        pg.textFont(UAV_OSD_SANS_MONO_24);
        pg.text("""
                         to move //       to aim
                               to shoot your gun
                    Tap         while moving to dash
                    Hold       for adrenaline
                    Dashing and adrenaline consume energy
                    Don't die
                    """, width / 2f, height / 2f);

        // display controls and actions in different colors
        pg.pushMatrix();
        pg.translate(width / 2f, height / 2f);
        pg.fill(Colors.RED.getCode());
        pg.text("WASD", -253, -108);
        pg.text("Mouse", 95, -108);
        pg.text("Left Mouse", -190, -72);
        pg.text("Spacebar", -180, -37);
        pg.text("Shift", -106, 0);
        pg.popMatrix();
    }
  }

  /* used for randomizing upgrades at the end of each wave */
  private static class UpgradeHolder {
    public boolean isWeapon;
    public Weapon weapon;
    public Item item;
    public String name, description;
    public int tooltipHeight;

    UpgradeHolder() {
      // generate a random weapon or upgrade (once upgrades have actually been added)
      isWeapon = Main.randInt(0, 2) == 0;
      if (isWeapon) {
        weapon = randomWeapon();
        name = weapon.getName();
        description = weapon.getDescription();
      }
      else {
        item = randomItem();
        name = item.getName();
        description = item.getDescription();
      }
      tooltipHeight = Hud.textHeight(description, OLNEY_LIGHT_16, 380) + 50;
    }
  }

  /* returns a random weapon that isn't equipped by the player, and that hasn't been already assigned to an upgrade */
  private static Weapon randomWeapon() {
    Weapon weapon = availableWeapons.get(Main.randInt(0, availableWeapons.size()));
    availableWeapons.remove(weapon);
    return weapon;
  }

  /* returns a random upgrade that is non-unique or that hasn't been equipped by the player */
  private static Item randomItem() {
    Item item = availableItems.get(Main.randInt(0, availableItems.size()));
    availableItems.remove(item);
    return item;
  }

  /* IT SHOULD NOT BE THIS HARD TO FIND OUT HOW TALL A BOX NEEDS TO BE. */
  public static int textHeight(String str, PFont font, int specificWidth) {
    pg.pushStyle();
    pg.textFont(font); // I definitely shouldn't need to load a font to find out how tall a box needs to be
    float leading = pg.textLeading;
    // split by new lines first
    String[] paragraphs = str.split("\n");
    int numberEmptyLines = 0;
    int numTextLines = 0;
    for (String paragraph : paragraphs) {
      // skip empty lines
      if (paragraph.isEmpty()) {
        ++numberEmptyLines;
      }
      else {
        ++numTextLines;
        // word wrap
        String[] wordsArray = paragraph.split(" ");
        StringBuilder tempString = new StringBuilder();
        for (String s : wordsArray) {
          if (pg.textWidth(tempString + s) < specificWidth) {
            tempString.append(s).append(" ");
          }
          else {
            tempString = new StringBuilder(s + " ");
            numTextLines++;
          }
        }
      }
    }
    pg.popStyle();

    float totalLines = numTextLines + numberEmptyLines;
    return round(totalLines * leading);
  }
}