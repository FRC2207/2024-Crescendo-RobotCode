package frc.robot.subsystems.leds;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.Constants.LedConstants;

public class LedsIO {
  public static AddressableLED m_Led = new AddressableLED(LedConstants.LedID);
  // Reuse buffer
  // Default to a length of 60, start empty output
  // Length is expensive to set, so only set it once, then just update data
  public static AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(LedConstants.totalLength);
  private int m_rainbowFirstPixelHue;

  public LedsIO() {
    m_Led.setLength(ledBuffer.getLength());

    // Set the data
    m_Led.setData(ledBuffer);
    m_Led.start();
  }

  public void rainbow() {
    // For every pixel
    for (var i = 0; i < Section.UNDERGLOW.end(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / Section.UNDERGLOW.start())) % 180;
      // Set the value
      ledBuffer.setHSV(i, hue, 255, 255);
    }
    // Increase to make the rainbow "move"
    m_rainbowFirstPixelHue += 3;
    // Check bounds
    m_rainbowFirstPixelHue %= 180;

    m_Led.setData(ledBuffer);
  }

  private static enum Section {
    LEFT,
    RIGHT,
    FULL,
    UNDERGLOW;

    public int start() {
      switch (this) {

        case LEFT:
          return LedConstants.underLength;
        case RIGHT:
          return LedConstants.underLength + LedConstants.leftLength;
        case FULL:
          return 0;
        case UNDERGLOW:
          return 0;
      }
    }

    public int end() {
      switch (this) {
        case LEFT:
          return LedConstants.underLength = LedConstants.leftLength;
        case RIGHT:
          return LedConstants.totalLength;
        case FULL:
          return LedConstants.totalLength;
        case UNDERGLOW:
          return LedConstants.underLength;

      }
    }
  }

}
