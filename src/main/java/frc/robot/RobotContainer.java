// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.commands.DriveWithController;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOADXRS450;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOSparkMax;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionIOPhotonVision;

public class RobotContainer {
  // Subsystems
  private Drive drive;
  private Vision vision;

  // Controller
  private final CommandXboxController xboxOne = new CommandXboxController(0);
  
  
  public RobotContainer() {
    switch (Constants.robot) {
      case "SIM":
        drive = 
          new Drive(
            new GyroIO() {},
            new ModuleIOSim(),
            new ModuleIOSim(),
            new ModuleIOSim(),
            new ModuleIOSim());
        break;
      case "Real":
        drive =
          new Drive(
            new GyroIOADXRS450() {},
            new ModuleIOSparkMax(0),
            new ModuleIOSparkMax(1),
            new ModuleIOSparkMax(2),
            new ModuleIOSparkMax(3));
        break;
    }
    
    // Create missing drive subsystem if needed.
    if (drive == null) {
      drive =
        new Drive(
          new GyroIO() {},
          new ModuleIOSim(),
          new ModuleIOSim(),
          new ModuleIOSim(),
          new ModuleIOSim());
    }

    // Create vision
    vision = new Vision(new VisionIOPhotonVision("0", 0));

    // Set up subsystem(s)
    vision.setDataInterfaces(drive::addVisionData, drive::getPose);

    configureBindings();
  }

  private void configureBindings() {
    // Joystick command factories
    // Function<Boolean, DriveWithController> driveWithControllerFactory =
    //     () ->
    //       new DriveWithController(
    //         drive,
    //         () -> xboxOne.getLeftX(),
    //         () -> xboxOne.getLeftY(),
    //         () -> xboxOne.getRightX(),
    //         () -> xboxOne.getRightY(),
    //         () -> xboxOne.a().getAsBoolean()
    //         );
    drive.setDefaultCommand(
      new DriveWithController(drive, () -> xboxOne.getLeftX(), () -> xboxOne.getLeftY(), () -> xboxOne.getRightX(), () -> xboxOne.getRightY(), () -> xboxOne.a().getAsBoolean())
    );
    xboxOne.x().onTrue(Commands.runOnce(drive::stopWithX, drive));
    System.out.println("Set default command");
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
