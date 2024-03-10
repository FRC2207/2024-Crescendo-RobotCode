package frc.robot.subsystems.launcher;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.leds.Leds;
import frc.robot.subsystems.leds.Leds.LedColor;
import frc.robot.subsystems.leds.Leds.Section;

public class Launcher extends SubsystemBase {
  private static final double launchSpeed = 1.0;
  private static final double spinUpTime = 0.5;
  private static final double stopDelay = 0.5;
  private static final double intakeSpeed = -1.0;
  private static final double intakeDelay = 1.0;

  private final LauncherIO io;
  private final LauncherIOInputsAutoLogged inputs = new LauncherIOInputsAutoLogged();
  private final Intake intake;
  private final Leds leds;

  public Launcher(LauncherIO io, Intake intake, Leds leds) {
    this.io = io;
    this.intake = intake;
    this.leds = leds;

    setDefaultCommand(
        run(
            () -> {
              io.setLeftLaunchVoltage(0.0);
            }));
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Launcher", inputs);

  }

  /** Returns a command that launches a note. */
  public Command launchCommand() {
    return Commands.sequence(
        runOnce(() -> { // Starts the launch motors to gain speed
          io.setLeftLaunchVoltage(launchSpeed);
          io.setRightLaunchVoltage(launchSpeed);
          leds.launchEnabled = true;
        }),
        Commands.waitSeconds(spinUpTime),

        intake.burpCommand(), // Intake ejects the disc into the launcher
        Commands.waitSeconds(stopDelay)

    ).finallyDo(() -> { // Stops the launch wheels and turns the LED off.
      io.setLeftLaunchVoltage(0.0);
      io.setRightLaunchVoltage(0.0);
    });
  }

  /** Returns a command that launches without intake usage */
  public Command testLaunchCommand() {
    return Commands.sequence(
        runOnce(() -> {
          io.setLeftLaunchVoltage(launchSpeed);
          io.setRightLaunchVoltage(launchSpeed);
        }),
        Commands.waitSeconds(stopDelay)

    ).finallyDo(() -> {
      io.setLeftLaunchVoltage(0.0);
      io.setRightLaunchVoltage(0.0);
    });
  }

  public Command launcherIntakeCommand() {
    return Commands.sequence(
        runOnce(() -> {
          io.setLeftLaunchVoltage(intakeSpeed);
          io.setRightLaunchVoltage(intakeSpeed);
        }),
        intake.intakeCommand(),

        Commands.waitSeconds(intakeDelay)).finallyDo(() -> {
          io.setLeftLaunchVoltage(0.0);
          io.setRightLaunchVoltage(0.0);
        });
  }
}
