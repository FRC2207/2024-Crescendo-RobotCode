package frc.robot.commands;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.FieldConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.util.AllianceFlipUtil;

public class AutoAlign extends DriveToPose {
    public static final Pose2d speakerCenterPose =
        new Pose2d(
            FieldConstants.Speaker.centerSpeakerOpening.toTranslation2d().plus(new Translation2d(1.08, 0.0)),
            Rotation2d.fromDegrees(-180));

    public static enum Target {
        CENTER,
        SOURCESIDE,
        AMPSIDE
    }
    
    public AutoAlign(Drive drive, Target target) {
        super(
            drive,
            false,
            () -> {
                Pose2d nearestTarget;
                if (target == Target.CENTER) {
                    nearestTarget = speakerCenterPose;
                } else if (target == Target.SOURCESIDE) {
                    nearestTarget = speakerCenterPose.transformBy(new Transform2d(0.229, 1.095, new Rotation2d(Units.degreesToRadians(-40))));
                } else if (target == Target.AMPSIDE) {
                    nearestTarget = speakerCenterPose.transformBy(new Transform2d(0.229, -1.095, new Rotation2d(Units.degreesToRadians(40))));;
                } else {
                    nearestTarget = speakerCenterPose;
                }

                // Dont slam into stage
                Pose2d allianceFlippedDrive = AllianceFlipUtil.apply(drive.getPose());
                if ((drive.getPose().getX() > FieldConstants.podiumX && DriverStation.getAlliance().get() == Alliance.Blue) || (drive.getPose().getX() < AllianceFlipUtil.apply(FieldConstants.podiumX) && DriverStation.getAlliance().get() == Alliance.Red)) {
                    nearestTarget = new Pose2d(nearestTarget.getX(), MathUtil.clamp(nearestTarget.getY(), allianceFlippedDrive.getY(), 4 - 2 * (allianceFlippedDrive.getX()-FieldConstants.podiumX)), nearestTarget.getRotation());
                }

                // Dont slam into subwoofer
                //if ((target == Target.AMPSIDE || target == Target.SOURCESIDE) && (drive.getPose().getY() < FieldConstants.Speaker.topLeftSpeaker.getY() - 0.25 || drive.getPose().getY() > FieldConstants.Speaker.topRightSpeaker.getY() + 0.25)) {
                //    nearestTarget = new Pose2d(MathUtil.clamp(nearestTarget.getX(), 1.5, Double.MAX_VALUE), nearestTarget.getY(), nearestTarget.getRotation());
                //}
                
                
                nearestTarget = AllianceFlipUtil.apply(nearestTarget);

                return nearestTarget;
            });
    }
}
