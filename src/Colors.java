/* the color palette for this entire project - based on avianAnnihilator's Sirens at Night palette
 * (lospec.com/palette-list/sirens-at-night) with an extra highlight added */
public enum Colors {
  // original palette
  WHITE(0xffdaf2e9),
  LIGHTER_TEAL(0xffb7e6da),
  LIGHT_TEAL(0xff95e0cc),
  TEAL(0xff39707a),
  DARK_TEAL(0xff23495d),
  BLACK(0xff1c2638),
  RED(0xff9b222b),
  PINK(0xfff14e52),
  // half-transparent versions
  TRANS_WHITE(0x80daf2e9),
  TRANS_LIGHTER_TEAL(0x80b7e6da),
  TRANS_LIGHT_TEAL(0x8095e0cc),
  TRANS_TEAL(0x8039707a),
  TRANS_DARK_TEAL(0x8023495d),
  TRANS_BLACK(0x801c2638),
  TRANS_RED(0x809b222b),
  TRANS_PINK(0x80f14e52),
  // completely transparent color, technically not part of the palette but it makes my life easier
  TRANSPARENT(0x00000000);
  private final int code;
  private final String stringEscapeCode;

  Colors(int code) {
    this.code = code;
    // generate an escape code for text formatting using some fancy bit shifting
    int alpha = 256 + ((code & 0xff000000) >> 24);
    int red = (code & 0xff0000) >> 16;
    int green = (code & 0xff00) >> 8;
    int blue = (code & 0xff);
    if (alpha == 0x80) {
      red /= 2;
      green /= 2;
      blue /= 2;
    }
    stringEscapeCode = String.format("\u001b[38;2;%d;%d;%dm", red, green, blue);
  }
  public int getCode() {
    return code;
  }
  // that's right, even the console output uses this palette!
  public String formatString(String str) {
    return stringEscapeCode + str + "\u001b[39m";
  }
}