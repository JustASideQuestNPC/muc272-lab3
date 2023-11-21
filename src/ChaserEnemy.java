import processing.core.PGraphics;
import processing.core.PVector;

import static java.lang.Math.*;

/* a simple enemy that chases the player */
public class ChaserEnemy extends EnemySuper {
  private float angle;

  /* ctor */
  ChaserEnemy(float x, float y) {
    super("chaser");
    position = new PVector(x, y);
    // initialize collider
    collider = new KCollider.Hitbox(position.x - 10, position.y - 20, 20, 40);
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
    angle = (float)atan2(Main.player.position.y - position.y, Main.player.position.x - position.x);
    sprite.setAngle(angle);
  }
}