package frc.robot.subsystems.drive;

import java.util.Arrays;
import java.util.List;

import org.littletonrobotics.junction.Logger;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.PIDConstants;
import com.pathplanner.lib.util.PathPlannerLogging;
import com.pathplanner.lib.util.ReplanningConfig;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.geometry.Twist3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.util.PoseEstimator.TimestampedVisionUpdate;

public class Drive extends SubsystemBase {
    // Constants
    private double maxLinearSpeed = Units.feetToMeters(16.6);
    private double TrackWidthX = Units.inchesToMeters(20.75);
    private double TrackWidthY = Units.inchesToMeters(20.75);
    
    public static final double coastThresholdMetersPerSec = 0.05; // Need to be under this to switch to coast while disabling
    public static final double coastThresholdSecs = 6.0; // Need to be under the above speed for this length of time to switch to coast

    private final GyroIO gyroIO;
    private final GyroIOInputsAutoLogged gyroInputs = new GyroIOInputsAutoLogged();
    private final Module[] modules = new Module[4]; // FL, FR, BL, BR

    private final SysIdRoutine sysId;

    private double maxAngularSpeed;
    private SwerveDriveKinematics kinematics = new SwerveDriveKinematics(getModuleTranslations());

    private ChassisSpeeds setpoint = new ChassisSpeeds();
    private SwerveModuleState[] lastSetpointStates = 
        new SwerveModuleState[] {
            new SwerveModuleState(),
            new SwerveModuleState(),
            new SwerveModuleState(),
            new SwerveModuleState()
        };
    private boolean isBrakeMode = false;
    private Timer lastMovementTimer = new Timer();

    //private frc.robot.util.PoseEstimator poseEstimator = new frc.robot.util.PoseEstimator(VecBuilder.fill(0.3, 0.3, 0.02*5));
    private frc.robot.util.PoseEstimator poseEstimator = new frc.robot.util.PoseEstimator(VecBuilder.fill(0.3, 0.3, 0.02)); // dif standard devs
    private double[] lastModulePositionMeters = new double[] {0.0, 0.0, 0.0, 0.0};
    private Rotation2d lastGyroYaw = new Rotation2d();
    private boolean gyroWasConnected = false;
    private Twist2d fieldVelocity = new Twist2d();

    public Drive(
        GyroIO gyroIO,
        ModuleIO flModuleIO,
        ModuleIO frModuleIO,
        ModuleIO blModuleIO,
        ModuleIO brModuleIO) {
      System.out.println("[Init] Creating Drive");
      this.gyroIO = gyroIO;
      modules[0] = new Module(flModuleIO, 0);
      modules[1] = new Module(frModuleIO, 1);
      modules[2] = new Module(blModuleIO, 2);
      modules[3] = new Module(brModuleIO, 3);
      lastMovementTimer.start();
      for (var module : modules) {
          module.setBrakeMode(false);
      }

      maxAngularSpeed =
        maxLinearSpeed / Arrays.stream(getModuleTranslations()).map(translation -> translation.getNorm()).max(Double::compare).get();

      // Configure SysId
      sysId = 
        new SysIdRoutine(
            new SysIdRoutine.Config(
                null, 
                edu.wpi.first.units.Units.Volts.of(7/1), 
                null, 
                (state) -> Logger.recordOutput("Drive/SysIdState", state.toString())),
            new SysIdRoutine.Mechanism(
                (voltage) -> {
                    for (int i = 0; i < 4; i++) {
                        modules[i].runCharacterization(voltage.in(edu.wpi.first.units.Units.Volts));
                    }
                }, 
                null, 
                this));
        
        // Configure AutoBuilder (PathPlanner)
        AutoBuilder.configureHolonomic(
            this::getPose,
            this::setPose,
            () -> kinematics.toChassisSpeeds(getModuleStates()),
            this::runVelocity,
            new HolonomicPathFollowerConfig(
                new PIDConstants(5.0, 0.0, 0.0),
                new PIDConstants(4.0, 0.0, 0.0),
                maxLinearSpeed,
                TrackWidthY / 2.0,
                new ReplanningConfig()),
            () -> 
                DriverStation.getAlliance().isPresent()
                    && DriverStation.getAlliance().get() == Alliance.Red,
            this);
        
        PathPlannerLogging.setLogActivePathCallback((activePath) -> {
            Logger.recordOutput("Odometry/Trajectory", activePath.toArray(new Pose2d[activePath.size()]));
        });
        PathPlannerLogging.setLogTargetPoseCallback((targetPose) -> {
            Logger.recordOutput("Odometry/TrajectorySetpoint", targetPose);
        });
    }

