package frc.robot.subsystems.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotBase;
import frc.robot.Constants;

import org.littletonrobotics.junction.Logger;

public class Module {
    private int index;
    private ModuleIO io;
    private final ModuleIOInputsAutoLogged inputs = new ModuleIOInputsAutoLogged();

    private static double wheelRadius = Units.inchesToMeters(2.0);
    private double driveKp;
    private double driveKd;
    private double driveKs;
    private double driveKv;
    private double turnKp;
    private double turnKd;

    private SimpleMotorFeedforward driveFeedforward = new SimpleMotorFeedforward(0.116970, 0.133240);
    private final PIDController driveFeedback =
        new PIDController(0.9, 0.0, 0.0, 0.02);
    private final PIDController turnFeedback =
        new PIDController(23.0, 0.0, 0.0, 0.02);

    public Module(ModuleIO io, int moduleNumber) {
        System.out.println("[Init] Creating module " + Integer.toString(moduleNumber));
        this.io = io;
        this.index = moduleNumber;

        turnFeedback.enableContinuousInput(-Math.PI, Math.PI);

        // These values have not yet been tuned. They are pulled from 6328's 2023 competition code. Expect changes to be required.
        switch (Constants.robot) {
            case "SIM":
                this.driveKp = 0.9;
                this.driveKd = 0.0;
                this.driveKs = 0.116970;
                this.driveKv = 0.133240;
                this.turnKp = 23.0;
                this.turnKd = 0.0;
                break;
            case "Real":
                this.driveKp = 0.1;
                this.driveKd = 0.0;
                this.driveKs = 0.18868;
                this.driveKv = 0.12825;
                this.turnKp = 4.0;
                this.turnKd = 0.0;
        }

        driveFeedback.setPID(driveKp, 0.0, driveKd);
        turnFeedback.setPID(turnKp, 0.0, turnKd);
        driveFeedforward = new SimpleMotorFeedforward(driveKs, driveKv);
    }

    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Drive/Module" + Integer.toString(index), inputs);
    }

    public SwerveModuleState runSetpoint(SwerveModuleState state) {
        // Optimize state based on current angle
        // This avoids "taking the long road" to a module angle. (270 degrees to 0 degrees should only be a 90 degree route, not 270.)
        var optimizedState = SwerveModuleState.optimize(state, getAngle());

        // Run turn controller
        io.setTurnVoltage(
            turnFeedback.calculate(getAngle().getRadians(), optimizedState.angle.getRadians()));

        // Update velocity based on turn error
        optimizedState.speedMetersPerSecond *= Math.cos(turnFeedback.getPositionError());

        // Run drive controller
        double velocityRadPerSec = optimizedState.speedMetersPerSecond / wheelRadius;
        io.setDriveVoltage(
        driveFeedforward.calculate(velocityRadPerSec)
            + driveFeedback.calculate(inputs.driveVelocityRadPerSec, velocityRadPerSec));

        return optimizedState;
    }

    public void runCharacterization(double volts) {
        // Closed loop control of module rotation
        io.setTurnVoltage(
            turnFeedback.calculate(getAngle().getRadians(), 0.0));

        // Open loop control of module drive
        io.setDriveVoltage(volts);
    }

    /** Disables al outputs to motors. */
    public void stop() {
        io.setTurnVoltage(0.0);
        io.setDriveVoltage(0.0);
    }

    /** Sets whether brake mode is enabled. */
    public void setBrakeMode(boolean enabled) {
        io.setDriveBrakeMode(enabled);
        io.setTurnBrakeMode(enabled);
    }

    /** Returns the current turn angle of the module. */
    public Rotation2d getAngle() {
        return new Rotation2d(MathUtil.angleModulus(inputs.turnAbsolutePositionRad));
    }

    /** Returns the current drive position of the module in meters. */
    public double getPositionMeters() {
        return inputs.drivePositionRad * wheelRadius;
    }

    /** Returns the current drive velocity of the module in meters per second. */
    public double getVelocityMetersPerSec() {
        return inputs.driveVelocityRadPerSec * wheelRadius;
    }

    /** Returns the module position (turn angle and drive position). */
    public SwerveModulePosition getPosition() {
        return new SwerveModulePosition(getPositionMeters(), getAngle());
    }

    /** Returns the module state (turn angle and drive velocity). */
    public SwerveModuleState getState() {
        return new SwerveModuleState(getVelocityMetersPerSec(), getAngle());
    }

    /** Returns the drive wheel radius. */
    public static double getWheelRadius() {
        return wheelRadius;
    }
}
