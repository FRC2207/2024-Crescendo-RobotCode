// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.function.BooleanSupplier;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.IntakeConstants;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.commands.AutoAlign;
import frc.robot.commands.DriveToPose;
import frc.robot.commands.DriveWithController;
import frc.robot.commands.IntakeGround;
import frc.robot.commands.IntakeGroundAuto;
import frc.robot.commands.AutoAlign.Target;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
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
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionIOPhotonVision;

public class RobotContainer {
  // Subsystems
  private Drive drive;
  private Vision vision;
  private Intake intake;
  private Launcher launcher;
  private Pivot pivot;

  // Controller
  private final CommandXboxController driveXbox = new CommandXboxController(0);
  private final CommandXboxController manipulatorXbox = new CommandXboxController(1);

  // Autonomous Routine Chooser - This may be changed if we move over to a solution such as PathPlanner that has it's own AutoBuilder. SmartDashboard for now :)
  private final LoggedDashboardChooser<Command> autoChooser;
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

    // Create vision
    vision = new Vision(
        new VisionIOPhotonVision("0", 0),
        new VisionIOPhotonVision("1", 1),
        new VisionIOPhotonVision("2", 2));

    // Set up subsystem(s)
    vision.setDataInterfaces(drive::addVisionData, drive::getPose);

    // Create named commands
    NamedCommands.registerCommand("shootButton", launcher.launchCommand().withTimeout(10.0));
    NamedCommands.registerCommand("shooterSpinUp", launcher.launcherSpinUp(0.75).withTimeout(0.5));

    Command intakeDown = pivot.stupidPIDCommand(Units.degreesToRadians(5.75));
    intakeDown.addRequirements(pivot);
    Command intakeUp = pivot.stupidPIDCommand(Units.degreesToRadians(175));
    intakeUp.addRequirements(pivot);

    NamedCommands.registerCommand("intakeDown", intakeDown.until(() -> pivot.getPivotAngleAdjusted() <= Units.degreesToRadians(6.5)));
    NamedCommands.registerCommand("intakeUp", intakeUp.until(() -> pivot.getPivotAngleAdjusted() >= Units.degreesToRadians(175)));
    NamedCommands.registerCommand("runIntake", Commands.run(() -> intake.setIntakeVoltageRaw(0.5), intake).until(() -> intake.hasNote() || intake.aboveAmpThreshold()).withTimeout(3).finallyDo(() -> intake.setIntakeVoltageRaw(0)));
    NamedCommands.registerCommand("intakeBump", Commands.run(() -> intake.setIntakeVoltageRaw(0.5), intake).withTimeout(0.02).finallyDo(() -> intake.setIntakeVoltageRaw(0)));

    // Create SendableChooser using PathPlanner AutoBuilder
    autoChooser = new LoggedDashboardChooser<>("Auto Chooser", AutoBuilder.buildAutoChooser());
    
    // Add autonomous routines to the SendableChooser
    autoChooser.addDefaultOption("Default Auto", autoDefault);

