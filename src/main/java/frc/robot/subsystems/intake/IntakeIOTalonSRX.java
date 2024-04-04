package frc.robot.subsystems.intake;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.Constants;

public class IntakeIOTalonSRX implements IntakeIO {
    private final TalonSRX intakeMotor = new TalonSRX(Constants.IntakeConstants.intakeMotorID);
    private final DigitalInput intakeLimitSwitch = new DigitalInput(Constants.IntakeConstants.intakeLimitID);

  public IntakeIOTalonSRX() {
    var config = new TalonSRXConfiguration();
    config.peakCurrentLimit = 40;
    // config.continuousCurrentLimit = 50;
    config.peakCurrentDuration = 250;
    config.continuousCurrentLimit = 30;
    config.voltageCompSaturation = 12.0;
    intakeMotor.configAllSettings(config);

  }

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    inputs.intakeAppliedVolts = intakeMotor.getMotorOutputVoltage();
    inputs.intakeCurrentAmps = new double[] {intakeMotor.getStatorCurrent()};
    inputs.intakeLimitSwitch = intakeLimitSwitch.get();
  }

  @Override
  public void setIntakeVoltage(double volts) {
    volts = MathUtil.clamp(volts, -1, 1);
    intakeMotor.set(TalonSRXControlMode.PercentOutput, volts);
  }

  public boolean hasNote() {
    return !intakeLimitSwitch.get();
  }
}
