import processing.core.PGraphics;
import processing.core.PVector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static java.lang.Math.*;

/* the player, obviously */
public class Player extends GameEntity {
  // velocity has two caps, the soft cap and the hard cap. the soft cap is effectively the player's base movement speed,
  // and directional inputs have no effect if velocity is higher than it. other sources such as dashing can increase the
  // player's velocity above the soft cap, but not above the hard cap.
  private static final float BASE_VELOCITY_SOFT_CAP = 750, VELOCITY_HARD_CAP = 1500; // pixels per second
  private static final float MOVE_ACCELERATION = 3000, FRICTION = 1500;
  private static final float HIGH_SPEED_FRICTION_MULT = 2f; // increases friction when above the soft speed cap
  private static final float VELOCITY_HARD_CAP_SQ = VELOCITY_HARD_CAP * VELOCITY_HARD_CAP;
  private float velocitySoftCap = BASE_VELOCITY_SOFT_CAP;
  private float velocitySoftCapSq = velocitySoftCap * velocitySoftCap; // used during updates

  /* hp/damage vars */
  public static final int BASE_MAX_HEALTH = 100;
  public int maxHealth = BASE_MAX_HEALTH;

  /* stamina vars */
  public static final int BASE_MAX_STAMINA = 1000;
  // after stamina is fully drained, the player can't use abilities until they regain at least this much stamina
  public static final int STAMINA_FULL_DRAIN_PENALTY = 300;
  // whenever the player damages an enemy, they regain (damage * ON_HIT_STAMINA_REGEN_MULT) points of stamina
  public static final int PASSIVE_STAMINA_REGEN = 150; // points/second
  public static final int DASH_STAMINA_COST = 300;
  public static final int SLOW_TIME_STAMINA_COST = 150; // points/second
  private static final float DASH_VELOCITY = 1500;
  private static final float DASH_DURATION = 0.05f; // dash duration in seconds
  public static final float SLOW_TIME_ABILITY_DT_MULT = 0.5f;
  public PVector velocity, onscreenPos;
  public float aimDirection;
  private Weapon weapon;
  private float dashMovementTimer;
  public int maxStamina = BASE_MAX_STAMINA;
  public float currentStamina = maxStamina;
  private boolean passiveStaminaRegenAllowed = true;
  private boolean staminaPenaltyActive = false;

  /* internal vars */
  private final ArrayList<Item> items = new ArrayList<>();

  /* ctor */
  Player(PVector position) {
    super("player"); // initialize tag list
    this.position = position.copy();
    velocity = new PVector(0, 0);
    // initialize collider
    colliders = new Collider.Hitbox[]{
        new Collider.Hitbox(new float[][]{
            {-18, -26},
            {-18,  -5},
            {-22,  29},
            {-32,  29},
            {-40,  17},
            {-40, -12},
            {-28, -26}
        }, position),
        new Collider.Hitbox(new float[][]{
            { 18, -26},
            { 18,  -5},
            { 22,  29},
            { 32,  29},
            { 40,  17},
            { 40, -12},
            { 28, -26}
        }, position),
        new Collider.Hitbox(new float[][]{
            {-18,  -4},
            {-14,   1},
            {-16,  19},
            {-22,  29}
        }, position),
        new Collider.Hitbox(new float[][]{
            { 18,  -4},
            { 14,   1},
            { 16,  19},
            { 22,  29}
        }, position),
        new Collider.Hitbox(new float[][]{
            {-10,   1},
            { -4, -11},
            {  4, -11},
            { 10,   1}
        }, position),
        new Collider.Hitbox(new float[][]{
            {-16,  19},
            {-14,   1},
            { 14,   1},
            { 16,  19}
        }, position)
    };
    // initialize sprite
    sprite = new Sprite("sprites/player-4x.png")
        .setDisplayAnchor(Sprite.DisplayAnchor.CENTER)
        .setAngle(aimDirection)
        .setAngleOffset(PI / 2)
        .setScale(0.5);
    currentHealth = maxHealth;
  }

  /* overload that takes discrete x and y coordinates */
  Player(float x, float y) {
    this(new PVector(x, y));
  }

