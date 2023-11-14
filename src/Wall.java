import processing.core.PGraphics;
public class Wall extends KEntity {
  private final float x, y, w, h;
  private static final int color = 0xff000000;

  // ctor
  Wall(float x, float y, float w, float h) {
    super("wall"); // initialize tag list

    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    // initialize collider
    collider = new KCollider.Hitbox(x, y, w, h);
  }

  // renders the wall to the canvas
  @Override
  public void render(PGraphics pg) {
    pg.noStroke();
    pg.fill(color);
    pg.rect(x, y, w, h);
  }
}