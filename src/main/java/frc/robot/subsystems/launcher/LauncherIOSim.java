package frc.robot.subsystems.launcher;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

public class LauncherIOSim implements LauncherIO{
  private DCMotorSim leftSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);
  private DCMotorSim rightSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);

  private double leftLaunchAppliedVolts = 0.0;
  private double rightLaunchAppliedVolts = 0.0;

  @Override
  public void updateInputs(LauncherIOInputs inputs) {
    leftSim.update(0.02);
    rightSim.update(0.02);

    inputs.leftLaunchPositionRad = leftSim.getAngularPositionRad();
    inputs.leftLaunchVelocityRadPerSec = leftSim.getAngularVelocityRadPerSec();
    inputs.leftLaunchAppliedVolts = leftLaunchAppliedVolts;
    inputs.leftLaunchCurrentAmps = new double[] {leftSim.getCurrentDrawAmps()};

    inputs.rightLaunchPositionRad = rightSim.getAngularPositionRad();
    inputs.rightLaunchVelocityRadPerSec = rightSim.getAngularVelocityRadPerSec();
    inputs.rightLaunchAppliedVolts = rightLaunchAppliedVolts;
    inputs.rightLaunchCurrentAmps = new double[] {rightSim.getCurrentDrawAmps()};
   
  }

  @Override
  public void setLeftLaunchVoltage(double volts) {
    leftLaunchAppliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    leftSim.setInputVoltage(leftLaunchAppliedVolts);
  }

  @Override
  public void setRightLaunchVoltage(double volts) {
    rightLaunchAppliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    rightSim.setInputVoltage(rightLaunchAppliedVolts);
  }
}