  /* updates the player with the current time delta */
  @Override
  public void update(float dt)  {
    // if the player is dashing, don't check movement keys, maintain the dash, and decrement the dash timer
    if (dashMovementTimer > 0) {
      dashMovementTimer -= dt;
    }
    else {
      // convert directional keys into a single vector with the movement direction
      PVector moveInput = new PVector(0, 0);
      if (Input.isActive("move up")) --moveInput.y;
      if (Input.isActive("move down")) ++moveInput.y;
      if (Input.isActive("move left")) --moveInput.x;
      if (Input.isActive("move right")) ++moveInput.x;

      // check for a dash input if the player has enough stamina
      if (currentStamina >= DASH_STAMINA_COST && !staminaPenaltyActive) {
        // start a dash if the dash key and at least one movement key is pressed
        if (Input.isActive("dash") && moveInput.magSq() != 0) {
          dashMovementTimer = DASH_DURATION;
          currentStamina -= DASH_STAMINA_COST;
          // align velocity to the move input and scale it to dash velocity
          velocity.set(PVector.mult(moveInput, DASH_VELOCITY));
        }
      }

      // do movement if at least one movement key is pressed
      boolean applyFriction = true;
      if (moveInput.magSq() != 0) {
        // scale to acceleration, then add to velocity
        moveInput.setMag(MOVE_ACCELERATION * dt);
        // velocity is "soft capped", which means that normal movement can't increase it above a certain speed, but
        // other sources (such as dashing) can increase velocity past the soft cap up to the hard cap
        if (velocity.magSq() <= velocitySoftCapSq) {
          velocity.add(moveInput);
          if (velocity.magSq() > VELOCITY_HARD_CAP_SQ) {
            velocity.setMag(velocitySoftCap);
          }
          applyFriction = false;
        }
      }
      // apply friction if necessary
      if (applyFriction) {
        float appliedFriction = FRICTION;
        if (velocity.magSq() > velocitySoftCapSq) appliedFriction *= HIGH_SPEED_FRICTION_MULT;
        velocity.setMag(max(velocity.mag() - appliedFriction * dt, 0));
      }
    }

    passiveStaminaRegenAllowed = true;
    if (Input.isActive("slow time") && currentStamina > 0 && !staminaPenaltyActive) {
      engine.setDtMult(SLOW_TIME_ABILITY_DT_MULT);
      // get the raw dt to make the ability cost accurate to realtime
      currentStamina -= SLOW_TIME_STAMINA_COST * engine.deltaTimeRaw();
      passiveStaminaRegenAllowed = false;
    }
    else {
      engine.setDtMult(1);
    }

    // check for stamina penalty
    if (currentStamina <= 0) staminaPenaltyActive = true;
    else if (currentStamina >= STAMINA_FULL_DRAIN_PENALTY) staminaPenaltyActive = false;

    // apply passive stamina regen
    if (passiveStaminaRegenAllowed && currentStamina < maxStamina) {
      currentStamina += PASSIVE_STAMINA_REGEN * dt;
      if (currentStamina > maxStamina) currentStamina = maxStamina;
    }

    // hard cap velocity just in case
    if (velocity.magSq() > VELOCITY_HARD_CAP_SQ) {
      velocity.setMag(VELOCITY_HARD_CAP);
    }

    // scale velocity to dt and add to position
    position.add(PVector.mult(velocity, dt));

    // find where on the canvas the player is displayed
    onscreenPos = PVector.add(PVector.sub(position, engine.getCameraPos()), engine.getCameraOffset());

    // update aim angle and fire weapon
    aimDirection = (float)atan2(Input.mousePos.y - onscreenPos.y, Input.mousePos.x - onscreenPos.x);
    weapon.doFireCheck();

    setColliderPos(position);
    setColliderAngle(aimDirection + (float)(PI / 2));

    // do collision checks
    for (GameEntity wall : engine.getTagged("wall")) {
      PVector transVec = new PVector();
      if (colliding(wall, transVec)) {
        // determine which component of the velocity to 0 out
        if (abs(transVec.x) > abs(transVec.y)) velocity.x = 0;
        else velocity.y = 0;
        position.add(transVec); // offset player out of the wall
      }
    }

    // update sprite position and rotation
    sprite.setPos(position).setAngle(aimDirection);

    // update engine camera
    engine.setCameraTarget(position);
  }

  /* runs anything (abilities, items, etc) that gets triggered when the player *deals* (not takes) damage */
  public void doOnHitEffects(float dealtDamage) {
    // trigger effects for all items - if the items doesn't do anything when this happens, its onDealDamage
    // method will automatically do nothing
    items.forEach((item) -> item.onDealDamage(this, dealtDamage));
  }

  /* runs anything (abilities, items, etc) that gets triggered when the player kills an enemy */
  public void doOnKillEffects(GameEntity enemy) {
    // trigger effects for all items - if the items doesn't do anything when this happens, its onKillEnemy
    // method will automatically do nothing
    items.forEach((item) -> item.onKillEnemy(this, enemy));
  }

  /* equips a new weapon */
  public void equipWeapon(Weapon weapon) {
    this.weapon = weapon;
    weapon.player = new WeakReference<>(this);
  }

  /* equips a new piece of items */
  public void addItem(Item item) {
    items.add(item);
    // remove unique items from the item pool
    if (item.isUnique()) Main.unequippedItems.remove(item);
    item.onEquip(this);
  }

  @Override
  public void damage(float dmg) {
    currentHealth -= dmg;
    if (currentHealth <= 0) {
      Main.playerDead = true;
    }
    else {
      // trigger effects for all items - if the items doesn't do anything when this happens, its onTakeDamage
      // method will automatically do nothing
      items.forEach((item) -> item.onTakeDamage(this, dmg));
    }
  }

  public boolean isStaminaPenaltyActive() {
    return staminaPenaltyActive;
  }

  public Weapon getWeapon() {
    return weapon;
  }

  /* used by items */
  public void addTag(String tag) {
    tags.add(tag);
  }
  public void modVelocitySoftCap(float amount) {
    velocitySoftCap += amount;
    velocitySoftCapSq = velocitySoftCap * velocitySoftCap;
  }
}