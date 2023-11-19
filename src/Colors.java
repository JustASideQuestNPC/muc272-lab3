/* the color palette for this entire project - based on avianAnnihilator's Sirens at Night palette
 * (lospec.com/palette-list/sirens-at-night) with an extra shade of teal added */
public enum Colors {
  // original palette
  WHITE(0xffdaf2e9),
  LIGHTER_TEAL(0xffb7e6da),
  LIGHT_TEAL(0xff95e0cc),
  MEDIUM_TEAL(0xff39707a),
  DARK_TEAL(0xff23495d),
  BLACK(0xff1c2638),
  DARK_RED(0xff9b222b),
  RED(0xfff14e52),
  // half-transparent versions
  TRANS_WHITE(0x80daf2e9),
  TRANS_LIGHTER_TEAL(0x80b7e6da),
  TRANS_LIGHT_TEAL(0x8095e0cc),
  TRANS_MEDIUM_TEAL(0x8039707a),
  TRANS_DARK_TEAL(0x8023495d),
  TRANS_BLACK(0x801c2638),
  TRANS_DARK_RED(0x809b222b),
  TRANS_RED(0x80f14e52),
  // completely transparent color, technically not part of the palette but it makes my life easier
  TRANSPARENT(0x00000000);

  private final int code;
  Colors(int code) {
    this.code = code;
  }
  public int getCode() {
    return code;
  }
}