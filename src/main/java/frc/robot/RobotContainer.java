// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

import frc.robot.commands.DriveWithController;
import frc.robot.subsystems.climber.Climber;
import frc.robot.subsystems.climber.ClimberIOSparkMax;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOADXRS450;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOSparkMax;

import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeIOSim;
import frc.robot.subsystems.intake.IntakeIOTalonSRX;

import frc.robot.subsystems.launcher.Launcher;
import frc.robot.subsystems.launcher.LauncherIOSim;
import frc.robot.subsystems.launcher.LauncherIOTalonSRX;

import frc.robot.subsystems.pivot.Pivot;
import frc.robot.subsystems.pivot.PivotIOSim;
import frc.robot.subsystems.pivot.PivotIOSparkMax;

public class RobotContainer {
  // Subsystems
  private Drive drive;
  private Intake intake;
  private Launcher launcher;
  private Pivot pivot;
  private Climber climber;

  // Controller
  private final CommandXboxController driveXbox = new CommandXboxController(0);
  private final CommandXboxController manipulatorXbox = new CommandXboxController(1);

  public RobotContainer() {
    switch (Constants.robot) {
      case "SIM":
        drive = new Drive(
            new GyroIO() {
            },
            new ModuleIOSim(),
            new ModuleIOSim(),
            new ModuleIOSim(),
            new ModuleIOSim());

        intake = new Intake(new IntakeIOSim());
        launcher = new Launcher(new LauncherIOSim(), intake);
        pivot = new Pivot(new PivotIOSim());
        break;
      case "Real":
        drive = new Drive(
            new GyroIOADXRS450() {
            },
            new ModuleIOSparkMax(0),
            new ModuleIOSparkMax(1),
            new ModuleIOSparkMax(2),
            new ModuleIOSparkMax(3));

        intake = new Intake(new IntakeIOTalonSRX());
        launcher = new Launcher(new LauncherIOTalonSRX(), intake);
        pivot = new Pivot(new PivotIOSparkMax());
        climber = new Climber(new ClimberIOSparkMax());
        break;
    }

    // Create missing drive subsystem if needed.
    if (drive == null) {
      drive = new Drive(
          new GyroIO() {
          },
          new ModuleIOSim(),
          new ModuleIOSim(),
          new ModuleIOSim(),
          new ModuleIOSim());
    }

    configureBindings();
  }

  private void configureBindings() {
    // Joystick command factories
    // Function<Boolean, DriveWithController> driveWithControllerFactory =
    // () ->
    // new DriveWithController(
    // drive,
    // () -> xboxOne.getLeftX(),
    // () -> xboxOne.getLeftY(),
    // () -> xboxOne.getRightX(),
    // () -> xboxOne.getRightY(),
    // () -> xboxOne.a().getAsBoolean()
    // );
    drive.setDefaultCommand(
        new DriveWithController(drive, () -> driveXbox.getLeftX(), () -> driveXbox.getLeftY(),
            () -> driveXbox.getRightX(), () -> driveXbox.getRightY(), () -> driveXbox.a().getAsBoolean()));
    driveXbox.x().onTrue(Commands.runOnce(drive::stopWithX, drive));
    System.out.println("Set default command");

    // Single controller commands
    driveXbox.povUp().whileTrue(Commands.run(() -> pivot.setPivotAngleRaw(-0.1825), pivot)).onFalse(Commands.run(() -> pivot.setPivotAngleRaw(0.0), pivot)).debounce(0.1);
    driveXbox.povDown().whileTrue(Commands.run(() -> pivot.setPivotAngleRaw(0.1825), pivot)).onFalse(Commands.run(() -> pivot.setPivotAngleRaw(0.0), pivot)).debounce(0.1);
    driveXbox.a().onTrue(intake.continuousCommand());
    driveXbox.b().onTrue(intake.burpCommand());
    driveXbox.y().onTrue(launcher.launchCommand());
    driveXbox.rightBumper().whileTrue(Commands.run(() -> intake.setIntakeVoltageRaw(1), intake)).onFalse(Commands.run(() -> intake.setIntakeVoltageRaw(0), intake));

    manipulatorXbox.a().onTrue(intake.continuousCommand());    
    manipulatorXbox.b().onTrue(launcher.launcherIntakeCommand());
    
    manipulatorXbox.rightBumper().onTrue(launcher.launchCommand());
    manipulatorXbox.x().onTrue(launcher.testLaunchCommand());

    manipulatorXbox.povUp().onTrue(climber.upBothCommand());
    manipulatorXbox.povDown().onTrue(climber.downBothCommand());
    manipulatorXbox.leftBumper().and(manipulatorXbox.povUp()).whileTrue(climber.upLeftCommand());
    manipulatorXbox.leftBumper().and(manipulatorXbox.povDown()).whileTrue(climber.downLeftCommand());
    manipulatorXbox.rightBumper().and(manipulatorXbox.povUp()).whileTrue(climber.upRightCommand());
    manipulatorXbox.rightBumper().and(manipulatorXbox.povDown()).whileTrue(climber.downRightCommand());




    //manipulatorXbox.x().onTrue(pivot.setIntakeAngle(0));
    //manipulatorXbox.y().onTrue(pivot.setIntakeAngle(90));

    // Move pivot motor with left joystick while holding the leftBumper
    manipulatorXbox.leftBumper().whileTrue(new RunCommand(  
      () -> pivot.setPivotAngleRaw(MathUtil.applyDeadband(manipulatorXbox.getLeftY(), .15) * Constants.IntakeConstants.rawPivotSpeedLimiter)
    ));

     manipulatorXbox.leftBumper().whileTrue(new RunCommand(  
      () -> intake.setIntakeVoltageRaw(MathUtil.applyDeadband(manipulatorXbox.getRightY(), .15) * Constants.IntakeConstants.rawIntakeSpeedLimiter)
    ));
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
