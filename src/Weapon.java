import processing.core.PVector;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

import static java.lang.Math.*;

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
    25,
    "devgun",
    "are you sure you should be reading this?"
  ),
  PISTOL(
    FireMode.SEMI,
    450,
    0,
    1800,
    8,
    1,
    1,
    0,
    50,
    "Pistol",
    "A basic handgun. High damage and accuracy with a reasonable rate of fire."
  ),

  AKM(
    FireMode.AUTO,
    600,
    0,
    1200,
    15,
    1,
    1,
    0,
    25,
    "AKM",
    "Every insurgent's favorite tool."
  ),

  SHOTGUN(
      FireMode.SEMI,
      360,
      0,
      1200,
      25,
      12,
      1,
      0,
      6,
      "SPAS-12",
      "According to Half-Life, the magazine tube is also a barrel."
  );

  private final FireMode fireMode;
  // only used for weapon descriptions
  private final float secondsPerRound, secondsPerBurst; // used for timing delay between shots and bursts
  private final int muzzleVelocity;
  private final int bulletsPerShot, shotsPerBurst;
  private final float spreadRange, halfSpreadRange;
  private final float recoilImpulse;
  private final float damagePerShot;
  public WeakReference<Player> player;
  private float fireTimer, burstTimer;
  private int shotsRemaining = 0;
  private boolean firing = false;
  private final Supplier<Boolean> inputChecker; // completely unnecessary functional interface
  private final String name, description;

  /* ctor */
  Weapon(FireMode fireMode, int roundsPerMinute, int burstsPerMinute, int muzzleVelocity, int spreadAngle,
         int bulletsPerShot, int shotsPerBurst, int recoilImpulse, float damagePerShot, String name,
         String description) {
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
    this.name = name;
    int accuracyPercentage = (int)(((float)(90 - spreadAngle) / 90) * 100);
    // if the weapon is a shotgun, the damage is displayed as "[damage per pellet]x[number of pellets]"
    String damageStr = Float.toString(damagePerShot);
    if (bulletsPerShot > 1) damageStr += "x" + Integer.toString(bulletsPerShot);
    this.description = String.format("%s\nDamage: %s\nAccuracy: %d%%\nRate of Fire: %d RPM",
                                     description, damageStr, accuracyPercentage, roundsPerMinute);

    // set input checker based on firemode
    if (this.fireMode == FireMode.AUTO) {
      inputChecker = () -> Input.isActive("fire auto");
    }
    else {
      inputChecker = () -> Input.isActive("fire semi");
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
        fireTimer -= Main.engine.deltaTime();
      }
    }
    else if (shotsPerBurst == 1) {
      if (inputChecker.get() && fireTimer <= 0) {
        fireShot();
        fireTimer = secondsPerRound;
      }
      else {
        fireTimer -= Main.engine.deltaTime();
      }
    }
    else {
      if (inputChecker.get() && burstTimer <= 0) {
        firing = true;
        shotsRemaining = shotsPerBurst;
      }
      else {
        burstTimer -= Main.engine.deltaTime();
      }
    }
  }

  /* fires bullets */
  private void fireShot() {
    for (int i = 0; i < bulletsPerShot; ++i) {
      float fireAngle = player.get().aimDirection + (float)(random() * spreadRange) - halfSpreadRange;
      PVector velocity = PVector.mult(PVector.fromAngle(fireAngle), muzzleVelocity);
      Main.engine.addEntity(new Bullet(player.get().position, velocity, damagePerShot, true));
      // if the weapon has recoil, apply it to the player
      if (recoilImpulse != 0) {
        PVector impulse = PVector.mult(PVector.fromAngle(fireAngle), -recoilImpulse);
        player.get().velocity.add(impulse);
      }
    }
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  /* determines what input mode is used */
  public enum FireMode {
    SEMI,
    AUTO,
  }
}