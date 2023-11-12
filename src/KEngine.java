import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;

public class KEngine {
  private final ArrayList<KEntity> entities; // holds all entities
  private final PGraphics canvas; // all entities are drawn to this
  private long currentTime;
  private float dt; // delta time (time since last frame)
  public boolean dtAsSeconds = true; // whether delta time is stored and passed as seconds or milliseconds

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
  public <T extends KEntity> T addEntity(T entity) {
    entity.engine = this; // all entities have a reference to the engine that contains them
    entities.add(entity);
    return entity;
  }

  // renders all active entities to the canvas
  public void render() {
    for (KEntity ent : entities) {
      ent.render(canvas);
    }
  }

  // updates delta time and entities
  public void update() {
    // update delta time
    long duration = System.nanoTime() - currentTime;
    currentTime = System.nanoTime();
    dt = (dtAsSeconds ? duration / 1000000000f : duration / 1000000f);

    // update all entities
    for (KEntity ent : entities) {
      ent.update(dt);
    }
  }

  // returns the current time delta
  public float deltaTime() {
    return dt;
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
}