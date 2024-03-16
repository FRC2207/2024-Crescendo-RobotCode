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
  private final Timer m_timer = new Timer();
  private final Intake intake;

  private static int brightnessLimit = 100;
  private int m_rainbowFirstPixelHue;
  private int m_range;
  public boolean on = true;
  private static final double fadeExponent = 0.4;

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
          solid(Section.UNDERGLOW, LedColor.BLUE);
        }
        if (ally.get() == Alliance.Red) {
          solid(Section.UNDERGLOW, LedColor.RED);
        }
      } else {
        rainbow(Section.UNDERGLOW, 4);
      }
    }));
  }

  @Override
  public void periodic() {
    m_Led.setData(ledBuffer);
    setStatusColors();
    manualLedState = m_chooser.getSelected();
  }

  /**
     * Sets the strip to a solid, singular color
     * 
     * @param section is the range you want to add the effect
     * @param color   is the color you want the LEDs to be
     */
    public void solid(Section section, LedColor color) {
      for (var i = section.start(); i < section.end(); i++) {
          final var hue = color.hues();
          final var value = color.banana();
          // Sets the specified LED to the HSV values for the preferred color
          ledBuffer.setHSV(i, hue, 255, value);
      }
  }

  /**
   * Sets the strip to a rainbow pattern that moves along the light strand
   * 
   * @param section is the range you want to add the effect
   * @param speed   is the speed you want the rainbow to run down the strip - 5 is
   *                recommended for long distance, 3 is recommended for short
   *                distances
   */
  public void rainbow(Section section, int speed) {
      // For designate range
      for (var i = section.start(); i < section.end(); i++) {
          // Calculate the hue - hue is easier for rainbows because the color
          // shape is a circle so only one value needs to precess
          final var hue = (m_rainbowFirstPixelHue + (i * 180 / section.end())) % 180;
          // Set the value
          ledBuffer.setHSV(i, hue, 255, brightnessLimit);
      }
      // Increase by to make the rainbow "move"
      m_rainbowFirstPixelHue += speed;
      // Check bounds
      m_rainbowFirstPixelHue %= 180;
  }

  /**
     * Fades between the colors designated - on the hue scale
     * 
     * @param section     is the range you want to add the effect
     * @param color1      is the base color
     * @param color2      is the color to fade in and out of
     * @param cycleLength is the frequency you fade the color by the pixels
     * @param duration    is the time it takes to cycle back to the original color
     */
    public void fade(Section section, LedColor color1, LedColor color2, int cycleLength, double duration) {
      double x = (1 - ((Timer.getFPGATimestamp() % duration) / duration)) * 2.0 * Math.PI;
      double xDiffPerLed = (2.0 * Math.PI) / cycleLength;
      final var value = color1.banana();
      for (int i = section.start(); i < section.end(); i++) {
          x += xDiffPerLed;
          if (i >= section.start()) {
              double ratio = (Math.pow(Math.sin(x), fadeExponent) + 1.0) / 2.0;
              if (Double.isNaN(ratio)) {
                  ratio = (-Math.pow(Math.sin(x + Math.PI), fadeExponent) + 1.0) / 2.0;
              }
              if (Double.isNaN(ratio)) {
                  ratio = 0.5;
              }

              int outputColor = (int) Math.round((color1.hues() * (1 - ratio)) + (color2.hues() * ratio));
              ledBuffer.setHSV(i, outputColor, 255, value);
          }
      }
  }

  /**
     * Pulses the lights to give them a breathing effect.
     * 
     * @param section  is the range you want to add the effect
     * @param color    is the color that with breath
     * @param duration is the time it takes to go through 1 cycle
     */
    public void breath(Section section, LedColor color1, double duration) {
      double x = ((Timer.getFPGATimestamp() % duration) / duration) * 2.0 * Math.PI;
      double ratio = (Math.sin(x) + 1.0) / 2.0;
      LedColor color2 = LedColor.BLACK;

      for (int i = section.start(); i < section.end(); i++) {
          final var value = (color1.banana() * (1 - ratio)) + (color2.banana() * ratio);
          ledBuffer.setHSV(i, color1.hues(), 255, (int) value);
      }
  }

  /**
   * Flashes the LEDs on and off at the designated speed
   * 
   * @param section  is the range you want to add the effect
   * @param color    is the color that will flash
   * @param duration is the time between each flash
   */
  public void strobe(Section section, LedColor color, double duration) {
      strobe(section, color, LedColor.BLACK, duration);
  }

  /**
   * Flashes the LEDs between two colors at the designated speed
   * 
   * @param section  is the range you want to add the effect
   * @param color1   is the base color
   * @param color2   is the secondary color
   * @param duration is the time between each flash
   */
  public void strobe(Section section, LedColor color1, LedColor color2, double duration) {
      if (!m_timer.advanceIfElapsed(duration / 2))
          return;

      if (!on) {
          solid(section, color1);
          on = true;
      } else {
          solid(section, color2);
          on = false;
      }
  }

    /**
     * Fills the section of LEDs with the color incrementally
     * 
     * @param section   is the range you want to add the effect
     * @param color     is the color you want to fill
     * @param increment is how many LEDs you want to fill per cycle
     * @param duration  is how long you want each cycle to last
     */
    public void fill(Section section, LedColor color, int increment, double duration) {
      LedColor color2 = LedColor.BLACK;
      for (int i = section.start(); i < section.end(); i++) {
          if (i < section.start() + m_range) {
              ledBuffer.setHSV(i, color.hues(), 255, color.banana());
          } else {
              ledBuffer.setHSV(i, color2.hues(), 255, color2.banana());
          }
      }
      //Advances at the calculated speed to
      double speed = duration / (section.end() - section.start());
      if (!m_timer.advanceIfElapsed(speed))
         return;

      //increase to fill the strip
      m_range += increment;
      // check bounds
      m_range %= section.end() - section.start();

  }

  /** Method to set the LEDs to different states during the match */
  private void setStatusColors() {
    if (automaticLED.getBoolean(false) == true) {
      // Sets the LED's to green when the robot has a note in the intake
      if (intake.hasNote() == true) {
        solid(Section.LEFT, LedColor.GREEN);
        solid(Section.RIGHT, LedColor.GREEN);
      } else {
        solid(Section.LEFT, LedColor.ORANGE);
        solid(Section.RIGHT, LedColor.ORANGE);
      }

      // Sets the LED's to orange when the robot is in autonomous mode
      if (DriverStation.isAutonomousEnabled() == true) {
        fade(Section.FULL, LedColor.ORANGE, LedColor.YELLOW, 1, 15);
      }
      // Sets the LED's to run along the strip when the Launch sequence has been enabled
      if (launchEnabled == true) {
        fade(Section.LEFT, LedColor.BLUE, LedColor.BLUE, 1, 3);
        fade(Section.RIGHT, LedColor.BLUE, LedColor.BLUE, 1, 3);
      }
      // Sets the LED's to run along the strip when the climbing sequence has been enabled
      if (climbEnabled == true) {
        fade(Section.FULL, LedColor.PURPLE, LedColor.PINK, 1, 1);
      }

    } else {
      switch (manualLedState) {
        case setColorGreen:
          solid(Section.LEFT, LedColor.GREEN);
          solid(Section.RIGHT, LedColor.GREEN);
          break;
        case setColorRed:
          solid(Section.LEFT, LedColor.RED);
          solid(Section.RIGHT, LedColor.RED);
          break;
        case setColorOrange:
          solid(Section.LEFT, LedColor.ORANGE);
          solid(Section.RIGHT, LedColor.ORANGE);
          break;
        case waveBlueGreen:
          fade(Section.LEFT, LedColor.GREEN, LedColor.BLUE, 1, 3);
          fade(Section.RIGHT, LedColor.GREEN, LedColor.BLUE, 1, 3);
          break;
        case wavePinkPurple:
          fade(Section.LEFT, LedColor.PINK, LedColor.PURPLE, 1, 3);
          fade(Section.RIGHT, LedColor.PINK, LedColor.PURPLE, 1, 3);
          break;
        case rainbow:
          rainbow(Section.LEFT, 3);
          rainbow(Section.RIGHT, 3);
          break;
        default:
          rainbow(Section.LEFT, 3);
          rainbow(Section.RIGHT, 3);
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
    PINK,
    BLACK;

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
            case BLACK:
                return 0;
            default:
                return 0;
        }
    }

    public int banana() {
        switch (this) {
            case RED:
                return (int) (brightnessLimit);
            case ORANGE:
                return (int) (brightnessLimit);
            case YELLOW:
                return (int) (brightnessLimit);
            case GREEN:
                return (int) (brightnessLimit);
            case BLUE:
                return (int) (brightnessLimit);
            case PURPLE:
                return (int) (brightnessLimit);
            case PINK:
                return (int) (brightnessLimit);
            case BLACK:
                return 0;
            default:
                return (int) (brightnessLimit % 255);
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
