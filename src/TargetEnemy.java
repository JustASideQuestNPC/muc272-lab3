import processing.core.PGraphics;
import processing.core.PVector;

/* basic stationary target used for testing and potentially a tutorial */
public class TargetEnemy extends EnemySuper {
  private static final int MAX_HEALTH = 100;

  /* ctor */
  TargetEnemy(float x, float y) {
    super("target");
    position = new PVector(x, y);
    currentHealth = MAX_HEALTH;
    collider = new KCollider.Hitbox(position, 30);
  }

  /* draws the target to the canvas */
  @Override
  public void render(PGraphics pg) {
    pg.noStroke();
    pg.fill(Colors.DARK_RED.getCode());
    pg.ellipse(position.x, position.y, 60, 60);
    pg.fill(Colors.LIGHT_TEAL.getCode());
    pg.ellipse(position.x, position.y, 40, 40);
    pg.fill(Colors.DARK_RED.getCode());
    pg.ellipse(position.x, position.y, 20, 20);

    renderHealthbar(pg, MAX_HEALTH, 0, -50, 70, 10);
  }
}