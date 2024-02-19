package frc.robot.subsystems.leds;

import java.util.Optional;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LedConstants;
import frc.robot.subsystems.intake.Intake;

public class Leds extends SubsystemBase {
  private static AddressableLED m_Led = new AddressableLED(LedConstants.LedID);
  private final Intake intake;
  // Reuse buffer
  // Default to a length of 60, start empty output
  // Length is expensive to set, so only set it once, then just update data
  private static AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(LedConstants.totalLength);
  private int m_rainbowFirstPixelHue;

  public Leds(Intake intake) {
    this.intake = intake;

    m_Led.setLength(ledBuffer.getLength());

    // Set the data
    m_Led.setData(ledBuffer);
    m_Led.start();
 
    /** Default command is setting the underglow of the robot */
    setDefaultCommand(run(() -> {
      Optional<Alliance> ally = DriverStation.getAlliance();
      if (ally.isPresent()) {
        if (ally.get() == Alliance.Blue) {
          setColor(Section.UNDERGLOW, LedColor.BLUE);
        }
        if (ally.get() == Alliance.Red) {
          setColor(Section.UNDERGLOW, LedColor.RED);
        }
      } else {
        rainbow(Section.FULL);
      }
    }));
  }

  /** Method to set a rainbow effect to a given section of the LED strip */
  public void rainbow(Section section) {
    // For every pixel
    for (var i = 0; i < section.end(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / section.start())) % 180;
      // Set the value
      ledBuffer.setHSV(i, hue, 255, 255);
    }
    // Increase to make the rainbow "move"
    m_rainbowFirstPixelHue += 3;
    // Check bounds
    m_rainbowFirstPixelHue %= 180;

    m_Led.setData(ledBuffer);
  }

  /** Method to set a given color to a given section of the LED strip */
  public void setColor(Section section, LedColor color) {
    for (var i = 0; i < section.end(); i++) {
      final var hue = color.hues();
      // Sets the specified LED to the HSV values for the preferred color
      ledBuffer.setHSV(i, hue, 255, 255);
    }

    m_Led.setData(ledBuffer);
  }

  public void setStatusColors() {
    if (intake.hasNote() == true) {                   // Sets the LED's to green when the robot has a note in the intake
      setColor(Section.LEFT, LedColor.GREEN);
    } else if (intake.hasNote() == false) {
      setColor(Section.LEFT, LedColor.RED);
    }

    if (DriverStation.isAutonomousEnabled() == true) {      // Sets the LED's to orange when the robot is in autonomous mode
      setColor(Section.FULL, LedColor.ORANGE);
    } else {
      m_Led.stop();
    }
    
  }

  public static enum LedColor {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE,
    PINK;

    public int hues() {
      switch (this) { // Values are divided by 2 because color pickers like Google are x/360, whereas
                      // WPILib is x/180

        case RED:
          return 0;
        case ORANGE:
          return 30 / 2;
        case YELLOW:
          return 60 / 2;
        case GREEN:
          return 110 / 2;
        case BLUE:
          return 230 / 2;
        case PURPLE:
          return 270 / 2;
        case PINK:
          return 310 / 2;
        default:
          return 0;
      }
    }
  }

  public static enum Section {
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
        default:
          return 0;
      }
    }

    public int end() {
      switch (this) {
        case LEFT:
          return LedConstants.underLength + LedConstants.leftLength;
        case RIGHT:
          return LedConstants.totalLength;
        case FULL:
          return LedConstants.totalLength;
        case UNDERGLOW:
          return LedConstants.underLength;
        default:
          return LedConstants.totalLength;
      }
    }
  }
}
