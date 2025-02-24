package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake extends SubsystemBase {
  private static final double burpSpeedLauncher = -1.0;
  private static final double intakeSpeedLauncher = 0.75;
  private static final double intakeDelay = 0.75;
  private static final double burpDelay = 0.75;

  private double currentTriggerTime = Double.MAX_VALUE;
  private boolean currentTriggered = false;

  private final IntakeIO io;
  private final IntakeIOInputsAutoLogged inputs = new IntakeIOInputsAutoLogged();

  public Intake(IntakeIO io) {
    this.io = io;
    setDefaultCommand(
        run(() -> {
          io.setIntakeVoltage(0.0);
        }));
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Intake", inputs);
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

  /** Returns a command that runs the intake continuously until the limit switches are pressed then stops. */
  public Command continuousCommand() {
    return Commands.sequence(
      runOnce(() -> {
        io.setIntakeVoltage(intakeSpeedLauncher);
      }),
      Commands.waitUntil(() -> hasNote() == true || aboveAmpThreshold() == true).withTimeout(5),
      runOnce(() -> {
        io.setIntakeVoltage(0.0);
      })
    );
  }

  /** Returns a command that burps a note out of the intake. */
  public Command burpCommand() {
    return Commands.sequence(
        runOnce(() -> {
          io.setIntakeVoltage(burpSpeedLauncher);
        }),
        Commands.waitSeconds(burpDelay)).finallyDo(() -> {
          io.setIntakeVoltage(0.0);
        });
  }

  /** Returns a command that burps a note out into the amp. */
  public Command ampCommand() {
    return Commands.sequence(
        runOnce(() -> {
          io.setIntakeVoltage(-0.47);
        }),
        Commands.waitSeconds(1.0)).finallyDo(() -> {
          io.setIntakeVoltage(0.0);
        });
  }

  /** Method to manually operate the intake rollers  */
  public void setIntakeVoltageRaw(double percent) {
    io.setIntakeVoltage(percent * 12);
  }

  public boolean hasNote() {
    return io.hasNote();
  }

  public boolean aboveAmpThreshold() {
    if (!currentTriggered && inputs.intakeCurrentAmps[0] > 40) {
      currentTriggered = true;
      currentTriggerTime = Timer.getFPGATimestamp();
    }
    
    if (currentTriggered && inputs.intakeCurrentAmps[0] < 40) {
      currentTriggered = false;
    }

    if (currentTriggered && Timer.getFPGATimestamp() - currentTriggerTime > 0.25) {
      return true;
    }
    return false;
  }
}
