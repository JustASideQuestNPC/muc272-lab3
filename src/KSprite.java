import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

/* handles displaying and rotating an image sprite */
public class KSprite {
  public static Main app;
  private final PImage image;
  private DisplayAnchor displayAnchor = DisplayAnchor.TOP_LEFT;
  private float x, y, xOffset, yOffset, width, height, angle, angleOffset;

  /* ctor, takes the path to the image */
  KSprite(String imagePath) {
    image = app.loadImage(imagePath);
    width = image.width;
    height = image.height;
  }

  /* builder pattern setters */
  public KSprite setPos(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }
  // overload that takes a PVector
  public KSprite setPos(PVector pos) {
    return setPos(pos.x, pos.y);
  }
  public KSprite setSize(float width, float height) {
    this.width = width;
    this.height = height;
    setDisplayAnchor(displayAnchor);
    return this;
  }
  public KSprite setDisplayAnchor(DisplayAnchor displayAnchor) {
    this.displayAnchor = displayAnchor;
    switch (displayAnchor) {
      case TOP:
        xOffset = width / 2;
        yOffset = 0;
        break;
      case BOTTOM:
        xOffset = width / 2;
        yOffset = height;
        break;
      case LEFT:
        xOffset = 0;
        yOffset = height / 2;
        break;
      case RIGHT:
        xOffset = width;
        yOffset = height / 2;
        break;
      case TOP_LEFT:
        xOffset = 0;
        yOffset = 0;
        break;
      case TOP_RIGHT:
        xOffset = width;
        yOffset = 0;
        break;
      case BOTTOM_LEFT:
        xOffset = 0;
        yOffset = height;
        break;
      case BOTTOM_RIGHT:
        xOffset = width;
        yOffset = height;
        break;
      case CENTER:
        xOffset = width / 2;
        yOffset = height / 2;
    }
    return this;
  }
  public KSprite setAngle(float angle) {
    this.angle = angle;
    return this;
  }
  public KSprite setAngleOffset(float angleOffset) {
    this.angleOffset = angleOffset;
    return this;
  }
  // overload that takes a double to keep things clean
  public KSprite setAngleOffset(double angleOffset) {
    return setAngleOffset((float)angleOffset);
  }
  // scales based on the size of the image, *not* the current size setting
  public KSprite setScale(float scale) {
    return setSize(image.width * scale, image.height * scale);
  }
  // overload that takes a double to keep code in other files clean
  public KSprite setScale(double scale) {
    return setScale((float)scale);
  }

  /* renders the sprite to the given canvas */
  public void render(PGraphics pg) {
    pg.pushStyle();
    pg.imageMode(0);
    pg.pushMatrix();
    pg.translate(x, y);
    pg.rotate(angle + angleOffset);
    pg.image(image, -xOffset, -yOffset, width, height);
    pg.popMatrix();
    pg.popStyle();
  }

  public enum DisplayAnchor {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
  }
}