package frc.robot.subsystems.pivot;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;

public class Pivot extends ProfiledPIDSubsystem {

    public Pivot(ProfiledPIDController controller) {
        super(controller);
        //TODO Auto-generated constructor stub
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
