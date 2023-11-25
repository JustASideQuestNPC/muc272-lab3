import processing.core.PVector;

import static java.lang.Math.*;

/* a simple enemy that chases the player */
public class ChaserEnemy extends EnemySuper {
  // 2 * pi is used a lot, so we cache it here to avoid some unnecessary multiplication operations and get a
  // worthlessly small performance boost
  private static final float TWO_PI = (float)(2 * PI);
  private static final float TURN_SPEED = (float)(PI * 3 / 2); // radians per second
  private static final float MOVE_SPEED = 600;  // pixels per second
  private static final float DAMAGE_TO_PLAYER = 30; // dealt when the enemy hits the player
  private static final int MAX_HEALTH = 20;
  private float angle, targetAngle;

  /* ctor */
  ChaserEnemy(PVector position) {
    super("chaser");
    currentHealth = MAX_HEALTH;
    this.position = position.copy();
    // initialize collider
    colliders = new Collider.Hitbox[]{new Collider.Hitbox(new float[][]{
        {6, -27},
        {20, -3},
        {20, 19},
        {8, 27},
        {-8, 27},
        {-20, 19},
        {-20, -3},
        {-6, -27}
    }, position)};
    // setup sprite
    sprite = new Sprite("sprites/chaser-4x.png")
        .setDisplayAnchor(Sprite.DisplayAnchor.CENTER)
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
    setColliderPos(position);
    setColliderAngle((float)(angle + PI / 2));

    // check for collisions with walls
    for (GameEntity wall : engine.getTagged("wall")) {
      if (colliding(wall)) {
        markForDelete = true;
        return;
      }
    }

    // check for collisions with the player
    if (colliding(Main.player)) {
      Main.player.damage(DAMAGE_TO_PLAYER);
      markForDelete = true;
      return;
    }

    // update sprite
    sprite.setAngle(angle);
    sprite.setPos(position);
  }

  @Override
  public void runOnDeath() {
    EnemyManager.EnemyType.CHASER.removeEnemy();
  }
}