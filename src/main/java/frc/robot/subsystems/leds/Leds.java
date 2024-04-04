package frc.robot.subsystems.leds;

import java.util.Optional;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.intake.Intake;

import frc.robot.subsystems.leds.BrightBearsLedClass.Section;
import frc.robot.subsystems.leds.BrightBearsLedClass.LedColor;

public class Leds extends SubsystemBase {
  private final Intake intake;
  private static BrightBearsLedClass ledClass = new BrightBearsLedClass();

  // Constants regarding manual LED states
  private static final String setColorGreen = "Solid Green";
  private static final String setColorRed = "Solid Red";
  private static final String setColorOrange = "Solid Orange";
  private static final String setTwoToneSolid = "Two Color Orange and Magenta";
  private static final String rainbow = "Rainbow";
  private static final String waveBlueGreen = "Wave Blue and Green";
  private static final String setColorLightBlue = "Wave Blue and Red";
  private static final String breathBlue = "Breath Blue";
  private static final String strobeRed = "Red Strobe";
  private static final String setColorBlack = "Black Light";
  private static final String fillGreen = "Fill Green";
  private static final String zipAqua = "Zip Aqua";
  private static final String colorTest = "Color Testing";
  private String manualLedState;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  // Constants regarding automatic LED states
  public ShuffleboardTab tab = Shuffleboard.getTab("Robot");
  public GenericEntry hueValue = tab.add("Hue Value", 0)
      .getEntry();

  // States to automatically set the LEDs
  public boolean launchEnabled = false;
  public boolean climbEnabled = false;
  public boolean automaticLED = false;

  public Leds(Intake intake) {
    this.intake = intake;

    m_chooser.setDefaultOption("Solid Orange", setColorOrange);
    m_chooser.addOption("Solid Green", setColorGreen);
    m_chooser.addOption("Solid Red", setColorRed);
    m_chooser.addOption("Solid Light Blue", setColorLightBlue);
    m_chooser.addOption("Two Color Orange and Magenta", setTwoToneSolid);
    m_chooser.addOption("Solid Black", setColorBlack);
    m_chooser.addOption("Rainbow", rainbow);
    m_chooser.addOption("Wave Blue and Green", waveBlueGreen);
    m_chooser.addOption("Breath Blue", breathBlue);
    m_chooser.addOption("Red Strobe", strobeRed);
    m_chooser.addOption("Fill Green", fillGreen);
    m_chooser.addOption("Zip Aqua", zipAqua);
    m_chooser.addOption("Color testing", colorTest);
    SmartDashboard.putData("Manual LED", m_chooser);
    manualLedState = m_chooser.getSelected();
  }

  @Override
  public void periodic() {
    manualLedState = m_chooser.getSelected();
  }

  public void robotStatus() {
    if (DriverStation.isEStopped()) {
      ledClass.strobe(Section.FULL, LedColor.RED, 1);
    }

    if (DriverStation.isDSAttached()) {
      ledClass.solid(Section.LAUNCHER, LedColor.ORANGE);
    } else if (DriverStation.isFMSAttached()) {
      ledClass.fade(Section.LAUNCHER, LedColor.RED, LedColor.YELLOW, 3, 2);
    } else {
      ledClass.fill(Section.LAUNCHER, LedColor.ORANGE, 1, 1, false);
    }
  }

  /** Method to set the LED effect during autonomous period*/
  public void setAutotonomousColors() {
    ledClass.rainbow(Section.FULL, 5);
  }

  /** Method to set the LEDs to different states during the match */
  public void setStatusColors() {
    if (DriverStation.isEStopped()) {
      ledClass.strobe(Section.FULL, LedColor.RED, 1);
    } else {
      Optional<Alliance> ally = DriverStation.getAlliance();
      if (ally.isPresent()) {
        if (ally.get() == Alliance.Blue) {
          ledClass.solid(Section.UNDERGLOW, LedColor.BLUE);
        } else {
          ledClass.solid(Section.UNDERGLOW, LedColor.RED);
        }
      } else {
        ledClass.rainbow(Section.UNDERGLOW, 5);
      }

      if (automaticLED == true) {
        // Sets the LED's to green when the robot has a note in the intake

        if (intake.hasNote()) {
          ledClass.solid(Section.LAUNCHER, LedColor.GREEN);
        }        
        // Sets the LED's to run along the strip when the Launch sequence has been enabled
        if (launchEnabled) {
          ledClass.fill(Section.LEFT, LedColor.BLUE, 1, .25, false);
          ledClass.fill(Section.RIGHT, LedColor.BLUE, 1, .25, true);
        }

        else if (climbEnabled) {
          ledClass.fade(Section.LAUNCHER, LedColor.PURPLE, LedColor.PINK, 2, 1);
        }

        else {
          ledClass.solid(Section.LAUNCHER, LedColor.ORANGE);
        }

      } else {
        switch (manualLedState) {
          case setColorGreen:
            ledClass.solid(Section.LAUNCHER, LedColor.GREEN);
            break;
          case setColorRed:
            ledClass.solid(Section.LAUNCHER, LedColor.RED);
            break;
          case setColorOrange:
            ledClass.solid(Section.LAUNCHER, LedColor.ORANGE);
            break;
          case setTwoToneSolid:
            ledClass.solidTwoColor(Section.LAUNCHER, LedColor.ORANGE, LedColor.MAGENTA);
            break;
          case waveBlueGreen:
            ledClass.fade(Section.LAUNCHER, LedColor.GREEN, LedColor.BLUE, 1, 3);
            break;
          case setColorLightBlue:
            ledClass.solid(Section.LAUNCHER, LedColor.LIGHT_BLUE);
            break;
          case breathBlue:
            ledClass.breath(Section.LAUNCHER, LedColor.BLUE, 3);
            break;
          case rainbow:
            ledClass.rainbow(Section.LAUNCHER, 3);
            break;
          case strobeRed:
            ledClass.strobe(Section.LAUNCHER, LedColor.RED, 1);
            break;
          case setColorBlack:
            ledClass.solid(Section.LAUNCHER, LedColor.BLACK);
            break;
          case fillGreen:
            ledClass.fill(Section.LAUNCHER, LedColor.GREEN, 1, 2, true);
            break;
          case zipAqua:
            ledClass.zip(Section.LAUNCHER, LedColor.AQUA, 10, 1, 2, true);
            break;
          case colorTest:
            ledClass.colorTest(Section.LAUNCHER, hueValue.getDouble(0));
            break;
          default:
            ledClass.rainbow(Section.LAUNCHER, 3);
            break;
        }
      }
    }
  }
}
