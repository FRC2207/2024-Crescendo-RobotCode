package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake extends SubsystemBase {
  private static final double burpSpeedLauncher = 1.0;
  private static final double intakeSpeedLauncher = -1.0;
  private static final double intakeDelay = 1.0;
  private static final double burpDelay = 1.0;

  private final IntakeIO io;
  private final IntakeIOInputsAutoLogged inputs = new IntakeIOInputsAutoLogged();

  public Intake(IntakeIO io) {
    this.io = io;
    setDefaultCommand(
      run(() -> {
        io.setIntakeVoltage(0.0);
      }
    ));
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Launcher", inputs);
  }

  /** Returns a command that intakes a note. */
  public Command intakeCommand() {
    return Commands.sequence(
        runOnce(() -> {
          io.setIntakeVoltage(intakeSpeedLauncher);
        }),
        Commands.waitSeconds(intakeDelay)).finallyDo(() -> {
          io.setIntakeVoltage(0.0);
        });
  }

  /** Returns a command that burps a note into the launcher. */
  public Command burpCommand() {
    return Commands.sequence(
        runOnce(() -> {
          io.setIntakeVoltage(burpSpeedLauncher);
        }),
        Commands.waitSeconds(burpDelay)).finallyDo(() -> {
          io.setIntakeVoltage(0.0);
        });
  }
}
