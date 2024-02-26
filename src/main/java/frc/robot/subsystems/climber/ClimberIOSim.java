package frc.robot.subsystems.climber;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import frc.robot.Constants.ClimberConstants;

public class ClimberIOSim implements ClimberIO{
    private DCMotorSim leftSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);
    private DCMotorSim rightSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);


    // outputs the values of the left and right climber arms
    public void updateInputs(ClimberIOInputs inputs) {
        inputs.leftClimberAppliedVolts = 0.0;
        inputs.rightClimberAppliedVolts = 0.0;
        inputs.leftClimberPosition = 0.0;
        inputs.rightClimberPosition = 0.0;
        
        inputs.leftCurrentAmps = new double[] {leftSim.getCurrentDrawAmps()};
        inputs.rightCurrentAmps = new double[] {rightSim.getCurrentDrawAmps()};
    }

    /** Method to bring the left arm to a designated position */
    public void setLeftPosition(double inches) {
         double inputRotations = inches
                / (Math.PI * Math.pow(
                        ClimberConstants.axleRadius + (ClimberConstants.stringDiameter
                                * Math.floor(leftEncoder.getPosition() / ClimberConstants.maxStringRotationPerStep)),
                        2));
        double outputRotations = inputRotations * ClimberConstants.gearRatio;
        leftSim.setState(outputRotations, 0.5);
    }

    }

    /** Method to bring the right arm to a designated position */
    public void setRightPosition(double inches) {}

    /** Returns the position of the left arm */
    public double getLeftPosition() {
        return 0;
    }

    /** Returns the position of the right arm */
    public double getRightPosition() {
        return 0;
    }

    public void setBreakMode(boolean bool) {}

    /** Sets the voltage for the left climber arm */
    public void setLeftSpeed(double speed) {}

    /** Sets the voltage for the right climber arm */
    public void setRightSpeed(double speed) {}
}