    public void periodic() {
        gyroIO.updateInputs(gyroInputs);
        Logger.processInputs("Drive/Gyro", gyroInputs);
        for (var module : modules) {
            module.periodic();
        }

        // Run modules
        if (DriverStation.isDisabled()) {
            // Stop moving while disabled
            for (var module : modules) {
                module.stop();
            }

            // Setpoint logging clear...
            Logger.recordOutput("SwerveStates/Setpoints", new SwerveModuleState[] {});
            Logger.recordOutput("SwerveStates/SetpointsOptimized", new SwerveModuleState[] {});

        } else {
            // Calculate module setpoints
            var setpointTwist =
                new Pose2d()
                        .log(
                            new Pose2d(
                                setpoint.vxMetersPerSecond * 0.02,
                                setpoint.vyMetersPerSecond * 0.02,
                                new Rotation2d(setpoint.omegaRadiansPerSecond * 0.02)));
            var adjustedSpeeds = 
                new ChassisSpeeds(
                    setpointTwist.dx / 0.02,
                    setpointTwist.dy / 0.02,
                    setpointTwist.dtheta / 0.02);
            SwerveModuleState[] setpointStates = kinematics.toSwerveModuleStates(adjustedSpeeds);
            SwerveDriveKinematics.desaturateWheelSpeeds(setpointStates, maxLinearSpeed);

            // Logging setpoint for debugging
            Logger.recordOutput("RobotDebug/DriveSetpoint", setpoint);
            Logger.recordOutput("RobotDebug/SetpointTwist", setpointTwist);
            Logger.recordOutput("RobotDebug/AdjustedSpeeds", adjustedSpeeds);
            Logger.recordOutput("RobotDebug/SetpointStates", setpointStates);
            Logger.recordOutput("RobotDebug/MaxAngularVal", maxAngularSpeed);

            // Set to last angles if zero
            if (adjustedSpeeds.vxMetersPerSecond == 0.0 && adjustedSpeeds.vyMetersPerSecond == 0.0 && adjustedSpeeds.omegaRadiansPerSecond == 0) {
                for (int i = 0; i < 4; i++) {
                    setpointStates[i] = new SwerveModuleState(0.0, lastSetpointStates[i].angle);
                }
            }
            lastSetpointStates = setpointStates;

            // Send setpoints to modules
            SwerveModuleState[] optimizedStates = new SwerveModuleState[4];
            for (int i = 0; i < 4; i++) {
                optimizedStates[i] = modules[i].runSetpoint(setpointStates[i]);
            }

            // Log setpoint states
            Logger.recordOutput("SwerveStates/Setpoints", setpointStates);
            Logger.recordOutput("SwerveStates/SetpointsOptimized", optimizedStates);
        }

        // Log measured states
        /*
        SwerveModuleState[] measuredStates = new SwerveModuleState[4];
        for (int i = 0; i < 4; i++) {
          measuredStates[i] = modules[i].getState();
        }
        */
        SwerveModuleState[] measuredStates = getModuleStates();
        Logger.recordOutput("SwerveStates/Measured", measuredStates);

        // Update odometry
        SwerveModulePosition[] wheelDeltas = new SwerveModulePosition[4];
        for (int i = 0; i < 4; i++) {
            wheelDeltas[i] =
                new SwerveModulePosition(
                    (modules[i].getPositionMeters() - lastModulePositionMeters[i]),
                    modules[i].getAngle());
            lastModulePositionMeters[i] = modules[i].getPositionMeters();
        }
        var twist = kinematics.toTwist2d(wheelDeltas);
        var gyroYaw = new Rotation2d(gyroInputs.yawPositionRad);
        if (gyroInputs.connected && gyroWasConnected) {
            twist = new Twist2d(twist.dx, twist.dy, gyroYaw.minus(lastGyroYaw).getRadians());
        }
        gyroWasConnected = gyroInputs.connected;
        lastGyroYaw = gyroYaw;
        poseEstimator.addDriveData(Timer.getFPGATimestamp(), twist);
        Logger.recordOutput("Odometry/Robot", getPose());

        // Log 3D odometry pose
        Pose3d robotPose3d = new Pose3d(getPose());
        robotPose3d =
            robotPose3d
                .exp(
                   new Twist3d(
                        0.0,
                        0.0,
                        Math.abs(gyroInputs.pitchPositionRad) * TrackWidthX / 2.0,
                        0.0,
                        gyroInputs.pitchPositionRad,
                        0.0))
                .exp(
                    new Twist3d(
                        0.0,
                        0.0,
                        Math.abs(gyroInputs.rollPositionRad) * TrackWidthY / 2.0,
                        gyroInputs.rollPositionRad,
                        0.0,
                        0.0));
        Logger.recordOutput("Odometry/Robot3d", robotPose3d);

        // Update field velocity
        ChassisSpeeds chassisSpeeds = kinematics.toChassisSpeeds(measuredStates);
        Translation2d linearFieldVelocity =
            new Translation2d(chassisSpeeds.vxMetersPerSecond, chassisSpeeds.vyMetersPerSecond)
                .rotateBy(getRotation());
        fieldVelocity =
            new Twist2d(
                linearFieldVelocity.getX(),
                linearFieldVelocity.getY(),
                gyroInputs.connected
                    ? gyroInputs.yawVelocityRadPerSec
                    : chassisSpeeds.omegaRadiansPerSecond);
        
        // Update brake mode
        boolean stillMoving = false;
        for (int i = 0; i < 4; i++) {
            if (Math.abs(modules[i].getVelocityMetersPerSec()) > coastThresholdMetersPerSec) {
                stillMoving = true;
            }
        }
        if (stillMoving) lastMovementTimer.reset();
        if (DriverStation.isEnabled()) {
            if (!isBrakeMode) {
                isBrakeMode = true;
                for (var module : modules) {
                    module.setBrakeMode(true);
                }
            }
        } else {
            if (isBrakeMode && lastMovementTimer.hasElapsed(coastThresholdSecs)) {
                isBrakeMode = false;
                for (var module : modules) {
                    module.setBrakeMode(false);
                }
            }
        }
    }

