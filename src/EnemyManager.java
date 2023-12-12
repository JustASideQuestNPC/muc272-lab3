import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/* invisible entity that spawns enemies */
public class EnemyManager extends GameEntity {
  private final JSONArray allWaveData; // json data for all waves
  private boolean completed;
  private int remainingEnemies;

  // cache a list of all enemy types to prevent copying this on every frame
  private static final List<EnemyType> enemyTypes = List.of(EnemyType.values());

  EnemyManager(JSONArray allWaveData) {
    // "purge exempt" ensures the manager will be in the engine for all of runtime
    super("purge exempt");

    this.allWaveData = allWaveData;
  }

  // loads a wave, throws an IllegalArgumentException if the wave at that number doesn't exist
  public void loadWave(int waveNum) throws IllegalArgumentException {
    if (waveNum < 0 || waveNum >= allWaveData.size()) {
      throw new IllegalArgumentException(String.format("Wave %d does not exist!", waveNum));
    }
    JSONObject waveData = allWaveData.getJSONObject(waveNum);
    for (EnemyType enemyType : enemyTypes) {
      if (waveData.hasKey(enemyType.getName())) {
        JSONObject enemyData = waveData.getJSONObject(enemyType.getName());
        enemyType.setNumInWave(enemyData.getInt("total"));
        enemyType.setMaxActive(enemyData.getInt("max active"));
      }
      else {
        enemyType.setNumInWave(0);
        enemyType.setMaxActive(0);
      }
    }
  }

  public void update(float dt) {
    completed = !Main.FORCE_DEBUG_WAVE;
    remainingEnemies = 0;
    // do update checks for each enemy type, also check if the wave has been completed
    for (EnemyType enemyType : EnemyType.values()) {
      enemyType.update(dt);
      remainingEnemies += enemyType.getNumInWave();
      if (!enemyType.completed) completed = false;
    }
  }

  public boolean waveFinished() {
    return completed;
  }

  public int getRemainingEnemies() {
    return remainingEnemies;
  }

  /* I can't decide whether this is the most elegant piece of code I've ever written, or the most terrifying...probably
   * both. Ether way it's definitely my greatest and most blatant heresy. */
  public enum EnemyType {
    CHASER(
        "chaser",
        ChaserEnemy::new,
        100,
        0.5f,
        400,
        10000
    ),
    TURRET(
        "turret",
        TurretEnemy::new,
        200,
        1,
        750,
        10000
    );

    private final String name; // name of the enemy, used for loading wave data
    private int numInWave; // how many of the enemy are left in the current wave
    private int numActive; // how many of the enemy are currently active in the engine
    private int maxActive; // how many of the enemy can be active in the engine at once
    private boolean completed; // whether all of the enemy for this wave have spawned and been killed
    private final int minSpawnX, minSpawnY, maxSpawnX, maxSpawnY; // used to prevent enemies from spawning inside walls
    private final int minPlayerDistanceSq, maxPlayerDistanceSq;
    private final Function<PVector, GameEntity> enemyCtor; // the constructor for the enemy type
    private final float spawnInterval; // minimum time between entity spawns, in seconds
    private float spawnTimer;

    EnemyType(String name, Function<PVector, GameEntity> enemyCtor, int worldBorderMargin, float spawnInterval,
              int minPlayerDistance, int maxPlayerDistance) {
      this.name = name;
      this.enemyCtor = enemyCtor;
      minSpawnX = worldBorderMargin;
      minSpawnY = worldBorderMargin;
      maxSpawnX = Main.WORLD_WIDTH - worldBorderMargin;
      maxSpawnY = Main.WORLD_HEIGHT - worldBorderMargin;
      this.spawnInterval = spawnInterval;

      minPlayerDistanceSq = minPlayerDistance * minPlayerDistance;
      maxPlayerDistanceSq = maxPlayerDistance * maxPlayerDistance;
    }

    // spawns enemies if they need to be spawned
    public void update(float dt) {
      completed = numActive == 0 && numInWave == 0;
      if (spawnTimer > 0) spawnTimer -= dt;
      else if (numActive < maxActive && numInWave > 0) {
        spawnEnemy();
      }
    }

    // spawns an enemy at a randomly generated spawn point
    private void spawnEnemy() {
      PVector spawnPos = new PVector();
      PVector playerDist = new PVector();
      // do-while loops are the same as while loops, but they check the condition after running, which means they're
      // guaranteed to run the code inside them at least once
      do {
        spawnPos.set(Main.randInt(minSpawnX, maxSpawnX), Main.randInt(minSpawnY, maxSpawnY));
        playerDist.set(PVector.sub(spawnPos, Objects.requireNonNull(Main.player.get()).position));
      } while (playerDist.magSq() < minPlayerDistanceSq || playerDist.magSq() > maxPlayerDistanceSq);

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

    public int getNumInWave() {
      return numInWave;
    }

    public String getName() {
      return name;
    }
  }
}