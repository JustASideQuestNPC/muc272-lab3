import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;

/* template class for entities in a game engine */
@SuppressWarnings("unused") // keeps my ide happy
public abstract class GameEntity {
  protected final ArrayList<String> tags; // tags, can be used for almost anything
  public Engine engine; // the engine containing the entity
  public EnemyManager enemyManager; // used if the entity is an enemy for keeping entity limits correct
  public Collider.Hitbox[] colliders; // included to prevent object slicing, does not need to be initialized
  public boolean markForDelete = false;
  public PVector position = new PVector(0, 0);
  protected float currentHealth;
  protected Sprite sprite;

  /* constants for debugging */
  public static boolean SHOW_COLLIDERS = false;
  private static final int COLLIDER_COLOR = 0xffff00ff;
  private static final int COLLIDER_STROKE_WEIGHT = 2;

  /* default ctor, intializes tags to an empty array */
  GameEntity() {
    tags = new ArrayList<>();
  }

  /* ctor with tags */
  GameEntity(String... tags) {
    this.tags = new ArrayList<>(Arrays.asList(tags));
  }

  /* renders the entity to pg. by default it renders the sprite if it has been created, otherwise it does nothing */
  public void render(PGraphics pg) {
    if (sprite != null) sprite.render(pg);
    if (SHOW_COLLIDERS && colliders != null) {
      for (Collider.Hitbox collider : colliders) collider.render(pg, COLLIDER_COLOR, COLLIDER_STROKE_WEIGHT);
    }
  }

  /* updates the entity, is passed the time since the last update; does nothing by default. */
  public void update(float dt) {}

  /* returns whether the entity has the specified tag */
  public final boolean hasTag(String tag) {
    return tags.contains(tag);
  }

  public final boolean colliding(GameEntity other, PVector transVec) {
    if (this.colliders != null && other.colliders != null) {
      for (Collider.Hitbox thisCollider : this.colliders) {
        for (Collider.Hitbox otherCollider : other.colliders) {
          if (Collider.colliding(thisCollider, otherCollider, transVec)) return true;
        }
      }
    }
    return false;
  }
  public final boolean colliding(GameEntity other) {
    for (Collider.Hitbox thisCollider : this.colliders) {
      for (Collider.Hitbox otherCollider : other.colliders) {
        if (Collider.colliding(thisCollider, otherCollider)) return true;
      }
    }
    return false;
  }

  public final void setColliderPos(PVector pos) {
    if (this.colliders != null) {
      for (Collider.Hitbox collider : colliders) {
        collider.setPos(pos);
      }
    }
  }

  public final void setColliderAngle(float angle) {
    if (this.colliders != null) {
      for (Collider.Hitbox collider : colliders) {
        collider.setAngle(angle);
      }
    }
  }

  public final float getCurrentHealth() {
    return currentHealth;
  }

  /* deals damage to the entity */
  public void damage(float dmg) {
    currentHealth -= dmg;
    if (currentHealth <= 0) markForDelete = true;
  }

  /* runs once when the entity is deleted by the engine; does nothing by default */
  public void runOnDeath() {}

  /* returns whether the entity is onscreen, almost certainly produces some false negatives */
  public final boolean isOnscreen() {
    PVector onscreenPos = PVector.add(PVector.sub(position, engine.getCameraPos()), engine.getCameraOffset());
    return onscreenPos.x > 0 && onscreenPos.x < Main.WINDOW_WIDTH
        && onscreenPos.y > 0 && onscreenPos.y < Main.WINDOW_HEIGHT;
  }
}