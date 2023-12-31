@SuppressWarnings("unused") // keeps my IDE happy
public enum Key {
  BACKSPACE(8),
  TAB(9),
  ENTER(10),
  SHIFT(16),
  CTRL(17), CONTROL(17),
  ALT(18),
  ESCAPE(27), ESC(27),
  SPACE(32), SPACEBAR(32),
  PAGE_UP(33),
  PAGE_DOWN(34),
  END(35),
  HOME(36),
  LEFT(37), LEFT_ARROW(37),
  UP(38), UP_ARROW(38),
  RIGHT(39), RIGHT_ARROW(39),
  DOWN(40), DOWN_ARROW(40),
  COMMA(44),
  DASH(45), MINUS(45),
  PERIOD(46),
  SLASH(47), FORWARD_SLASH(47),
  ZERO(48),
  ONE(49),
  TWO(50),
  THREE(51),
  FOUR(52),
  FIVE(53),
  SIX(54),
  SEVEN(55),
  EIGHT(56),
  NINE(57),
  SEMICOLON(58),
  EQUALS(61),
  A(65),
  B(66),
  C(67),
  D(68),
  E(69),
  F(70),
  G(71),
  H(72),
  I(73),
  J(74),
  K(75),
  L(76),
  M(77),
  N(78),
  O(79),
  P(80),
  Q(81),
  R(82),
  S(83),
  T(84),
  U(85),
  V(86),
  W(87),
  X(88),
  Y(89),
  Z(90),
  OPEN_BRACKET(91), LEFT_BRACKET(91),
  BACKSLASH(92),
  CLOSE_BRACKET(93), RIGHT_BRACKET(93),
  NUMPAD_ZERO(96),
  NUMPAD_ONE(97),
  NUMPAD_TWO(98),
  NUMPAD_THREE(99),
  NUMPAD_FOUR(100),
  NUMPAD_FIVE(101),
  NUMPAD_SIX(102),
  NUMPAD_SEVEN(103),
  NUMPAD_EIGHT(104),
  NUMPAD_NINE(105),
  NUMPAD_STAR(106),
  NUMPAD_PLUS(107),
  NUMPAD_COMMA(108),
  NUMPAD_MINUS(109),
  NUMPAD_PERIOD(110),
  NUMPAD_SLASH(111),
  F1(112),
  F2(113),
  F3(114),
  F4(115),
  F5(116),
  F6(117),
  F7(118),
  F8(119),
  F9(120),
  F10(121),
  F11(122),
  F12(123),
  DELETE(127),
  NUM_LOCK(144),
  SCROLL_LOCK(145),
  INSERT(155),
  BACKTICK(192),
  QUOTE(222), SINGLE_QUOTE(222),
  LEFT_MOUSE(0),
  RIGHT_MOUSE(1),
  MIDDLE_MOUSE(2);
  private final int code;

  Key(int code) {
    this.code = code;
  }

  int getCode() {
    return code;
  }
}