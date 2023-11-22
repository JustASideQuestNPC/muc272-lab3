import processing.core.PGraphics;
import processing.core.PVector;

/* bullet fired by player weapons */
public class Bullet extends KEntity {
  public PVector position, velocity;

  // yes, i'm aware that i could name this "damage", but having a variable with the same name as a method is wrong on so
  // many levels. i have far too much dignity to stoop that low...unlike the processing devs.
  public final int impactDamage;

  private final boolean shotByPlayer; // determines what the bullet damages and if it triggers on-hit effects

  /* ctor */
  Bullet(PVector position, PVector velocity, int impactDamage, boolean shotByPlayer) {
    super("bullet"); // initialize tag list
    this.position = position.copy();
    this.velocity = velocity;
    this.impactDamage = impactDamage;
    this.shotByPlayer = shotByPlayer;
    collider = new KCollider.Hitbox(position.copy(), new PVector(0, 0));
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
    // update collider - bullets use a line instead of a point to prevent them from being on one side
    // of an enemy on one frame, and on the other side of them on the next
    collider.start.set(position);
    position.add(PVector.mult(velocity, dt));
    collider.end.set(position);

    // check for collisions with walls
    for (KEntity wall : engine.getTagged("wall")) {
      if (colliding(wall)) {
        markForDelete = true;
        return; // return ends the update early and prevents a bunch of unnecessary collision checks
      }
    }

    // check for collisions with targets
    if (shotByPlayer) {
      for (KEntity enemy : engine.getTagged("enemy")) {
        if (colliding(enemy)) {
          enemy.damage(impactDamage);
          Main.player.doOnHitEffects(impactDamage);
          markForDelete = true;
          return;
        }
      }
    }
    else {
      if (colliding(Main.player)) {
        Main.player.damage(impactDamage);
        markForDelete = true;
      }
    }
  }
}