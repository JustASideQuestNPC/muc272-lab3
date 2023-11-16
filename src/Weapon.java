import processing.core.PVector;

import java.util.function.Supplier;

import static java.lang.Math.random;
import static java.lang.Math.toRadians;

// all player weapons
public enum Weapon {
  DEVGUN(
    FireMode.AUTO,
    1800,
    180,
    1500,
    5,
    1,
    3
  );

  private final FireMode fireMode;
  private final int roundsPerMinute;  // only used for weapon descriptions
  private final float secondsPerRound, secondsPerBurst; // used for timing delay between firing and bursts
  private final int muzzleVelocity; // used for adding bullets when fire() is called
  private final int spreadAngle;
  private final int bulletsPerShot, shotsPerBurst;
  private final float spreadRange, halfSpreadRange;
  private KEngine engine;
  public Player player;
  private float fireTimer, burstTimer;
  private int shotsRemaining = 0;
  private boolean firing = false;
  private final Supplier<Boolean> inputChecker;


  Weapon(FireMode fireMode, int roundsPerMinute, int burstsPerMinute, int muzzleVelocity, int spreadAngle,
         int bulletsPerShot, int shotsPerBurst) {
    this.fireMode = fireMode;
    this.roundsPerMinute = roundsPerMinute;
    secondsPerRound = 1 / (roundsPerMinute / 60f);
    secondsPerBurst = 1 / (burstsPerMinute / 60f);
    this.muzzleVelocity = muzzleVelocity;
    this.spreadAngle = spreadAngle;
    spreadRange = (float)toRadians(spreadAngle);
    halfSpreadRange = spreadRange / 2;
    fireTimer = 0;
    this.bulletsPerShot = bulletsPerShot;
    this.shotsPerBurst = shotsPerBurst;

    // set input checker based on firemode
    if (this.fireMode == FireMode.AUTO) {
      inputChecker = () -> KInput.isActive("fire auto");
    }
    else {
      inputChecker = () -> KInput.isActive("fire semi");
    }
  }

  // checks for fire input and attempts to fire a bullet at an angle from a position
  public void doFireCheck() {
    // continue firing a burst if one is currently being fired
    if (firing) {
      if (fireTimer <= 0) {
        fireShot();
        if (--shotsRemaining == 0) {
          burstTimer = secondsPerBurst;
          firing = false;
        }
        else {
          fireTimer = secondsPerRound;
        }
      }
      else {
        fireTimer -= engine.deltaTime();
      }
    }
    else if (shotsPerBurst == 1) {
      if (inputChecker.get() && fireTimer <= 0) {
        fireShot();
        fireTimer = secondsPerRound;
      }
      else {
        fireTimer -= engine.deltaTime();
      }
    }
    else {
      if (inputChecker.get() && burstTimer <= 0) {
        firing = true;
        shotsRemaining = shotsPerBurst;
      }
      else {
        burstTimer -= engine.deltaTime();
      }
    }
  }

  // fires bullets
  private void fireShot() {
    for (int i = 0; i < bulletsPerShot; ++i) {
      float fireAngle = player.aimDirection + (float)(random() * spreadRange) - halfSpreadRange;
      PVector velocity = PVector.mult(PVector.fromAngle(fireAngle), muzzleVelocity);
      engine.addEntity(new Bullet(player.position, velocity));
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
    AUTO,
  }
}