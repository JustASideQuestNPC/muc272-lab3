import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

/* handles displaying and rotating an image sprite */
public class KSprite {
  public static Main app;
  private final PImage image;
  private int imageMode = 0; // Processing would prefer I use CORNER, but CORNER is actually just 0
  private float x, y, width, height, angle, angleOffset;

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
    return this;
  }
  public KSprite setImageMode(int imageMode) throws IllegalArgumentException {
    if (imageMode != 0 && imageMode != 3) {
      throw new IllegalArgumentException(String.format("Invalid image mode (expected 0 (CORNER) or 3 (CENTER)," +
                                                           "recieved %d)\nNote: image mode 1 (CORNERS) is not supported" +
                                                           "for technical reasons", imageMode));
    }
    this.imageMode = imageMode;
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
  // overload that takes a double to keep things clean
  public KSprite setScale(double scale) {
    return setScale((float)scale);
  }

  /* renders the sprite to the given canvas */
  public void render(PGraphics pg) {
    pg.pushStyle();
    pg.imageMode(imageMode);
    pg.pushMatrix();
    pg.translate(x, y);
    pg.rotate(angle + angleOffset);
    pg.image(image, 0, 0, width, height);
    pg.popMatrix();
    pg.popStyle();
  }
}