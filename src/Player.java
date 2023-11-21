import processing.core.PVector;

import static java.lang.Math.*;

/* the player, obviously */
public class Player extends KEntity {
  // velocity has two caps, the soft cap and the hard cap. the soft cap is effectively the player's base movement speed,
  // and directional inputs have no effect if velocity is higher than it. other sources such as dashing can increase the
  // player's velocity above the soft cap, but not above the hard cap.
  private static final float VELOCITY_SOFT_CAP = 750, VELOCITY_HARD_CAP = 1500;
  private static final float MOVE_ACCELERATION = 3000, FRICTION = 1500;
  private static final float HIGH_SPEED_FRICTION_MULT = 2f; // increased friction when above the soft speed cap
  private static final float VELOCITY_SOFT_CAP_SQ = VELOCITY_SOFT_CAP * VELOCITY_SOFT_CAP; // used during updates
  private static final float VELOCITY_HARD_CAP_SQ = VELOCITY_HARD_CAP * VELOCITY_HARD_CAP;
  public static final int MAX_STAMINA = 1000;
  // whenever the player damages an enemy, they regain (damage * ON_HIT_STAMINA_REGEN_MULT) points of stamina
  public static final float ON_HIT_STAMINA_REGEN_MULT = 1;
  public static final int PASSIVE_STAMINA_REGEN = 50; // points/second
  public static final int DASH_STAMINA_COST = 300;
  public static final int SLOW_TIME_STAMINA_COST = 500; // points/second
  private static final float DASH_VELOCITY = 1500;
  private static final float DASH_DURATION = 0.05f; // dash duration in seconds
  public static final float SLOW_TIME_ABILITY_DT_MULT = 0.2f;
  public PVector position, velocity, onscreenPos;
  public float aimDirection;
  private Weapon weapon;
  private float dashMovementTimer;
  private float currentStamina = MAX_STAMINA;
  private boolean passiveStaminaRegenAllowed = true;

  /* ctor */
  Player(PVector pos) {
    super("player"); // initialize tag list
    position = pos.copy();
    velocity = new PVector(0, 0);
    // initialize collider
    collider = new KCollider.Hitbox(pos.x, pos.y, 25);
    // initialize sprite
    sprite = new KSprite("sprites/player-4x.png")
        .setImageMode(3) // processing would prefer i use CENTER, but all the all-caps names are really just ints
        .setPos(position)
        .setAngle(aimDirection)
        .setAngleOffset(PI / 2)
        .setScale(0.5);
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
      if (KInput.isActive("move up")) --moveInput.y;
      if (KInput.isActive("move down")) ++moveInput.y;
      if (KInput.isActive("move left")) --moveInput.x;
      if (KInput.isActive("move right")) ++moveInput.x;

      // check for a dash input if the player has enough stamina
      if (currentStamina >= DASH_STAMINA_COST) {
        // start a dash if the dash key and at least one movement key is pressed
        if (KInput.isActive("dash") && moveInput.magSq() != 0) {
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
        if (velocity.magSq() <= VELOCITY_SOFT_CAP_SQ) {
          velocity.add(moveInput);
          if (velocity.magSq() > VELOCITY_HARD_CAP_SQ) {
            velocity.setMag(VELOCITY_SOFT_CAP);
          }
          applyFriction = false;
        }
      }
      // apply friction if necessary
      if (applyFriction) {
        float appliedFriction = FRICTION;
        if (velocity.magSq() > VELOCITY_SOFT_CAP_SQ) appliedFriction *= HIGH_SPEED_FRICTION_MULT;
        velocity.setMag(max(velocity.mag() - appliedFriction * dt, 0));
      }
    }

    passiveStaminaRegenAllowed = true;
    if (KInput.isActive("slow time") && currentStamina > 0) {
      engine.setDtMult(SLOW_TIME_ABILITY_DT_MULT);
      currentStamina -= SLOW_TIME_STAMINA_COST * dt;
      passiveStaminaRegenAllowed = false;
    }
    else {
      engine.setDtMult(1);
    }

    // apply passive stamina regen
    if (passiveStaminaRegenAllowed && currentStamina < MAX_STAMINA) {
      currentStamina += PASSIVE_STAMINA_REGEN * dt;
      if (currentStamina > MAX_STAMINA) currentStamina = MAX_STAMINA;
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
    aimDirection = (float)atan2(KInput.mousePos.y - onscreenPos.y, KInput.mousePos.x - onscreenPos.x);
    weapon.doFireCheck();

    setColliderPos(position);

    // do collision checks
    for (KEntity wall : engine.getTagged("wall")) {
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

  // runs anything (abilities, equipment, etc) that gets triggered when the player *deals* (not takes) damage
  public void doOnHitEffects(int dealtDamage) {
    // apply stamina regen bonus
    currentStamina = min(currentStamina + dealtDamage * ON_HIT_STAMINA_REGEN_MULT, MAX_STAMINA);
  }

  /* equips a new weapon */
  public void equipWeapon(Weapon weapon) {
    this.weapon = weapon;
    weapon.player = this;
  }

  public float getCurrentStamina() {
    return currentStamina;
  }
}