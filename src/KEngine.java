import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings("unused") // keeps my ide happy
public final class KEngine {
  private final ArrayList<KEntity> entities; // holds all entities
  private final PGraphics canvas; // all entities are drawn to this
  private long currentTime;
  private float dt; // delta time (time since last frame)
  private boolean dtAsSeconds = true; // whether delta time is stored and passed as seconds or milliseconds
  private final PVector cameraPos = new PVector(0, 0);
  private final PVector cameraTarget = new PVector(0, 0);
  private final PVector cameraOffset = new PVector(0, 0);
  private float cameraTightness = 1;
  private boolean cameraEnabled = false;

  private final String ansiCodeYellow = "";

  // getters/setters
  public void setCameraPos(PVector cameraPos) {
    this.cameraPos.set(cameraPos);
    // update camera target to prevent movement on the next update
    cameraTarget.set(cameraPos);
  }

  public void setCameraTarget(PVector cameraTarget) {
    this.cameraTarget.set(cameraTarget);
  }

  public void setCameraTightness(float cameraTightness) {
    this.cameraTightness = cameraTightness;
  }

  public void setCameraEnabled(boolean cameraEnabled) {
    this.cameraEnabled = cameraEnabled;
  }

  public void setCameraOffset(PVector cameraOffset) {
    this.cameraOffset.set(cameraOffset);
  }

  // overloads that take separate coordinates instead of PVectors
  public void setCameraPos(float x, float y) {
    setCameraPos(new PVector(x, y));
  }

  public void setCameraTarget(float x, float y) {
    setCameraTarget(new PVector(x, y));
  }

  public void setCameraOffset(float x, float y) {
    setCameraOffset(new PVector(x, y));
  }

  public PVector getCameraPos() {
    return cameraPos;
  }
  public PVector getCameraTarget() {
    return cameraTarget;
  }
  public PVector getCameraOffset() {
    return cameraOffset;
  }

  // ctors
  KEngine(PGraphics pg) {
    entities = new ArrayList<>();
    canvas = pg;
    // start timer for tracking time delta
    currentTime = System.nanoTime();
  }

  KEngine(PApplet app) {
    this(app.getGraphics());
  }

  // adds an entity to the entity list, then returns a reference to that entity
  @SuppressWarnings("UnusedReturnValue") // keeps my ide happy
  public <T extends KEntity> T addEntity(T entity) {
    entity.engine = this; // all entities have a reference to the engine that contains them
    entities.add(entity);
    return entity;
  }

  // renders all active entities to the canvas
  public void render() {
    canvas.pushMatrix();
    canvas.translate(-cameraPos.x + cameraOffset.x, -cameraPos.y + cameraOffset.y);
    for (KEntity ent : entities) {
      ent.render(canvas);
    }
    canvas.popMatrix();
  }

  // updates delta time and entities
  public void update() {
    // update delta time
    long duration = System.nanoTime() - currentTime;
    currentTime = System.nanoTime();
    dt = (dtAsSeconds ? duration / 1000000000f : duration / 1000000f);

    // update all entities
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < entities.size(); ++i) {
      entities.get(i).update(dt);
    }

    // remove deleted entities
    entities.removeIf((ent) -> ent.markForDelete);

    if (cameraEnabled) updateCamera();
  }

  // returns the current time delta
  public float deltaTime() {
    return dt;
  }

  // returns all entities with the given tag (if there are no entities with that tag, returns an empty list)
  public ArrayList<KEntity> getTagged(String tag) {
    ArrayList<KEntity> tagged = new ArrayList<>();
    for (KEntity ent : entities) {
      if (ent.hasTag(tag)) tagged.add(ent);
    }
    return tagged;
  }

  // returns the number of active entities
  public int getNumEntities() {
    return entities.size();
  }

  // sets how delta time is stored and passed either "seconds" or "milliseconds" (case insensitive)
  public void setDtMode(String mode) throws IllegalArgumentException {
    mode = mode.toLowerCase(); // i die inside a little every time someone has case-sensitive arguments
    if (mode.equals("seconds")) dtAsSeconds = true;
    else if (mode.equals("milliseconds")) dtAsSeconds = false;
    else throw new IllegalArgumentException(
        String.format("Invalid delta time mode: Expected \"seconds\" or \"milliseconds\", received \"%s\"", mode)
      );
  }

  // moves the camera toward the target based on the camera's tightness
  private void updateCamera() {
    cameraPos.set(PVector.lerp(cameraPos, cameraTarget, cameraTightness));
  }
}