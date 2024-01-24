package frc.robot.subsystems.launcher;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.intake.Intake;

public class Launcher extends SubsystemBase {
  private static final double launchSpeed = 1.0;
  private static final double spinUpTime = 1.0;
  private static final double stopDelay = 3.0;

  private final LauncherIO io;
  private final LauncherIOInputsAutoLogged inputs = new LauncherIOInputsAutoLogged();
  private final Intake intake;

  public Launcher(LauncherIO io, Intake intake) {
    this.io = io;
    this.intake = intake;

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

  /** Returns a command that intakes a note. */
  public Command launchCommand() {
    return Commands.sequence(
      runOnce(() -> {
          io.setLeftLaunchVoltage(launchSpeed);
          io.setRightLaunchVoltage(launchSpeed);
        }),
        Commands.waitSeconds(spinUpTime),

        intake.burpCommand(),

        Commands.waitSeconds(stopDelay)
      
      ).finallyDo(() -> {
        io.setLeftLaunchVoltage(0.0);
        io.setRightLaunchVoltage(0.0);
      });
  }
}
