import processing.core.PGraphics;
import processing.core.PVector;

/* bullet fired by player weapons */
public class Bullet extends KEntity {
  public PVector position, velocity;

  // yes, i'm aware that i could name this "damage", but having a variable with the same name as a method is wrong on so
  // many levels. i have far too much dignity to stoop that low...unlike the processing devs.
  public final int impactDamage;

  /* ctor */
  Bullet(PVector position, PVector velocity, int impactDamage) {
    super("bullet"); // initialize tag list
    this.position = position.copy();
    this.velocity = velocity;
    this.impactDamage = impactDamage;
    collider = new KCollider.Hitbox(position);
  }

  /* draws the bullet to the canvas */
  @Override
  public void render(PGraphics pg) {
    pg.noStroke();
    pg.fill(Colors.RED.getCode());
    pg.ellipse(position.x, position.y, 10, 10);
  }

  /* updates position and velocity, then runs collision checks */
  @Override
  public void update(float dt) {
    position.add(PVector.mult(velocity, dt));
    setColliderPos(position);

    // check for collisions with walls
    for (KEntity wall : engine.getTagged("wall")) {
      if (colliding(wall)) {
        markForDelete = true;
        return; // return ends the update early and prevents a bunch of unnecessary collision checks
      }
    }

    // check for collisions with enemies
    for (KEntity enemy : engine.getTagged("enemy")) {
      if (colliding(enemy)) {
        enemy.damage(impactDamage);
        markForDelete = true;
        return;
      }
    }
  }
}