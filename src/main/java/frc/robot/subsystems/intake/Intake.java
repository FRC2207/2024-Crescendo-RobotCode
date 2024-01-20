package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Intake extends SubsystemBase {
    private static final double launchSpeedLauncher = 1.0;
  private static final double launchSpeedFeeder = 1.0;
  private static final double intakeSpeedLauncher = -1.0;
  private static final double intakeSpeedFeeder = -0.2;
  private static final double launchDelay = 1.0;

  private final IntakeIO io;
  private final IntakeIOInputsAutoLogged inputs = new IntakeIOInputsAutoLogged();

  public Intake(IntakeIO io) {
    this.io = io;
    setDefaultCommand(
        run(
            () -> {
              io.setVoltage(0.0);
            }));
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Launcher", inputs);
  }

  /** Returns a command that intakes a note. */
  public Command intakeCommand() {
    return startEnd(
        () -> {
          io.setVoltage(intakeSpeedLauncher);
        },
        () -> {
          io.setVoltage(0.0);
        });
  }

  /** Returns a command that launches a note. */
  public Command launchCommand() {
    return Commands.sequence(
            runOnce(
                () -> {
                  io.setVoltage(launchSpeedLauncher);
                }),
            Commands.waitSeconds(launchDelay),
            runOnce(
                () -> {
                  io.setFeedVoltage(launchSpeedFeeder);
                }),
            Commands.idle())
        .finallyDo(
            () -> {
              io.setVoltage(0.0);
            });
  }

}