    if (Constants.isTuningMode) {
      autoChooser.addOption("Drive SysId (Dynamic Forward)", drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
      autoChooser.addOption("Drive SysId (Dynamic Reverse)", drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));
      autoChooser.addOption("Drive SysId (Quasistatic Forward)", drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
      autoChooser.addOption("Drive SysId (Quasistatic Reverse)", drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    }

    configureBindings();
  }
  private boolean atAngle(){
    return pivot.getPivotAngleAdjusted() >= Units.degreesToRadians(175);
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
    driveXbox.leftBumper().onTrue(launcher.launchCommand());
    driveXbox.rightBumper().whileTrue(Commands.run(() -> intake.setIntakeVoltageRaw(0.5), intake)).onFalse(Commands.run(() -> intake.setIntakeVoltageRaw(0), intake));

    //driveXbox.leftBumper().onTrue(Commands.runOnce(() -> drive.setPose(AutoAlign.speakerCenterPose)));
    // This si our reset command driveXbox.leftBumper().onTrue(Commands.runOnce(() -> drive.setPose(new Pose2d(new Translation2d(), new Rotation2d(Units.degreesToRadians(180))))));
    //driveXbox.start().whileTrue(new DriveToPose(drive, true, new Pose2d(new Translation2d(3, 2), new Rotation2d(Math.PI/4))));
    driveXbox.start().whileTrue(new AutoAlign(drive, Target.CENTER));
    driveXbox.back().whileTrue(new AutoAlign(drive, Target.SOURCESIDE));
    //driveXbox.leftBumper().onTrue(pivot.setIntakeAngle(Units.degreesToRadians(165)));
    //driveXbox.rightBumper().onTrue(pivot.setIntakeAngle(Units.degreesToRadians(10)));
    //driveXbox.rightTrigger().onTrue(Commands.runOnce(() -> pivot.setShouldRunStupid(true))).onFalse(Commands.runOnce(() -> pivot.setShouldRunStupid(false)).andThen(Commands.runOnce(() -> pivot.setPivotAngleRaw(0))));
    
    IntakeGroundAuto intakeGroundAuto = new IntakeGroundAuto(intake, pivot);
    //driveXbox.leftTrigger().onTrue(intakeGroundAuto).onFalse(Commands.runOnce(() -> {intakeGroundAuto.cancel(); pivot.stupidPIDCommand(Units.DegreesToRadians(165));}));
    Command pivotDown = pivot.stupidPIDCommand(Units.degreesToRadians(5));
    pivotDown.addRequirements(pivot);
    Command pivotUp = pivot.stupidPIDCommand(Units.degreesToRadians(175));
    pivotUp.addRequirements(pivot);
    //driveXbox.rightTrigger().onTrue(intakeDown).onTrue(intake.continuousCommand()).onFalse(Commands.runOnce(() -> intake.setIntakeVoltageRaw(0.0), intake)).onFalse(intakeUp.until(() -> pivot.getPivotAngleAdjusted() >= Units.degreesToRadians(175)));
    driveXbox.rightTrigger()
        .onTrue(
          Commands.parallel(pivotDown, intake.continuousCommand()))
        .onFalse(
          Commands.parallel( 
            Commands.runOnce(() -> intake.setIntakeVoltageRaw(0.0), intake),
            Commands.race(
              pivot.stupidPIDCommand(Units.degreesToRadians(175)),
              Commands.waitUntil(() -> pivot.getPivotAngleAdjusted() >= Units.degreesToRadians(175))
            )
            //intakeUp.until(() -> pivot.getPivotAngleAdjusted() >= Units.degreesToRadians(175))
    ));

    Command intakeAmp = pivot.stupidPIDCommand(2.325 + Units.degreesToRadians(-1));
    intakeAmp.addRequirements(pivot);
    // Command scoreAmp = Commands.sequence(
    //   intakeAmp.until(() -> pivot.getPivotAngleAdjusted() <= 2.4),
    //   intake.ampCommand(),
    //   intakeUp);

    Command scoreAmp = Commands.sequence(
      intakeAmp.withTimeout(1.5),
      intake.ampCommand(),
      pivotUp.withTimeout(0.25));
    
    driveXbox.y().onTrue(scoreAmp);

    manipulatorXbox.rightTrigger()
        .onTrue(
          Commands.parallel(pivotDown, intake.continuousCommand()))
        .onFalse(
          Commands.parallel( 
            Commands.runOnce(() -> intake.setIntakeVoltageRaw(0.0), intake),
            Commands.race(
              pivot.stupidPIDCommand(Units.degreesToRadians(175)),
              Commands.waitUntil(() -> pivot.getPivotAngleAdjusted() >= Units.degreesToRadians(175))
            )));

    manipulatorXbox.a().onTrue(intake.burpCommand());
    manipulatorXbox.y().onTrue(scoreAmp);
    manipulatorXbox.rightBumper().onTrue(launcher.launchCommand());

    manipulatorXbox.povUp().whileTrue(Commands.run(() -> pivot.setPivotAngleRaw(-0.25), pivot))
      .onFalse(Commands.run(() -> pivot.setPivotAngleRaw(0.0), pivot)).debounce(0.1);

    manipulatorXbox.povDown().whileTrue(Commands.run(() -> pivot.setPivotAngleRaw(0.325), pivot))
      .onFalse(Commands.run(() -> pivot.setPivotAngleRaw(0.0), pivot)).debounce(0.1);
  }

  public Command getAutonomousCommand() {
    if (autoChooser.get() != null) {
      return autoChooser.get();
    } else {
      return Commands.print("No autonomous command configured");
    }
  }
}
