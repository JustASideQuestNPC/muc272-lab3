import processing.core.PGraphics;
import processing.core.PVector;

/* bullet fired by player weapons */
public class Bullet extends KEntity {
  public PVector position, velocity;

  /* ctor */
  Bullet(PVector position, PVector velocity) {
    super("bullet"); // initialize tag list
    this.position = position.copy();
    this.velocity = velocity;
    collider = new KCollider.Hitbox(position);
  }

  /* draws the bullet to the canvas */
  @Override
  public void render(PGraphics pg) {
    pg.noStroke();
    pg.fill(Colors.PINK.getCode());
    pg.ellipse(position.x, position.y, 10, 10);
  }

  /* updates position and velocity */
  @Override
  public void update(float dt) {
    position.add(PVector.mult(velocity, dt));
    setColliderPos(position);

    // check for collisions
    for (KEntity wall : engine.getTagged("wall")) {
      if (colliding(wall)) {
        markForDelete = true;
        return;
      }
    }
  }
}