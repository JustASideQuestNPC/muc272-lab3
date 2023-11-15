import processing.core.PVector;

import java.util.function.Supplier;

import static java.lang.Math.random;
import static java.lang.Math.toRadians;

// all player weapons
public enum Weapon {
  DEVGUN(
    FireMode.AUTO,
    1200,
    1500,
    15
  );

  private final FireMode fireMode;
  private final int roundsPerMinute;  // only used for weapon descriptions
  private final float secondsPerRound; // used for timing delay between firing
  private final int muzzleVelocity; // used for adding bullets when fire() is called
  private final int spreadAngle;
  private final float spreadRange, halfSpreadRange;
  private KEngine engine;
  public Player player;
  private float fireTimer;
  private final Supplier<Boolean> inputChecker;


  Weapon(FireMode fireMode, int roundsPerMinute, int muzzleVelocity, int spreadAngle) {
    this.fireMode = fireMode;
    this.roundsPerMinute = roundsPerMinute;
    secondsPerRound = 1 / (roundsPerMinute / 60f);
    this.muzzleVelocity = muzzleVelocity;
    this.spreadAngle = spreadAngle;
    spreadRange = (float)toRadians(spreadAngle);
    halfSpreadRange = spreadRange / 2;
    fireTimer = 0;

    // set input checker based on firemode
    if (this.fireMode == FireMode.SEMI) {
      inputChecker = () -> KInput.isActive("fire semi");
    }
    else {
      inputChecker = () -> KInput.isActive("fire auto");
    }
  }

  // checks for fire input and attempts to fire a bullet at an angle from a position
  public void doFireCheck() {
    if (fireTimer > 0) {
      fireTimer -= engine.deltaTime();
    }
    if (inputChecker.get() && fireTimer <= 0) {
      float fireAngle = player.aimDirection + (float)(random() * spreadRange) - halfSpreadRange;
      PVector velocity = PVector.mult(PVector.fromAngle(fireAngle), muzzleVelocity);
      engine.addEntity(new Bullet(player.position, velocity));
      fireTimer = secondsPerRound;
    }
  }

  // sets the engine reference, called once in setup
  public void setEngine(KEngine engine) {
    this.engine = engine;
  }

  // getters
  public int getRoundsPerMinute() {
    return roundsPerMinute;
  }

  public int getMuzzleVelocity() {
    return muzzleVelocity;
  }
  public FireMode getFireMode() {
    return fireMode;
  }
  public int getSpreadAngle() {
    return spreadAngle;
  }

  public enum FireMode {
    SEMI,
    AUTO
  }
}