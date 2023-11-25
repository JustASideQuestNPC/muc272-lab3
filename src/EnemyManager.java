import processing.core.PVector;

import java.util.function.Function;

import static java.lang.Math.random;

/* invisible entity that spawns enemies */
public class EnemyManager extends KEntity {
  EnemyManager() {
    // hard-coded wave info for debugging
    EnemyType.CHASER.setNumInWave(Integer.MAX_VALUE);
    EnemyType.CHASER.setMaxActive(1);
  }

  public void update(float dt) {
    // do update checks for each enemy type
    for (EnemyType enemyType : EnemyType.values()) enemyType.update(dt);
  }

  /* I can't decide whether this is the most elegant piece of code I've ever written, or the most terrifying...probably
   * both. Ether way it's definitely my greatest and most blatant heresy. */
  public enum EnemyType {
    CHASER(
        ChaserEnemy::new,
        30,
        0.5f
    );


    private int numInWave; // how many of the enemy are left in the current wave
    private int numActive; // how many of the enemy are currently active in the engine
    private int maxActive; // how many of the enemy can be active in the engine at once
    private final int minSpawnX, minSpawnY, maxSpawnX, maxSpawnY; // used to prevent enemies from spawning inside walls
    private final Function<PVector, KEntity> enemyCtor; // the constructor for the enemy type
    private final float spawnInterval; // minimum time between entity spawns, in seconds
    private float spawnTimer;

    EnemyType(Function<PVector, KEntity> enemyCtor, int worldBorderMargin, float spawnInterval) {
      this.enemyCtor = enemyCtor;
      minSpawnX = worldBorderMargin;
      minSpawnY = worldBorderMargin;
      maxSpawnX = Main.WORLD_WIDTH - worldBorderMargin;
      maxSpawnY = Main.WORLD_HEIGHT - worldBorderMargin;
      this.spawnInterval = spawnInterval;
    }

    // spawns enemies if they need to be spawned
    public void update(float dt) {
      if (spawnTimer > 0) spawnTimer -= dt;
      else if (numActive < maxActive && numInWave > 0) {
        spawnEnemy();
      }
    }

    // spawns an enemy at a randomly generated spawn point
    private void spawnEnemy() {
      PVector spawnPos = new PVector();
      spawnPos.set(randInRange(minSpawnX, maxSpawnX), randInRange(minSpawnY, maxSpawnY));
      Main.engine.addEntity(enemyCtor.apply(spawnPos));
      ++numActive;
      --numInWave;
      spawnTimer = spawnInterval;
    }

    public void removeEnemy() {
      if (numActive > 0) --numActive;
    }

    public void setNumInWave(int numInWave) {
      this.numInWave = numInWave;
    }

    public void setMaxActive(int maxActive) {
      this.maxActive = maxActive;
    }

    // why did no one think that a "random in range" function was a good idea? if c++ has one then java has no excuse
    private int randInRange(int min, int max) {
      return (int)(random() * (max - min) + min);
    }
  }
}