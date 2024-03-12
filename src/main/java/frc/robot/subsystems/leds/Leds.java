package frc.robot.subsystems.leds;

import java.util.Optional;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.LedConstants;
import frc.robot.subsystems.intake.Intake;

public class Leds extends SubsystemBase {
  public AddressableLED m_Led = new AddressableLED(LedConstants.LedID);
  private static AddressableLEDBuffer ledBuffer = new AddressableLEDBuffer(LedConstants.totalLength);
  private final Intake intake;

  private int m_rainbowFirstPixelHue;
  private static final double waveExponent = 0.4;

  // Constants regarding manual LED states
  private static final String setColorGreen = "Solid Green";
  private static final String setColorRed = "Solid Red";
  private static final String setColorOrange = "Solid Orange";
  private static final String rainbow = "Rainbow";
  private static final String waveBlueGreen = "Wave Blue and Green";
  private static final String wavePinkPurple = "Wave Pink and Purple";
  private String manualLedState;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  // Constants regarding automatic LED states
  public ShuffleboardTab tab = Shuffleboard.getTab("Robot");
  public GenericEntry automaticLED = tab.add("Automatic LEDs", false)
      .withWidget(BuiltInWidgets.kToggleButton)
      .getEntry();

  // States to automatically set the LEDs
  public boolean launchEnabled = false;
  public boolean climbEnabled = false;

  public Leds(Intake intake) {
    this.intake = intake;

    m_Led.setLength(ledBuffer.getLength());
    m_Led.setData(ledBuffer);
    m_Led.start();

    m_chooser.setDefaultOption("Solid Orange", setColorOrange);
    m_chooser.addOption("Solid Green", setColorGreen);
    m_chooser.addOption("Solid Red", setColorRed);
    m_chooser.addOption("Rainbow", rainbow);
    m_chooser.addOption("Wave Blue and Green", waveBlueGreen);
    m_chooser.addOption("Wave Pink and Purple", wavePinkPurple);
    SmartDashboard.putData("Manual LED", m_chooser);
    manualLedState = m_chooser.getSelected();

    Optional<Alliance> ally = DriverStation.getAlliance();
    setDefaultCommand(runOnce(() -> {
      if (ally.isPresent()) {
        if (ally.get() == Alliance.Blue) {
          setColor(Section.UNDERGLOW, LedColor.BLUE);
        }
        if (ally.get() == Alliance.Red) {
          setColor(Section.UNDERGLOW, LedColor.RED);
        }
      } else {
        rainbow(Section.UNDERGLOW);
      }
    }));
  }

  @Override
  public void periodic() {
    m_Led.setData(ledBuffer);
    setStatusColors();
    manualLedState = m_chooser.getSelected();
  }

  /** Method to set a rainbow effect to a given section of the LED strip */
  public void rainbow(Section section) {
    // For designate range
    for (var i = section.start(); i < section.end(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / section.end())) % 180;
      // Set the value
      ledBuffer.setHSV(i, hue, 255, 20);
    }
    // Increase by to make the rainbow "move"
    m_rainbowFirstPixelHue += 3;
    // Check bounds
    m_rainbowFirstPixelHue %= 180;
  }

  /** Method to set a given color to a given section of the LED strip */
  public void setColor(Section section, LedColor color) {
    for (var i = section.start(); i < section.end(); i++) {
      final var hue = color.hues();
      // Sets the specified LED to the HSV values for the preferred color
      ledBuffer.setHSV(i, hue, 255, 255);
    }
  }

  public void wave(Section section, LedColor color, LedColor color2, double cycleLength, double duration) {
    double x = (1 - ((Timer.getFPGATimestamp() % duration) / duration)) * 2.0 * Math.PI;
    double xDiffPerLed = (2.0 * Math.PI) / cycleLength;
    for (int i = section.start(); i < section.end(); i++) {
      x += xDiffPerLed;
      if (i >= section.start()) {
        double ratio = (Math.pow(Math.sin(x), waveExponent) + 1.0) / 2.0;
        if (Double.isNaN(ratio)) {
          ratio = (-Math.pow(Math.sin(x + Math.PI), waveExponent) + 1.0) / 2.0;
        }
        if (Double.isNaN(ratio)) {
          ratio = 0.5;
        }

        int outputColor = (int) Math.round((color.hues() * (1 - ratio)) + (color2.hues() * ratio));

        ledBuffer.setHSV(i, outputColor, 255, 255);
      }
    }
  }

  /** Method to set the LEDs to different states during the match */
  private void setStatusColors() {
    if (automaticLED.getBoolean(false) == true) {
      // Sets the LED's to green when the robot has a note in the intake
      if (intake.hasNote() == true) {
        setColor(Section.LEFT, LedColor.GREEN);
        setColor(Section.RIGHT, LedColor.GREEN);
      } else {
        setColor(Section.LEFT, LedColor.ORANGE);
        setColor(Section.RIGHT, LedColor.ORANGE);
      }

      // Sets the LED's to orange when the robot is in autonomous mode
      if (DriverStation.isAutonomousEnabled() == true) {
        wave(Section.FULL, LedColor.ORANGE, LedColor.YELLOW, 1, 15);
      }
      // Sets the LED's to run along the strip when the Launch sequence has been enabled
      if (launchEnabled == true) {
        wave(Section.LEFT, LedColor.BLUE, LedColor.BLUE, .1, 3);
        wave(Section.RIGHT, LedColor.BLUE, LedColor.BLUE, .1, 3);
      }
      // Sets the LED's to run along the strip when the climbing sequence has been enabled
      if (climbEnabled == true) {
        wave(Section.FULL, LedColor.PURPLE, LedColor.PINK, .1, 1);
      }

    } else {
      switch (manualLedState) {
        case setColorGreen:
          setColor(Section.LEFT, LedColor.GREEN);
          setColor(Section.RIGHT, LedColor.GREEN);
          break;
        case setColorRed:
          setColor(Section.LEFT, LedColor.RED);
          setColor(Section.RIGHT, LedColor.RED);
          break;
        case setColorOrange:
          setColor(Section.LEFT, LedColor.ORANGE);
          setColor(Section.RIGHT, LedColor.ORANGE);
          break;
        case waveBlueGreen:
          wave(Section.LEFT, LedColor.GREEN, LedColor.BLUE, 1, 3);
          wave(Section.RIGHT, LedColor.GREEN, LedColor.BLUE, 1, 3);
          break;
        case wavePinkPurple:
          wave(Section.LEFT, LedColor.PINK, LedColor.PURPLE, 1, 3);
          wave(Section.RIGHT, LedColor.PINK, LedColor.PURPLE, 1, 3);
          break;
        case rainbow:
          rainbow(Section.LEFT);
          rainbow(Section.RIGHT);
          break;
        default:
          rainbow(Section.LEFT);
          rainbow(Section.RIGHT);
          break;
      }
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
          return 20 / 2;
        case YELLOW:
          return 60 / 2;
        case GREEN:
          return 110 / 2;
        case BLUE:
          return 240 / 2;
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
          return LedConstants.underLength + LedConstants.leftLength + LedConstants.rightLength;
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
