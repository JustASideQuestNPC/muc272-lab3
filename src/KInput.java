import processing.core.PConstants;

import java.util.HashMap;

@SuppressWarnings("unused") // keeps my IDE happy
public class KInput {

  // holds all input bindings - hashmaps are like arrays, but they use another value as a key, instead of an index (in
  // this case a string, so that accessing a bind with a given name is super easy)
  private static final HashMap<String, Bind> inputBinds = new HashMap<>();

  // holds the state of every key on the keyboard (kind of). using an integer as a hashmap key feels like an array with
  // extra steps, but it actually saves a ton of memory because of how key codes work. the codes for different keys
  // are between 0 and 222, but only 96 of those are valid keys, so a basic array to hold all of their states would be
  // over twice as large as it needs to be.
  private static final HashMap<Integer, Boolean> keyStates = new HashMap<>();

  // binds added without specifying an activation mode in addInput will default to this activation mode. changing this
  // will not affect inputs that have already been added!
  public static BindMode defaultMode = BindMode.CONTINUOUS;

  // adds an input binding to the handler. if an input binding with the same name already exists, it is overwritten
  public static void addInput(String name, Key[] keys, BindMode mode) {
    inputBinds.put(name, new Bind(keys, mode));
  }
  // overloads to use a single key instead of an array, and to make the mode defaultable
  public static void addInput(String name, Key key, BindMode mode) {
    addInput(name, new Key[]{key}, mode);
  }
  public static void addInput(String name, Key[] keys) {
    addInput(name, keys, defaultMode);
  }
  public static void addInput(String name, Key key) {
    addInput(name, new Key[]{key}, defaultMode);
  }

  // updates all inputs
  public static void update() {
    // forEach takes a function (or a lambda, which is an unnamed, single-use function that's meant for things like
    // this), then loops through the container (in this case a hashmap) and calls that function on every item inside it
    inputBinds.forEach((s, bind) -> bind.update());
  }

  // returns the state of an input binding, throws an IllegalArgumentException if the name is invalid (in other words,
  // checking the state of a bind that doesn't exist will crash your program)
  public static boolean isActive(String name) throws IllegalArgumentException {
    // inputBinds.get will already throw an exception if given an invalid name, but i'm throwing my own here so that
    // it'll have a message with more useful info. you're welcome :)
    if (!inputBinds.containsKey(name)) {
      throw new IllegalArgumentException(String.format(
          "Cannot get whether the input \"%s\" is active because it does not exist!", name));
    }
    return inputBinds.get(name).isActive();
  }

  // returns the keys bound to an input binding - for technical reasons, this always returns an array even if only one
  // key is bound to the input. throws an IllegalArgumentException if the name is invalid
  public static Key[] getBoundKeys(String name) throws IllegalArgumentException {
    if (!inputBinds.containsKey(name)) {
      throw new IllegalArgumentException(String.format(
          "Cannot get the key(s) bound to the input \"%s\" is active because it does not exist!", name));
    }
    return inputBinds.get(name).getBoundKeys();
  }

  // sets which keys are bound to an input binding, overwriting the keys that are currently bound to it in the process.
  // throws an IllegalArgumentException if the name is invalid
  public static void setBoundKeys(String name, Key[] keys) throws IllegalArgumentException {
    if (!inputBinds.containsKey(name)) {
      throw new IllegalArgumentException(String.format(
          "Cannot set the key(s) bound to the input \"%s\" is active because it does not exist!", name));
    }
    inputBinds.get(name).setBoundKeys(keys);
  }
  // overload to use a single key instead of an array
  public static void setBoundKeys(String name, Key key) throws IllegalArgumentException {
    // no need to throw an exception here because the call to setBoundKeys will already run that check
    setBoundKeys(name, new Key[]{key});
  }

  // returns the activation mode of an input binding, throws an IllegalArgumentException if the name is invalid
  public static BindMode getBindMode(String name) throws IllegalArgumentException {
    if (!inputBinds.containsKey(name)) {
      throw new IllegalArgumentException(String.format(
          "Cannot get the activation mode for the input \"%s\" because it does not exist!", name));
    }
    return inputBinds.get(name).getMode();
  }

  // sets the activation mode of an input binding, throws an IllegalArgumentException if the name as invalid
  public static void setBindMode(String name, BindMode mode) throws IllegalArgumentException {
    if (!inputBinds.containsKey(name)) {
      throw new IllegalArgumentException(String.format(
          "Cannot set the activation mode for the input \"%s\" because it does not exist!", name));
    }
    inputBinds.get(name).setMode(mode);
  }

  // returns the state of a key
  public static boolean getKeyState(Key key) {
    return keyStates.getOrDefault(key.getCode(), false);
  }

  public static class Bind {
    private BindMode mode;
    private Key[] boundKeys;
    private boolean active, wasActive;

    // ctor
    Bind(Key[] keys, BindMode mode) {
      boundKeys = keys;

      this.mode = mode;
      active = false;
      wasActive = (mode == BindMode.RELEASE_ONLY);
    }

    // updates whether the bind should be active or not
    public void update() {
      // check if at least one key bound to the input is pressed
      boolean boundKeyPressed = false;
      for (Key k : boundKeys) {
        if (keyStates.getOrDefault(k.getCode(), false)) {
          boundKeyPressed = true;
          break;
        }
      }

      // update based on the activation mode
      if (mode == BindMode.CONTINUOUS) {
        active = boundKeyPressed;
      }
      else {
        if ((mode == BindMode.PRESS_ONLY && boundKeyPressed) ||
            (mode == BindMode.RELEASE_ONLY && !boundKeyPressed)) {
          if (!wasActive) {
            active = true;
            wasActive = true;
          }
          else {
            active = false;
          }
        }
        else {
          active = false;
          wasActive = false;
        }
      }
    }

    public boolean isActive() {
      return active;
    }

    // getters/setters
    public BindMode getMode() {
      return mode;
    }
    public void setMode(BindMode mode) {
      this.mode = mode;
    }
    public Key[] getBoundKeys() {
      return boundKeys;
    }
    public void setBoundKeys(Key[] boundKeys) {
      this.boundKeys = boundKeys;
    }
  }

  // activation modes for input binds
  public enum BindMode {
    CONTINUOUS,   // bind is active whenever it is pressed
    PRESS_ONLY,   // bind activates for a single frame when pressed
    RELEASE_ONLY  // bind activates for a single frame when released
  }

  // called in the Processing-specific pressed/released functions
  public static void pressKey(int code) {
    keyStates.put(code, true);
  }
  public static void releaseKey(int code) {
    keyStates.put(code, false);
  }
  public static void pressMouse(int button) {
    // switch-case statements are like if/else if chains, but *waaayy* faster
    switch (button) {
      case PConstants.LEFT:
        keyStates.put(Key.LEFT_MOUSE.getCode(), true);
        break;
      case PConstants.RIGHT:
        keyStates.put(Key.RIGHT_MOUSE.getCode(), true);
        break;
      case PConstants.CENTER:
        keyStates.put(Key.MIDDLE_MOUSE.getCode(), true);
        break;
    }
  }
  public static void releaseMouse(int button) {
    // switch-case statements are like if/else if chains, but *waaayy* faster
    switch (button) {
      case PConstants.LEFT:
        keyStates.put(Key.LEFT_MOUSE.getCode(), false);
        break;
      case PConstants.RIGHT:
        keyStates.put(Key.RIGHT_MOUSE.getCode(), false);
        break;
      case PConstants.CENTER:
        keyStates.put(Key.MIDDLE_MOUSE.getCode(), false);
        break;
    }
  }
}