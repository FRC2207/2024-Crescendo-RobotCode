package frc.robot.subsystems.climber;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkBase.IdleMode;
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

        setBreakMode(true);
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
        double outputRotations = inches / (2 * Math.PI) * (ClimberConstants.axleRadius);
                
        double inputRotations = outputRotations * ClimberConstants.gearRatio;
        leftEncoder.setPosition(inputRotations);
    }

    /** Method to bring the right arm to a designated position */
    public void setRightPosition(double inches) {
        double outputRotations = inches / (2 * Math.PI) * (ClimberConstants.axleRadius);
                
        double inputRotations = outputRotations * ClimberConstants.gearRatio;
        rightEncoder.setPosition(inputRotations);
    }

    /** Toggles break mode for both arms */
    public void setBreakMode(boolean bool) {
        if (bool == true) {
            leftMotor.setIdleMode(IdleMode.kBrake);
            rightMotor.setIdleMode(IdleMode.kBrake);
        } else {
            leftMotor.setIdleMode(IdleMode.kCoast);
            rightMotor.setIdleMode(IdleMode.kCoast);
        }
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
