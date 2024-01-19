package frc.robot.commands;

import java.util.function.Supplier;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.GeomUtil;

public class DriveWithController extends Command {
    // Additional constants up here...

    private final Drive drive;
    private final Supplier<Double> leftXSupplier;
    private final Supplier<Double> leftYSupplier;
    private final Supplier<Double> rightXSupplier;
    private final Supplier<Double> rightYSupplier;
    private final Supplier<Boolean> robotRelativeOverride;
    private ChassisSpeeds lastSpeeds = new ChassisSpeeds();

    public DriveWithController(
        Drive drive,
        Supplier<Double> leftXSupplier,
        Supplier<Double> leftYSupplier,
        Supplier<Double> rightXSupplier,
        Supplier<Double> rightYSupplier,
        Supplier<Boolean> robotRelativeOverride) {
      addRequirements(drive);
      this.drive = drive;
      this.leftXSupplier = leftXSupplier;
      this.leftYSupplier = leftYSupplier;
      this.rightXSupplier = rightXSupplier;
      this.rightYSupplier = rightYSupplier;
      this.robotRelativeOverride = robotRelativeOverride;
    }

    @Override
    public void initialize() {
        lastSpeeds = new ChassisSpeeds();
    }

    @Override
    public void execute() {
        // Go to X right before the end of the match
        if (DriverStation.getMatchTime() >= 0.0 && DriverStation.getMatchTime() < 0.25) {
            drive.stopWithX();
            return;
        }

        // Get values from double suppliers
        double leftX = -leftXSupplier.get();
        double leftY = -leftYSupplier.get();
        double rightX = -rightXSupplier.get();
        double rightY = -rightYSupplier.get();

        // Get direction and magnitude of linear axes
        double linearMagnitude = Math.hypot(leftY, leftX);
        Rotation2d linearDirection = new Rotation2d(leftY, leftX);

        // Apply deadband
        linearMagnitude = MathUtil.applyDeadband(linearMagnitude, 0.05);
        rightX = MathUtil.applyDeadband(rightX, 0.05);
        rightY = MathUtil.applyDeadband(rightY, 0.05);

        // Apply squaring
        linearMagnitude = Math.copySign(linearMagnitude * linearMagnitude, linearMagnitude);
        rightX = Math.copySign(rightX * rightX, rightX);
        rightY = Math.copySign(rightY * rightY, rightY);
 
        // Apply speed limits
        linearMagnitude *= 1.0/4;
        rightX *= 0.75/4;

        // Calculate new linear components
        Translation2d linearVelocity =
            new Pose2d(new Translation2d(), linearDirection)
                .transformBy(GeomUtil.translationToTransform(linearMagnitude, 0.0))
                .getTranslation();
        
        // Convert to meters per second
        ChassisSpeeds speeds =
            new ChassisSpeeds(
                linearVelocity.getX() * drive.getMaxLinearSpeedMetersPerSec(),
                linearVelocity.getY() * drive.getMaxLinearSpeedMetersPerSec(),
                rightX * drive.getMaxAngularSpeedRadPerSec());
        
        // Convert from field relative
        if (!robotRelativeOverride.get()) {
            var driveRotation = drive.getRotation();
            // This was giving a weird error as written. Trying this and we'll see what happens
            if (DriverStation.getAlliance().equals(Alliance.Red)) {
                driveRotation = driveRotation.plus(new Rotation2d(Math.PI));
            }
            speeds = 
                ChassisSpeeds.fromRobotRelativeSpeeds(
                    speeds.vxMetersPerSecond,
                    speeds.vyMetersPerSecond, 
                    speeds.omegaRadiansPerSecond,
                    driveRotation.times(-1.0)); // Invert this for sim
        }

        // Send X command to drive logic. Not included due to extra implementation required
        //var driveTranslation = AllianceFlipUtil.apply(drive.getPose().getTranslation());
        
        // Send to drive
        drive.runVelocity(speeds);
    }

    @Override
    public void end(boolean interrupted) {
        drive.stop();
    }
}
