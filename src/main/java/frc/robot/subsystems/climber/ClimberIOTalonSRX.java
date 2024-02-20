package frc.robot.subsystems.climber;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants.ClimberConstants;

public class ClimberIOTalonSRX implements ClimberIO {
    private final TalonSRX leftMotor = new TalonSRX(ClimberConstants.leftMotorID);
    private final TalonSRX rightMotor = new TalonSRX(ClimberConstants.rightMotorID);

    public ClimberIOTalonSRX() {
        rightMotor.setInverted(true);
    }

    // outputs the values of the left and right climber arms
    public void updateInputs(ClimberIOInputs inputs) {
        inputs.leftClimberAppliedVolts = leftMotor.getMotorOutputVoltage();
        inputs.rightClimberAppliedVolts = rightMotor.getMotorOutputVoltage();

        inputs.leftCurrentAmps = new double[] { leftMotor.getStatorCurrent() };
        inputs.rightCurrentAmps = new double[] { rightMotor.getStatorCurrent() };
    }

    // sets the voltage for the left climber arm
    public void setLeftVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1, 1);
        leftMotor.set(TalonSRXControlMode.PercentOutput, volts);
    }

    // sets the voltage for the right climber arm
    public void setRightVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1, 1);
        rightMotor.set(TalonSRXControlMode.PercentOutput, volts);
    }
}
