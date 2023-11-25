import processing.core.PGraphics;

/* a wall with collision */
public class Wall extends GameEntity {
  private final float x, y, w, h;
  private static final int color = Colors.BLACK.getCode();

  /* ctor */
  Wall(float x, float y, float w, float h) {
    super("wall"); // initialize tag list

    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    // initialize collider
    colliders = new Collider.Hitbox[]{new Collider.Hitbox(x, y, w, h)};
  }

  /* renders the wall to the canvas */
  @Override
  public void render(PGraphics pg) {
    pg.noStroke();
    pg.fill(color);
    pg.rect(x, y, w, h);
  }
}