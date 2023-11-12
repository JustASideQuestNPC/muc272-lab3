import processing.core.PGraphics;
import processing.core.PVector;

import static java.lang.Math.max;

public class Player extends KEntity {
  // nb: if something is a constant and should never be changed, it's convention to declare it as "static final" (unless
  // it can't be static, in which case just make it final)
  public static final float maxVelocity = 500, acceleration = 1000, friction = 1000;
  public PVector position, velocity;

  // ctor
  Player(PVector pos) {
    super("player"); // initialize tag list
    position = pos.copy();
    velocity = new PVector(0, 0);
  }

  // overload that takes discrete x and y coordinates
  Player(float x, float y) {
    this(new PVector(x, y));
  }

  // draws the player to the canvas
  @Override public void render(PGraphics pg) {
    pg.noStroke();
    pg.fill(0xffff0000);
    pg.ellipse(position.x, position.y, 50, 50);
  }

  // updates the player with the current time delta
  @Override public void update(float dt) {
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

    // cap movement speed
    velocity.limit(maxVelocity);

    // scale velocity to dt and add to position
    position.add(PVector.mult(velocity, dt));
  }
}