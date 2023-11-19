import processing.core.PGraphics;
import processing.core.PVector;

import static java.lang.Math.*;

/* the player, obviously */
public class Player extends KEntity {
  // velocity has two caps, the soft cap and the hard cap. the soft cap is effectively the player's base movement speed,
  // and directional inputs have no effect if velocity is higher than it. other sources such as dashing can increase the
  // player's velocity to any amount, but it is locked to the hard cap and can never be increased above it.
  private static final float VELOCITY_SOFT_CAP = 750, VELOCITY_HARD_CAP = 1500;
  private static final float MOVE_ACCELERATION = 3000, FRICTION = 1500;
  private static final float HIGH_SPEED_FRICTION_MULT = 2f; // increased friction when above the soft speed cap
  private static final float VELOCITY_SOFT_CAP_SQ = VELOCITY_SOFT_CAP * VELOCITY_SOFT_CAP; // used during updates
  private static final float VELOCITY_HARD_CAP_SQ = VELOCITY_HARD_CAP * VELOCITY_HARD_CAP;
  private static final float DASH_VELOCITY = 1500;
  private static final float DASH_DURATION = 0.05f; // durations are in seconds
  public static final float DASH_COOLDOWN = 1f;
  public static final int NUM_DASHES = 3;
  private static final int BODY_COLOR = Colors.MEDIUM_TEAL.getCode();
  private static final int AIM_LINE_COLOR = Colors.BLACK.getCode();
  public PVector position, velocity, onscreenPos;
  public float aimDirection;
  private Weapon weapon;
  private float dashMovementTimer;
  private int remainingDashes = NUM_DASHES;
  private float dashCooldownTimer;

  /* ctor */
  Player(PVector pos) {
    super("player"); // initialize tag list
    position = pos.copy();
    velocity = new PVector(0, 0);
    // initialize collider
    collider = new KCollider.Hitbox(pos.x, pos.y, 25);
    // dash cooldowns are held in an array to make displaying them on the HUD easier
  }

  /* overload that takes discrete x and y coordinates */
  Player(float x, float y) {
    this(new PVector(x, y));
  }

  /* draws the player to the canvas */
  @Override
  public void render(PGraphics pg) {
    pg.noStroke();
    pg.fill(BODY_COLOR);
    pg.ellipse(position.x, position.y, 50, 50);

    pg.stroke(AIM_LINE_COLOR);
    pg.strokeWeight(5);
    pg.line(position.x, position.y,
            position.x + (float)cos(aimDirection) * 25,
            position.y + (float)sin(aimDirection) * 25);
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

      // check for a dash input if the player has at least one charged dash
      if (remainingDashes > 0) {
        // start a dash if the dash key and at least one movement key is pressed
        if (KInput.isActive("dash") && moveInput.magSq() != 0) {
          dashMovementTimer = DASH_DURATION;
          --remainingDashes;
          dashCooldownTimer = DASH_COOLDOWN;
          // align velocity to the move input and scale it to dash velocity
          velocity.set(PVector.mult(moveInput, DASH_VELOCITY));
        }
      }
      // update dash cooldown if needed
      if (remainingDashes < NUM_DASHES && dashCooldownTimer > 0) {
        dashCooldownTimer -= dt;
        if (dashCooldownTimer <= 0) {
          ++remainingDashes;
          if (remainingDashes < NUM_DASHES) {
            dashCooldownTimer = DASH_COOLDOWN;
          }
          else {
            dashCooldownTimer = 0;
          }
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

    // update engine camera
    engine.setCameraTarget(position);
  }

  /* equips a new weapon */
  public void equipWeapon(Weapon weapon) {
    this.weapon = weapon;
    weapon.player = this;
  }

  public float getRemainingDashCooldown() {
    return DASH_COOLDOWN - dashCooldownTimer;
  }

  public int getDashNumber() {
    return remainingDashes;
  }
}