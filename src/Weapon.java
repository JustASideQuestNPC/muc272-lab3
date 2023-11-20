import processing.core.PVector;

import java.util.function.Supplier;

import static java.lang.Math.random;
import static java.lang.Math.toRadians;

/* all player weapons - i'll give java the win here, its enums are way cooler than c++ enums */
public enum Weapon {
  DEVGUN(
    FireMode.AUTO,
    600,
    0,
    1200,
    0,
    1,
    1,
    0,
    25
  );

  private final FireMode fireMode;
  // only used for weapon descriptions
  private final float secondsPerRound, secondsPerBurst; // used for timing delay between shots and bursts
  private final int muzzleVelocity;
  private final int bulletsPerShot, shotsPerBurst;
  private final float spreadRange, halfSpreadRange;
  private final float recoilImpulse;
  private final int damagePerShot;
  private KEngine engine;
  public Player player;
  private float fireTimer, burstTimer;
  private int shotsRemaining = 0;
  private boolean firing = false;
  private final Supplier<Boolean> inputChecker; // completely unnecessary functional interface


  /* ctor */
  Weapon(FireMode fireMode, int roundsPerMinute, int burstsPerMinute, int muzzleVelocity, int spreadAngle,
         int bulletsPerShot, int shotsPerBurst, int recoilImpulse, int damagePerShot) {
    this.fireMode = fireMode;
    secondsPerRound = 1 / (roundsPerMinute / 60f);
    secondsPerBurst = 1 / (burstsPerMinute / 60f);
    this.muzzleVelocity = muzzleVelocity;
    spreadRange = (float)toRadians(spreadAngle);
    halfSpreadRange = spreadRange / 2;
    fireTimer = 0;
    this.bulletsPerShot = bulletsPerShot;
    this.shotsPerBurst = shotsPerBurst;
    this.recoilImpulse = recoilImpulse;
    this.damagePerShot = damagePerShot;

    // set input checker based on firemode
    if (this.fireMode == FireMode.AUTO) {
      inputChecker = () -> KInput.isActive("fire auto");
    }
    else {
      inputChecker = () -> KInput.isActive("fire semi");
    }
  }

  /* checks for fire input and attempts to fire a bullet at an angle from a position */
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

  /* fires bullets */
  private void fireShot() {
    for (int i = 0; i < bulletsPerShot; ++i) {
      float fireAngle = player.aimDirection + (float)(random() * spreadRange) - halfSpreadRange;
      PVector velocity = PVector.mult(PVector.fromAngle(fireAngle), muzzleVelocity);
      engine.addEntity(new Bullet(player.position, velocity, damagePerShot, true));
      // if the weapon has recoil, apply it to the player
      if (recoilImpulse != 0) {
        PVector impulse = PVector.mult(PVector.fromAngle(fireAngle), -recoilImpulse);
        player.velocity.add(impulse);
      }
    }
  }

  /* sets the engine reference, called once in setup */
  public void setEngine(KEngine engine) {
    this.engine = engine;
  }

  /* determines what input mode is used */
  @SuppressWarnings("unused")
  public enum FireMode {
    SEMI,
    AUTO,
  }
}