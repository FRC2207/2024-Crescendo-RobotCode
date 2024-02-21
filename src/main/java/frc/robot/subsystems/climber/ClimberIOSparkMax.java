package frc.robot.subsystems.climber;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants.ClimberConstants;

public class ClimberIOSparkMax implements ClimberIO {
    private final CANSparkMax leftMotor = new CANSparkMax(ClimberConstants.leftMotorID, MotorType.kBrushless);
    private final CANSparkMax rightMotor = new CANSparkMax(ClimberConstants.rightMotorID, MotorType.kBrushless);
    private final RelativeEncoder leftEncoder =  leftMotor.getEncoder();
    private final RelativeEncoder rightEncoder = rightMotor.getEncoder();

    public ClimberIOSparkMax() {
        rightMotor.setInverted(true);
    }

    // outputs the values of the left and right climber arms
    public void updateInputs(ClimberIOInputs inputs) {
        inputs.leftClimberAppliedVolts = leftMotor.getAppliedOutput();
        inputs.rightClimberAppliedVolts = rightMotor.getAppliedOutput();
    }

    public void setLeftPosition(double inches) {
        double rotations = inches * (Math.PI * Math.pow(ClimberConstants.axleRadius, 2));
        leftEncoder.setPosition(rotations);
    }

    // sets the voltage for the left climber arm
    public void setLeftVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1,1);
        leftMotor.setVoltage(volts);
    }

    // sets the voltage for the right climber arm
    public void setRightVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1,1);
        rightMotor.setVoltage(volts);
    }
}
