package frc.robot.subsystems.pivot;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.robot.Constants;
import frc.robot.Constants.IntakeConstants;

public class Pivot extends ProfiledPIDSubsystem {
    private final PivotIOInputsAutoLogged inputs = new PivotIOInputsAutoLogged();
    private final PivotIO io;
    private final ArmFeedforward m_feedforward = new ArmFeedforward(
            IntakeConstants.kSVolts, IntakeConstants.kGVolts,
            IntakeConstants.kVVoltSecondPerRad, IntakeConstants.kAVoltSecondSquaredPerRad);

    public Pivot(PivotIO io) {
        super(
                new ProfiledPIDController(
                        Constants.IntakeConstants.kP,
                        0,
                        0,
                        new TrapezoidProfile.Constraints(
                                Constants.IntakeConstants.kMaxVelocityRadPerSecond,
                                Constants.IntakeConstants.kMaxAccelerationRadPerSecSquared)),
                0);
        this.io = io;
        // Start arm at rest in neutral position
        setGoal(Constants.IntakeConstants.kArmOffsetRads);

    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Pivot", inputs);
    }

    @Override
    public void useOutput(double output, TrapezoidProfile.State setpoint) {
        // Calculate the feedforward from the sepoint
        double feedforward = m_feedforward.calculate(setpoint.position, setpoint.velocity);
        // Add the feedforward to the PID output to get the motor output
        io.setPivotVoltage(output + feedforward);

    }

    @Override
    protected double getMeasurement() {
        // TODO Auto-generated method stub
        return io.getMeasurement();
    }

    /** Method to manually operate the pivot angle */
    public void setPivotAngleRaw(double percent) {
        //If the arm is beyond the desired range and continuing in that direction, stop.
        if (getMeasurement() >= IntakeConstants.pivotMaxAngleRad && inputs.appliedVolts < 0) { percent = 0; } 
        if (getMeasurement() <= IntakeConstants.pivotMinAngleRad && inputs.appliedVolts > 0) { percent = 0; }

        io.setPivotVoltage(percent * 12);   // Otherwise run at the designated speed
    }

    /** Returns a command to set the angle of the pivot using PID control */
    public Command setIntakeAngle(double angle) {
        return runOnce(() -> {
            setGoal(angle);
            enable();
        });
    }

}
