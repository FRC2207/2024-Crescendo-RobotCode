package frc.robot.subsystems.intake;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants;

public class IntakeIOTalonSRX implements IntakeIO {
    private final TalonSRX intakeMotor = new TalonSRX(Constants.IntakeConstants.intakeMotorID);

  public IntakeIOTalonSRX() {
    var config = new TalonSRXConfiguration();
    config.peakCurrentLimit = 80;
    config.peakCurrentDuration = 250;
    config.continuousCurrentLimit = 60;
    config.voltageCompSaturation = 12.0;
    intakeMotor.configAllSettings(config);
  }

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    inputs.launchAppliedVolts = intakeMotor.getMotorOutputVoltage();
    inputs.launchCurrentAmps = new double[] {intakeMotor.getStatorCurrent()};
  }

  @Override
  public void setLaunchVoltage(double volts) {
    volts = MathUtil.clamp(volts, -1, 1);
    intakeMotor.set(TalonSRXControlMode.PercentOutput, volts * 12.0);
  }
}
