import processing.core.PGraphics;
import processing.core.PVector;

import static java.lang.Math.atan2;
import static processing.core.PConstants.PI;

/* a stationary enemy that shoots bullets at the player */
public class TurretEnemy extends EnemySuper {
  private static final float BURST_DELAY = 1.25f; // time between bursts
  private static final float SHOT_DELAY = 0.15f; // time between shots in a burst
  private static final int SHOTS_PER_BURST = 4;
  private static final int BULLET_VELOCITY = 750;
  private static final float BULLET_DAMAGE = 20;

  private PVector bulletSpawnPoint = new PVector();
  private float angle = 0;
  private int shotsRemaining = SHOTS_PER_BURST; // how many shots are left in the current burst
  private float shotCooldown = BURST_DELAY;

  TurretEnemy(PVector position) {
    currentHealth = 150;
    this.position = position.copy();
    this.sprite = new Sprite("sprites/turret-base.png")
        .setDisplayAnchor(Sprite.DisplayAnchor.CENTER)
        .setPos(position);
    this.colliders = new Collider.Hitbox[]{
        new Collider.Hitbox(new float[][]{
            {-42,    -50},
            {-32,    -50},
            {-19.5f, -37.5f},
            {-37.5f, -19.5f},
            {-50,    -32},
            {-50,    -42}
        }),
        new Collider.Hitbox(new float[][]{
            {42,    -50},
            {32,    -50},
            {19.5f, -37.5f},
            {37.5f, -19.5f},
            {50,    -32},
            {50,    -42}
        }),
        new Collider.Hitbox(new float[][]{
            {-42,    50},
            {-32,    50},
            {-19.5f, 37.5f},
            {-37.5f, 19.5f},
            {-50,    32},
            {-50,    42}
        }),
        new Collider.Hitbox(new float[][]{
            {42,    50},
            {32,    50},
            {19.5f, 37.5f},
            {37.5f, 19.5f},
            {50,    32},
            {50,    42}
        }),
        new Collider.Hitbox(new float[][]{
            {-37.5f, -19.5f},
            {-19.5f, -37.5f},
            { 19.5f, -37.5f},
            { 37.5f, -19.5f},
            { 37.5f,  19.5f},
            { 19.5f,  37.5f},
            {-19.5f,  37.5f},
            {-37.5f,  19.5f}
        })
    };
    setColliderPos(position);
  }

  @Override
  public void render(PGraphics pg) {
    super.render(pg); // render sprite and hitboxes if enabled
    // render turret
    pg.pushMatrix();
    pg.translate(position.x, position.y);
    pg.rotate(angle + PI / 2);
    pg.noStroke();
    pg.fill(Colors.DARK_TEAL.getCode());
    pg.ellipse(0, 0, 46, 46);
    pg.rect(-10, -50, 20, 50);
    pg.popMatrix();

    if (SHOW_COLLIDERS) {
      pg.stroke(0xffff00ff);
      pg.strokeWeight(6);
      pg.point(bulletSpawnPoint.x, bulletSpawnPoint.y);
    }
  }

  @Override
  public void update(float dt) {
    // aim at the player
    angle = (float)atan2(Main.player.get().position.y - position.y,
                         Main.player.get().position.x - position.x);

    // if shots are on cooldown, update the timer
    if (shotCooldown > 0) {
      shotCooldown -= dt;
    }
    else {
      // update shot counter and cooldown
      if (shotsRemaining == 0) {
        shotsRemaining = SHOTS_PER_BURST;
        shotCooldown = BURST_DELAY;
      }
      else {
        shotCooldown = SHOT_DELAY;
      }
      --shotsRemaining;

      // update bullet spawn point - this is only done when firing since it's a relatively expensive operation
      bulletSpawnPoint = PVector.fromAngle(angle);
      bulletSpawnPoint.mult(50);
      bulletSpawnPoint.add(position);

      // create a velocity vector and spawn a bullet
      PVector bulletVelocity = PVector.fromAngle(angle);
      bulletVelocity.mult(BULLET_VELOCITY);

      engine.addEntity(new Bullet(bulletSpawnPoint, bulletVelocity, BULLET_DAMAGE, false));
    }
  }

  @Override
  public void runOnDeath() { EnemyManager.EnemyType.TURRET.removeEnemy(); }
}