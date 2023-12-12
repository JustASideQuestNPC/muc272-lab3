import processing.core.PApplet;
import processing.sound.SoundFile;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

/* manages playback for all sound effects */
public class SoundManager {
  private static final Map<String, String> filePaths = Map.ofEntries(
    entry("game over", "sounds/placeholder_gameover.wav"),
    entry("get upgrade", "sounds/placeholder_getupgrade.wav"),
    entry("shoot", "sounds/placeholder_shoot.wav"),
    entry("take damage", "sounds/placeholder_takedamage.wav"),
    entry("wave complete", "sounds/placeholder_wavecomplete.wav"),
    entry("run complete", "sounds/placeholder_runcomplete.wav")
  );

  private static final Map<String, SoundFile> soundFiles = new HashMap<>();

  /* loads all sound effects into soundfiles that can be played */
  public static void init(PApplet app) {
    filePaths.forEach((k, v) -> soundFiles.put(k, new SoundFile(app, v)));
  }

  /* plays a sound effect if it isn't already playing; throws an exception if the effect doesn't exist. */
  public static void play(String name) throws IllegalArgumentException {
    if (!soundFiles.containsKey(name)) throw new IllegalArgumentException(String.format(
        "The sound effect \"%s\" does not exist!", name
    ));

    SoundFile effect = soundFiles.get(name);
    if (!effect.isPlaying()) effect.play();
  }
}