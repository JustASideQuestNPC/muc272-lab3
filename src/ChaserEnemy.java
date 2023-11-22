import processing.core.PGraphics;
import processing.core.PVector;

import static java.lang.Math.*;

/* a simple enemy that chases the player */
public class ChaserEnemy extends EnemySuper {
  // 2 * pi is used a lot, so we cache it here to avoid some unnecessary multiplication operations and get a
  // worthlessly small performance boost
  private static final float TWO_PI = (float)(2 * PI);
  private static final float TURN_SPEED = (float)(PI * 3 / 2); // radians per second
  private static final float MOVE_SPEED = 600;  // pixels per second
  private float angle, targetAngle;

  /* ctor */
  ChaserEnemy(float x, float y) {
    super("chaser");
    position = new PVector(x, y);
    // initialize collider
    collider = new KCollider.Hitbox(position.x, position.y, 20);
    // setup sprite
    sprite = new KSprite("sprites/chaser-4x.png")
        .setImageMode(3)
        .setPos(position)
        .setAngle(angle)
        .setAngleOffset(PI / 2)
        .setScale(0.5);
  }


  /* updates everything */
  @Override
  public void update(float dt) {
    // find the angle to the player and add half a rotation so the enemy rotates toward the player
    targetAngle = (float)(atan2(Main.player.position.y - position.y, Main.player.position.x - position.x) + PI);

    // clamp our angle and the target angle to within a single rotation to prevent spinning infinitely - two mod
    // operations are required here to make sure the angles are always positive
    angle = (angle % TWO_PI + TWO_PI) % TWO_PI;
    targetAngle = (targetAngle % TWO_PI + TWO_PI) % TWO_PI;

    // rotate toward the player if we aren't already aiming at them
    if (angle != targetAngle) {
      // store which side of the target angle we're on to prevent an overshoot later
      boolean lessThanTargetAngle = (angle < targetAngle);

      // rotate in the correct direction
      if (lessThanTargetAngle) {
        if (abs(angle - targetAngle) > PI) angle += TURN_SPEED * dt;
        else angle -= TURN_SPEED * dt;
      }
      else {
        if (abs(angle - targetAngle) > PI) angle -= TURN_SPEED * dt;
        else angle += TURN_SPEED * dt;
      }

      // if we overshot the target angle, snap our angle to it
      if ((angle > targetAngle && lessThanTargetAngle) || (angle < targetAngle && !lessThanTargetAngle)) {
        angle = targetAngle;
      }
    }

    // move forward
    PVector moveStep = PVector.fromAngle(angle).setMag(MOVE_SPEED * dt);
    position.add(moveStep);
    collider.setPos(position);

    // update sprite
    sprite.setAngle(angle);
    sprite.setPos(position);
  }
}