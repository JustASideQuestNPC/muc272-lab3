import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;

import static processing.core.PGraphics.*;

/* an interactable button for use with the hud */
public class Button {
  private int x, y, width, height, strokeWeight, strokeColor, fillColor, textColor;
  private int hoveredFillColor = -1, hoveredStrokeColor = -1, hoveredTextColor = -1, hoveredStrokeWeight = -1;
  private int pressedFillColor = -1, pressedStrokeColor = -1, pressedTextColor = -1, pressedStrokeWeight = -1;
  private int textAlignX, textAlignY, textX, textY;
  private String text;
  private PFont font;
  private final PGraphics pg;
  private boolean hovered, pressed;

  /* ctor */
  Button(PGraphics pg) {
    this.pg = pg;
  }

  /* buttons have way too many variables to put them all in the constructor, but builder pattern go brrr :) */
  public Button setPos(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }
  public Button setSize(int width, int height) {
    this.width = width;
    this.height = height;
    return this;
  }
  public Button setText(String text) {
    this.text = text;
    return this;
  }
  public Button setTextAlign(int textAlignX, int textAlignY) {
    this.textAlignX = textAlignX;
    if (textAlignX == LEFT) textX = 0;
    else if (textAlignX == CENTER) textX = width / 2;
    else textX = width;

    this.textAlignY = textAlignY;
    if (textAlignY == TOP) textY = 0;
    else if (textAlignY == CENTER) textY = height / 2;
    else textY = height;
    return this;
  }
  public Button setFont(PFont font) {
    this.font = font;
    return this;
  }
  public Button setTextPos(int x, int y) {
    this.textX = x;
    this.textY = y;
    return this;
  }
  public Button setStrokeWeight(int strokeWeight) {
    this.strokeWeight = strokeWeight;
    hoveredStrokeWeight = strokeWeight;
    pressedStrokeWeight = strokeWeight;
    return this;
  }
  public Button setStrokeColor(Colors strokeColor) {
    this.strokeColor = strokeColor.getCode();
    hoveredStrokeColor = strokeColor.getCode();
    pressedStrokeColor = strokeColor.getCode();
    return this;
  }
  public Button setFillColor(Colors fillColor) {
    this.fillColor = fillColor.getCode();
    hoveredFillColor = fillColor.getCode();
    pressedFillColor = fillColor.getCode();
    return this;
  }
  public Button setTextColor(Colors textColor) {
    this.textColor = textColor.getCode();
    hoveredTextColor = textColor.getCode();
    pressedTextColor = textColor.getCode();
    return this;
  }
  public Button setHoveredFillColor(Colors hoveredFillColor) {
    this.hoveredFillColor = hoveredFillColor.getCode();
    pressedFillColor = hoveredFillColor.getCode();
    return this;
  }
  public Button setHoveredStrokeColor(Colors hoveredStrokeColor) {
    this.hoveredStrokeColor = hoveredStrokeColor.getCode();
    pressedStrokeColor = hoveredStrokeColor.getCode();
    return this;
  }
  public Button setHoveredTextColor(Colors hoveredTextColor) {
    this.hoveredTextColor = hoveredTextColor.getCode();
    pressedTextColor = hoveredTextColor.getCode();
    return this;
  }
  public Button setHoveredStrokeWeight(int hoveredStrokeWeight) {
    this.hoveredStrokeWeight = hoveredStrokeWeight;
    pressedStrokeWeight = hoveredStrokeWeight;
    return this;
  }
  public Button setPressedFillColor(Colors pressedFillColor) {
    this.pressedFillColor = pressedFillColor.getCode();
    return this;
  }
  public Button setPressedStrokeColor(Colors pressedStrokeColor) {
    this.pressedStrokeColor = pressedStrokeColor.getCode();
    return this;
  }
  public Button setPressedTextColor(Colors pressedTextColor) {
    this.pressedTextColor = pressedTextColor.getCode();
    return this;
  }
  public Button setPressedStrokeWeight(Colors pressedStrokeWeight) {
    this.pressedStrokeWeight = pressedStrokeWeight.getCode();
    return this;
  }

  /* renders the button */
  public void render() {
    if (pressed) {
      pg.fill(pressedFillColor);
      if (pressedStrokeWeight > 0) {
        pg.stroke(pressedStrokeColor);
        pg.strokeWeight(pressedStrokeWeight);
      }
      else {
        pg.noStroke();
      }
    }
    else if (hovered) {
      pg.fill(hoveredFillColor);
      if (hoveredStrokeWeight > 0) {
        pg.stroke(hoveredStrokeColor);
        pg.strokeWeight(hoveredStrokeWeight);
      }
      else {
        pg.noStroke();
      }
    }
    else {
      pg.fill(fillColor);
      if (strokeWeight > 0) {
        pg.stroke(strokeColor);
        pg.strokeWeight(strokeWeight);
      }
      else {
        pg.noStroke();
      }
    }
    pg.rect(x, y, width, height);
    pg.textAlign(textAlignX, textAlignY);
    pg.noStroke();
    if (pressed) pg.fill(pressedTextColor);
    else if (hovered) pg.fill(hoveredTextColor);
    else pg.fill(textColor);
    pg.textFont(font);
    pg.text(text, x + textX, y + textY);
  }

  /* updates the button */
  public void update() {
    PVector mpos = KInput.mousePos;
    hovered = mpos.x >= x && mpos.x <= x + width && mpos.y >= y && mpos.y <= y + height;
    pressed = KInput.getKeyState(Key.LEFT_MOUSE) && hovered;
  }

  public boolean isHovered() {
    return hovered;
  }
  public void setHovered(boolean hovered) {
    this.hovered = hovered;
  }
  public boolean isPressed() {
    return pressed;
  }
}