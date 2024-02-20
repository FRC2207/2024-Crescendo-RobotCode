package frc.robot.subsystems.climber;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants.ClawConstants;

public class ClimberIOSparkMax implements ClimberIO {
    private final CANSparkMax leftMotor = new CANSparkMax(ClawConstants.leftMotorID, MotorType.kBrushless);
    private final CANSparkMax rightMotor = new CANSparkMax(ClawConstants.rightMotorID, MotorType.kBrushless);

    public ClimberIOSparkMax() {
        rightMotor.setInverted(true);
    }

    // outputs the values of the left and right claw arms
    public void updateInputs(ClimberIOInputs inputs) {
        inputs.leftClimberAppliedVolts = leftMotor.getAppliedOutput();
        inputs.rightClimberAppliedVolts = rightMotor.getAppliedOutput();
    }

    // sets the voltage for the left claw arm
    public void setLeftVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1,1);
        leftMotor.setVoltage(volts);
    }

    // sets the voltage for the right claw arm
    public void setRightVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1,1);
        rightMotor.setVoltage(volts);
    }
}
