import processing.core.PGraphics;
import processing.core.PVector;

import static java.lang.Math.*;

/* the player, obviously */
public class Player extends KEntity {
  private static final float maxVelocity = 750, acceleration = 1000, friction = 1000;
  private static final int BODY_COLOR = Colors.TEAL.getCode();
  private static final int AIM_LINE_COLOR = Colors.BLACK.getCode();
  public PVector position, velocity, onscreenPos;
  public float aimDirection;
  private Weapon weapon;

  /* ctor */
  Player(PVector pos) {
    super("player"); // initialize tag list
    position = pos.copy();
    velocity = new PVector(0, 0);
    // initialize collider
    collider = new KCollider.Hitbox(pos.x, pos.y, 25);
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
  public void update(float dt) {
    // convert directional keys into a single vector with the movement direction
    PVector moveInput = new PVector(0, 0);
    if (KInput.isActive("move up"))    --moveInput.y;
    if (KInput.isActive("move down"))  ++moveInput.y;
    if (KInput.isActive("move left"))  --moveInput.x;
    if (KInput.isActive("move right")) ++moveInput.x;

    // do movement if at least one movement key is pressed
    if (moveInput.magSq() != 0) {
      // scale to acceleration, then add to velocity
      moveInput.setMag(acceleration * dt);
      velocity.add(moveInput);
    }
    // otherwise, apply friction
    else {
      velocity.setMag(max(velocity.mag() - friction * dt, 0));
    }

    // find where on the canvas the player is displayed
    onscreenPos = PVector.add(PVector.sub(position, engine.getCameraPos()), engine.getCameraOffset());

    // update aim angle and fire weapon
    aimDirection = (float)atan2(KInput.mousePos.y - onscreenPos.y, KInput.mousePos.x - onscreenPos.x);
    weapon.doFireCheck();

    // cap movement speed
    velocity.limit(maxVelocity);

    // scale velocity to dt and add to position
    position.add(PVector.mult(velocity, dt));

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
}