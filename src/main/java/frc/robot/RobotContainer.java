// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.commands.DriveWithController;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOADXRS450;
import frc.robot.subsystems.drive.GyroIONavX2;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOSparkMax;

import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.IntakeIOSim;
import frc.robot.subsystems.intake.IntakeIOTalonSRX;

import frc.robot.subsystems.launcher.Launcher;
import frc.robot.subsystems.launcher.LauncherIOSim;
import frc.robot.subsystems.launcher.LauncherIOSparkMax;

import frc.robot.subsystems.pivot.Pivot;
import frc.robot.subsystems.pivot.PivotIOSim;
import frc.robot.subsystems.pivot.PivotIOSparkMax;

public class RobotContainer {
  // Subsystems
  private Drive drive;
  private Intake intake;
  private Launcher launcher;
  private Pivot pivot;

  // Controller
  private final CommandXboxController driveXbox = new CommandXboxController(0);
  private final CommandXboxController manipulatorXbox = new CommandXboxController(1);

  // Autonomous Routine Chooser - This may be changed if we move over to a solution such as PathPlanner that has it's own AutoBuilder. SmartDashboard for now :)
  private final SendableChooser<Command> autoChooser = new SendableChooser<>();
  private final Command autoDefault = Commands.print("Default auto selected. No autonomous command configured.");

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
        drive =
          new Drive(
            new GyroIONavX2() {},
            new ModuleIOSparkMax(0),
            new ModuleIOSparkMax(1),
            new ModuleIOSparkMax(2),
            new ModuleIOSparkMax(3));

        intake = new Intake(new IntakeIOTalonSRX());
        launcher = new Launcher(new LauncherIOSparkMax(), intake);
        pivot = new Pivot(new PivotIOSparkMax());
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

    // Add autonomous routines to the SendableChooser
    autoChooser.setDefaultOption("Default Auto", autoDefault);

    if (Constants.isTuningMode) {
      autoChooser.addOption("Drive SysId (Dynamic Forward)", drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
      autoChooser.addOption("Drive SysId (Dynamic Reverse)", drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));
      autoChooser.addOption("Drive SysId (Quasistatic Forward)", drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
      autoChooser.addOption("Drive SysId (Quasistatic Reverse)", drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    }

    // Put SendableChooser to SmartDashboard
    SmartDashboard.putData("Auto Chooser", autoChooser);

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

    // Move pivot motor with left joystick while holding the leftBumper
    manipulatorXbox.leftBumper().whileTrue(new RunCommand(  
      () -> pivot.setPivotAngleRaw(MathUtil.applyDeadband(manipulatorXbox.getLeftY(), .15) * Constants.IntakeConstants.rawPivotSpeedLimiter)
    ));

     manipulatorXbox.leftBumper().whileTrue(new RunCommand(  
      () -> intake.setIntakeVoltageRaw(MathUtil.applyDeadband(manipulatorXbox.getRightY(), .15) * Constants.IntakeConstants.rawIntakeSpeedLimiter)
    ));
  }

  public Command getAutonomousCommand() {
    if (autoChooser.getSelected() != null) {
      return autoChooser.getSelected();
    } else {
      return Commands.print("No autonomous command configured");
    }
  }
}
