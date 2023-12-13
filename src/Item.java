import java.util.function.Consumer;

/* all non-weapon upgrades that the player can get at the end of each wave */
public enum Item {
  FULL_SPEED_TIME_SLOW(
      "Sensory Booster",
      "Move at full speed during Adrenaline.",
      true,
      (player) -> player.addTag("dt mult exempt"),
      null,
      null,
      null
  ),
  STAMINA_ON_TAKE_DAMAGE(
      "Impact Converter",
      "Taking damage restores Energy.",
      true,
      null,
      null,
      (player, damage) -> {
        player.currentStamina += damage * 10;
        if (player.currentStamina > player.maxStamina) player.currentStamina = player.maxStamina;
      },
      null
  ),
  HEALTH_ON_DEAL_DAMAGE(
      "Blood Siphon",
      "Dealing damage restores a small amount of health.",
      true,
      null,
      (player, damage) -> {
        player.currentHealth += damage / 2;
        if (player.currentHealth > player.maxHealth) player.currentHealth = player.maxHealth;
      },
      null,
      null
  ),
  STAMINA_ON_DEAL_DAMAGE(
      "Energy Siphon",
      "Dealing damage restores a small amount of Energy.",
      true,
      null,
      (player, damage) -> {
        player.currentStamina += damage * 2;
        if (player.currentStamina > player.maxStamina) player.currentStamina = player.maxStamina;
      },
      null,
      null
  ),
  MAX_STAMINA_BOOST(
      "Extra Batteries",
      "Permanently increase your maximum Energy.",
      false,
      (player) -> {
        player.maxStamina += 200;
        player.currentStamina += 200;
      },
      null,
      null,
      null
  ),
  MAX_HEALTH_BOOST(
      "Reinforced Hull",
      "Permanently increase your maximum health.",
      false,
      (player) -> {
        player.maxHealth += 20;
        player.currentHealth += 20;
      },
      null,
      null,
      null
  ),
  MOVE_SPEED_BOOST(
      "Improved Thrusters",
      "Permanently increase your maximum speed.",
      false,
      (player) -> player.modVelocitySoftCap(50),
      null,
      null,
      null
  );

  private final Consumer<Player> equipLambda;
  private final boolean unique; // if true, the equipment can only be taken once
  private final BiConsumer<Player, Float> dealDamageLambda, takeDamageLambda;
  private final BiConsumer<Player, GameEntity> killEnemyLambda;
  private final String name, description;

  Item(String name, String description, boolean unique, Consumer<Player> equipLambda,
       BiConsumer<Player, Float> dealDamageLambda, BiConsumer<Player, Float> takeDamageLambda,
       BiConsumer<Player, GameEntity> killEnemyLambda) {
    this.name = name;
    this.description = description;
    this.unique = unique;
    this.equipLambda = (equipLambda != null ? equipLambda : (player) -> {});
    this.dealDamageLambda = (dealDamageLambda != null ? dealDamageLambda : (player, damage) -> {});
    this.takeDamageLambda = (takeDamageLambda != null ? takeDamageLambda : (player, damage) -> {});
    this.killEnemyLambda = (killEnemyLambda != null ? killEnemyLambda : (player, enemy) -> {});
  }

  // methods for calling each lambda
  public void onEquip(Player player) {
    equipLambda.accept(player);
  }
  public void onDealDamage(Player player, float damage) {
    dealDamageLambda.accept(player, damage);
  }
  public void onTakeDamage(Player player, float damage) {
    takeDamageLambda.accept(player, damage);
  }
  public void onKillEnemy(Player player, GameEntity enemy) {
    killEnemyLambda.accept(player, enemy);
  }

  public String getName() {
    return name;
  }
  public String getDescription() {
    return description;
  }
  public boolean isUnique() {
    return unique;
  }

  // normally this would be where i'd rant about how java should have this...but in this case i don't really mind that
  // it doesn't. all the most common interface formats are covered and new ones obviously aren't hard to implement.
  @FunctionalInterface
  interface BiConsumer <T, U> {
    void accept(T arg1, U arg2);
  }
}