package frc.robot.subsystems.launcher;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants;

public class LauncherIOTalonSRX implements LauncherIO{
     private final TalonSRX leftLaunchMotor = new TalonSRX(Constants.LaunchConstants.launchMotorID);
     private final TalonSRX rightLaunchMotor = new TalonSRX(Constants.LaunchConstants.followMotorID);


  public LauncherIOTalonSRX() {
    var config = new TalonSRXConfiguration();
    config.peakCurrentLimit = 80;
    config.peakCurrentDuration = 250;
    config.continuousCurrentLimit = 60;
    config.voltageCompSaturation = 12.0;
    leftLaunchMotor.configAllSettings(config);
    rightLaunchMotor.configAllSettings(config);
  }

  @Override
  public void updateInputs(LauncherIOInputs inputs) {
    inputs.leftLaunchAppliedVolts = leftLaunchMotor.getMotorOutputVoltage();
    inputs.rightLaunchAppliedVolts = rightLaunchMotor.getMotorOutputVoltage();

    inputs.leftLaunchCurrentAmps = new double[] {leftLaunchMotor.getStatorCurrent()};
    inputs.rightLaunchCurrentAmps = new double[] {rightLaunchMotor.getStatorCurrent()};

  }

  @Override
  public void setLeftLaunchVoltage(double volts) {
    volts = MathUtil.clamp(volts, -1, 1);
    leftLaunchMotor.set(TalonSRXControlMode.PercentOutput, volts * 12.0);
  }

  @Override
  public void setRightLaunchVoltage(double volts) {
    volts = MathUtil.clamp(volts, -1, 1);
    rightLaunchMotor.set(TalonSRXControlMode.PercentOutput, volts * 12.0);
  }
}
