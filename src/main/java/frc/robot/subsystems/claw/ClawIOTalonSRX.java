package frc.robot.subsystems.claw;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants.ClawConstants;

public class ClawIOTalonSRX implements ClawIO {
    private final TalonSRX leftMotor = new TalonSRX(ClawConstants.leftMotorID);
    private final TalonSRX rightMotor = new TalonSRX(ClawConstants.rightMotorID);

    public ClawIOTalonSRX() {
        rightMotor.setInverted(true);
    }

    // outputs the values of the left and right claw arms
    public void updateInputs(ClawIOInputs inputs) {
        inputs.leftClawAppliedVolts = leftMotor.getMotorOutputVoltage();
        inputs.rightClawAppliedVolts = rightMotor.getMotorOutputVoltage();

        inputs.leftCurrentAmps = new double[] { leftMotor.getStatorCurrent() };
        inputs.rightCurrentAmps = new double[] { rightMotor.getStatorCurrent() };
    }

    // sets the voltage for the left claw arm
    public void setLeftVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1, 1);
        leftMotor.set(TalonSRXControlMode.PercentOutput, volts);
    }

    // sets the voltage for the right claw arm
    public void setRightVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1, 1);
        rightMotor.set(TalonSRXControlMode.PercentOutput, volts);
    }
}