    private SwerveModuleState[] getModuleStates() {
        SwerveModuleState[] measuredStates = new SwerveModuleState[4];
        for (int i = 0; i < 4; i++) {
          measuredStates[i] = modules[i].getState();
        }
        return measuredStates;
    }

    /** 
     * Runs the drive at the desired velocity.
     * 
     * @param speeds Speeds in meters/sec
     */
    public void runVelocity(ChassisSpeeds speeds) {
        setpoint = speeds;
    }

    /** Stops the drive. */
    public void stop() {
        runVelocity(new ChassisSpeeds());
    }

    /**
     * Stops the drive and turns the modules to an X arrangement to resist movement. The modules will
     * return to their normal orientations the next time a nonzero velocity is requested.
     */
    public void stopWithX() {
        stop();
        for (int i = 0; i < 4; i++) {
            lastSetpointStates[i] =
                new SwerveModuleState(
                    lastSetpointStates[i].speedMetersPerSecond, getModuleTranslations()[i].getAngle());
        }
    }

    /**
     * Returns sysId commands for the quasistatic test in the specified direction.
     * 
     * @param direction The {@link SysIdRoutine#Direction} to run the sysId test in.
     * 
     * @return A sysId quasistatic command.
     */
    public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
        return sysId.quasistatic(direction);
    }

    /**
     * Returns sysId commands for the dynamic test in the specified direction.
     * 
     * @param direction The {@link SysIdRoutine#Direction} to run the sysId test in.
     * 
     * @return A sysId dynamic command.
     */
    public Command sysIdDynamic(SysIdRoutine.Direction direction) {
        return sysId.dynamic(direction);
    }

    /** Returns the maximum linear speed in meters per sec. */
    public double getMaxLinearSpeedMetersPerSec() {
        return maxLinearSpeed;
    }

    /** Returns the maximum angular speed in radians per sec. */
    public double getMaxAngularSpeedRadPerSec() {
        return maxAngularSpeed;
    }

    /**
     * Returns the measured X, Y, and theta field velocities in meters per sec. The components
     * of the twist are velocities and NOT changes in position.
     */
    public Twist2d getFieldVelocity() {
        return fieldVelocity;
    }

    /** Returns the current pitch (Y rotation). */
    public Rotation2d getPitch() {
        return new Rotation2d(gyroInputs.pitchPositionRad);
    }

    /** Returns the current roll (X rotation). */
    public Rotation2d getRoll() {
        return new Rotation2d(gyroInputs.rollPositionRad);
    }

    /** Returns the current yaw velocity (Z rotation) in radians per second. */
    public double getYawVelocity() {
        return gyroInputs.yawVelocityRadPerSec;
    }

    /** Returns the current pitch velocity (Y rotation) in radians per second. */
    public double getPitchVelocity() {
        return gyroInputs.pitchVelocityRadPerSec;
    }

    /** Returns the current roll velocity (X rotation) in radians per second. */
    public double getRollVelocity() {
        return gyroInputs.rollVelocityRadPerSec;
    }

    /** Returns the current odometry pose. */
    public Pose2d getPose() {
        return poseEstimator.getLatestPose();
    }

    /** Returns the current odometry rotation. */
    public Rotation2d getRotation() {
        return poseEstimator.getLatestPose().getRotation();
    }

    /** Resets the current odometry pose. */
    public void setPose(Pose2d pose) {
        poseEstimator.resetPose(pose);
    }

    /** Adds vision data to the pose estimation. */
    public void addVisionData(List<TimestampedVisionUpdate> visionData) {
        poseEstimator.addVisionData(visionData);
    }

    /** Returns an array of module translations. */
    public Translation2d[] getModuleTranslations() {
        return new Translation2d[] {
            new Translation2d(TrackWidthX / 2.0, TrackWidthY / 2.0),
            new Translation2d(TrackWidthX / 2.0, -TrackWidthY / 2.0),
            new Translation2d(-TrackWidthX / 2.0, TrackWidthY / 2.0),
            new Translation2d(-TrackWidthX / 2.0, -TrackWidthY / 2.0)
        };
    }
}
