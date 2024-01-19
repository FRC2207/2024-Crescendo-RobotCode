package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLogOutput;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Intake extends SubsystemBase {
    private final IntakeIO io;
    private final FlywheelIOInputsAutoLogged inputs = new FlywheelIOInputsAutoLogged();
    private final SimpleMotorFeedforward ffModel;

    public Intake(IntakeIO io) {
        this.io = io;

        // Switch constants based on mode (the physics simulator is treated as a
        // separate robot with different tuning)
        switch (Constants.robot) {
            case "Real":
                ffModel = new SimpleMotorFeedforward(0.1, 0.05);
                io.configurePID(1.0, 0.0, 0.0);
                break;
            case "SIM":
                ffModel = new SimpleMotorFeedforward(0.0, 0.03);
                io.configurePID(0.5, 0.0, 0.0);
                break;
            default:
                ffModel = new SimpleMotorFeedforward(0.0, 0.0);
                break;
        }
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Intake", inputs);
    }

    /** Run open loop at the specified voltage. */
    public void runVolts(double volts) {
        io.setVoltage(volts);
    }

    /** Run closed loop at the specified velocity. */
    public void runVelocity(double velocityRPM) {
        var velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(velocityRPM);
        io.setVelocity(velocityRadPerSec, ffModel.calculate(velocityRadPerSec));

        // Log flywheel setpoint
        Logger.recordOutput("Intake/SetpointRPM", velocityRPM);
    }

    /** Stops the flywheel. */
    public void stop() {
        io.stop();
    }

    /** Returns the current velocity in RPM. */
    @AutoLogOutput
    public double getVelocityRPM() {
        return Units.radiansPerSecondToRotationsPerMinute(inputs.velocityRadPerSec);
    }

    /** Returns the current velocity in radians per second. */
    public double getCharacterizationVelocity() {
        return inputs.velocityRadPerSec;
    }

}
