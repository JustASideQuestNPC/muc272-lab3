import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Arrays;

// template class for entities in a game engine. should never be created directly
public abstract class KEntity {
  protected final ArrayList<String> tags; // tags, can be used for almost anything
  public KEngine engine; // the engine containing the entity

  // default ctor, intializes tags to an empty array
  KEntity() {
    tags = new ArrayList<>();
  }

  // ctor with tags
  KEntity(String... args) {
    tags = new ArrayList<>(Arrays.asList(args));
  }

  // renders the entity to pg. does nothing by default.
  public void render(PGraphics pg) {}

  // updates the entity, is passed the time since the last update. does nothing by default.
  public void update(float deltaTime) {}

  // returns whether the entity has the specified tag
  public final boolean hasTag(String tag) {
    return tags.contains(tag);
  }
}