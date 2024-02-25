package frc.robot.subsystems.climber;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants.ClimberConstants;

public class ClimberIOSparkMax implements ClimberIO {
    private final CANSparkMax leftMotor = new CANSparkMax(ClimberConstants.leftMotorID, MotorType.kBrushless);
    private final CANSparkMax rightMotor = new CANSparkMax(ClimberConstants.rightMotorID, MotorType.kBrushless);
    private final RelativeEncoder leftEncoder = leftMotor.getEncoder();
    private final RelativeEncoder rightEncoder = rightMotor.getEncoder();

    public ClimberIOSparkMax() {
        rightMotor.setInverted(true);
        rightEncoder.setInverted(true);
    }

    // outputs the values of the left and right climber arms
    public void updateInputs(ClimberIOInputs inputs) {
        inputs.leftClimberAppliedVolts = leftMotor.getAppliedOutput();
        inputs.rightClimberAppliedVolts = rightMotor.getAppliedOutput();
        inputs.leftClimberPosition = getLeftPosition();
        inputs.rightClimberPosition = getRightPosition();
    }

    /** Method to bring the left arm to a designated position */
    public void setLeftPosition(double inches) {
        double inputRotations = inches
                / (Math.PI * Math.pow(
                        ClimberConstants.axleRadius + (ClimberConstants.stringDiameter
                                * Math.floor(leftEncoder.getPosition() / ClimberConstants.maxStringRotationPerStep)),
                        2));
        double outputRotations = inputRotations * ClimberConstants.gearRatio;
        leftEncoder.setPosition(outputRotations);
    }

    /** Method to bring the right arm to a designated position */
    public void setRightPosition(double inches) {
        double inputRotations = inches
                / (Math.PI * Math.pow(
                        ClimberConstants.axleRadius + (ClimberConstants.stringDiameter
                                * Math.floor(rightEncoder.getPosition() / ClimberConstants.maxStringRotationPerStep)),
                        2));
        double outputRotations = inputRotations * ClimberConstants.gearRatio;
        rightEncoder.setPosition(outputRotations);
    }

    /** Returns the position of the left arm */
    public double getLeftPosition() {
       return leftEncoder.getPosition();
    }

    /** Returns the position of the right arm */
    public double getRightPosition() {
        return rightEncoder.getPosition();
    }

    /** Sets the voltage for the left climber arm */
    public void setLeftSpeed(double speed) {
        speed = MathUtil.clamp(speed, -1, 1);
        leftMotor.set(speed);
    }

    /** Sets the voltage for the right climber arm */
    public void setRightSpeed(double speed) {
        speed = MathUtil.clamp(speed, -1, 1);
        rightMotor.set(speed);
    }
}
