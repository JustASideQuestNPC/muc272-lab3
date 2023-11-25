import processing.core.PGraphics;
import processing.core.PVector;

/* superclass that all enemies inherit from - currently kind of unnecessary but will become more useful later */
public abstract class EnemySuper extends KEntity {
  private final int HEALTHBAR_CURRENT_COLOR = Colors.RED.getCode();
  private final int HEALTHBAR_MAX_COLOR = Colors.DARK_RED.getCode();

  /* ctor with tags that automatically appends the enemy tag */
  EnemySuper(String... tags) {
    super(tags);
    this.tags.add("enemy");
    this.tags.add("has hud direction indicator");
  }

  /* draws a healthbar above the enemy */
  protected final void renderHealthbar(PGraphics pg, int maxHealth, int x, int y, int width, int height) {
    int currentHealthWidth = (int)((float)width / maxHealth * currentHealth + 0.5);
    pg.noStroke();
    pg.fill(HEALTHBAR_MAX_COLOR);
    pg.rect(position.x + x - width / 2f, position.y + y, width, height);
    pg.fill(HEALTHBAR_CURRENT_COLOR);
    pg.rect(position.x + x - width / 2f, position.y + y, currentHealthWidth, height);
  }
}