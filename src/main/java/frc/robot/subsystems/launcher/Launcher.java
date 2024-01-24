package frc.robot.subsystems.launcher;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.intake.IntakeIO;
import frc.robot.subsystems.intake.IntakeIOInputsAutoLogged;

public class Launcher extends SubsystemBase {
  private static final double launchSpeed = 1.0;
  private static final double launchDelay = 1.0;

  private final LauncherIO io;
  private final LauncherIOInputsAutoLogged inputs = new LauncherIOInputsAutoLogged();

  public Launcher(LauncherIO io) {
    this.io = io;
    setDefaultCommand(
        run(
            () -> {
              io.setLaunchVoltage(0.0);
            }));
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Launcher", inputs);

  }

  /** Returns a command that intakes a note. */
  public Command launchCommand() {
    return Commands.sequence(
      runOnce(() -> {
          io.setLaunchVoltage(launchSpeed);
        }),
        Commands.waitSeconds(launchDelay)
      ).finallyDo(() -> {
        io.setLaunchVoltage(0.0);
      });
  }
}
