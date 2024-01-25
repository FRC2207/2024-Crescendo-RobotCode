package frc.robot.subsystems.pivot;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.robot.Constants;

public class Pivot extends ProfiledPIDSubsystem {
    private final PivotIOInputsAutoLogged inputs = new PivotIOInputsAutoLogged();


    public Pivot(ProfiledPIDController controller) {
        super(
        new ProfiledPIDController(
            Constants.IntakeConstants.kP,
            0,
            0,
            new TrapezoidProfile.Constraints(
                Constants.IntakeConstants.kMaxVelocityRadPerSecond,
                Constants.IntakeConstants.kMaxAccelerationRadPerSecSquared)),
        0);
        // Start arm at rest in neutral position
        setGoal(Constants.IntakeConstants.kArmOffsetRads);
    }

    @Override
    protected void useOutput(double output, State setpoint) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useOutput'");
    }

    @Override
    protected double getMeasurement() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMeasurement'");
    }
    
}
