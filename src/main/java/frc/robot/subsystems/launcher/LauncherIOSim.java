package frc.robot.subsystems.launcher;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

public class LauncherIOSim implements LauncherIO{
     private DCMotorSim launchSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);
  private DCMotorSim feedSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);

  private double launchAppliedVolts = 0.0;
  private double feedAppliedVolts = 0.0;

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    launchSim.update(0.02);
    feedSim.update(0.02);

    inputs.launchPositionRad = launchSim.getAngularPositionRad();
    inputs.launchVelocityRadPerSec = launchSim.getAngularVelocityRadPerSec();
    inputs.launchAppliedVolts = launchAppliedVolts;
    inputs.launchCurrentAmps = new double[] {launchSim.getCurrentDrawAmps()};

    inputs.feedPositionRad = feedSim.getAngularPositionRad();
    inputs.feedVelocityRadPerSec = feedSim.getAngularVelocityRadPerSec();
    inputs.feedAppliedVolts = feedAppliedVolts;
    inputs.feedCurrentAmps = new double[] {feedSim.getCurrentDrawAmps()};
  }

  @Override
  public void setLaunchVoltage(double volts) {
    launchAppliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    launchSim.setInputVoltage(launchAppliedVolts);
  }
}
