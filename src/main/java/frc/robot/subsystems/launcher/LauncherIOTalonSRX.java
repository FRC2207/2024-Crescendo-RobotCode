package frc.robot.subsystems.launcher;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants;

public class LauncherIOTalonSRX implements LauncherIO{
     private final TalonSRX launchMotor = new TalonSRX(Constants.LaunchConstants.launchMotorID);
     private final TalonSRX launchMotorFollow = new TalonSRX(Constants.LaunchConstants.followMotorID);

     // launchMotorFollow.follow(launchMotor);

  public LauncherIOTalonSRX() {
    var config = new TalonSRXConfiguration();
    config.peakCurrentLimit = 80;
    config.peakCurrentDuration = 250;
    config.continuousCurrentLimit = 60;
    config.voltageCompSaturation = 12.0;
    launchMotor.configAllSettings(config);
  }

  @Override
  public void updateInputs(LauncherIOInputs inputs) {
    inputs.launchAppliedVolts = launchMotor.getMotorOutputVoltage();
    inputs.launchCurrentAmps = new double[] {launchMotor.getStatorCurrent()};
  }

  @Override
  public void setLaunchVoltage(double volts) {
    volts = MathUtil.clamp(volts, -1, 1);
    launchMotor.set(TalonSRXControlMode.PercentOutput, volts * 12.0);
  }
}
