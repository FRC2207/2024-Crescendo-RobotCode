package frc.robot.subsystems.leds;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;

public class LedsIO {
    public static AddressableLED underGlow = new AddressableLED(3);
    // Reuse buffer
    // Default to a length of 60, start empty output
    // Length is expensive to set, so only set it once, then just update data
    public static AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(60);
    private int m_rainbowFirstPixelHue;

    public LedsIO (){
      underGlow.setLength(ledBuffer.getLength());

    // Set the data
    underGlow.setData(ledBuffer);
    underGlow.start();
    }

    public void rainbow() {
        // For every pixel
        for (var i = 0; i < ledBuffer.getLength(); i++) {
          // Calculate the hue - hue is easier for rainbows because the color
          // shape is a circle so only one value needs to precess
          final var hue = (m_rainbowFirstPixelHue + (i * 180 / ledBuffer.getLength())) % 180;
          // Set the value
          ledBuffer.setHSV(i, hue, 255, 255);
        }
        // Increase by to make the rainbow "move"
        m_rainbowFirstPixelHue += 3;
        // Check bounds
        m_rainbowFirstPixelHue %= 180;

        underGlow.setData(ledBuffer);
      }
}
