package frc.robot.subsystems.claw;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class ClawIOSim implements ClawIO {
  private FlywheelSim leftSim = new FlywheelSim(DCMotor.getNEO(1), 16, .00001);
  private FlywheelSim rightSim = new FlywheelSim(DCMotor.getNEO(1), 16, .00001);

  private double leftAppliedVolts = 0.0;
  private double rightAppliedVolts = 0.0;

  // outputs the values of the left and right claw arms
  public void updateInputs(ClawIOInputs inputs) {
    inputs.leftClawAppliedVolts = leftAppliedVolts;
    inputs.rightClawAppliedVolts = rightAppliedVolts;

    inputs.leftCurrentAmps = new double[] {leftSim.getCurrentDrawAmps()};
    inputs.rightCurrentAmps = new double[] {rightSim.getCurrentDrawAmps()};
  }

  // sets the voltage for the left claw arm
  public void setLeftVoltage(double volts) {
    leftAppliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    leftSim.setInputVoltage(leftAppliedVolts);
  }

  // sets the voltage for the right claw arm
  public void setRightVoltage(double volts) {
    rightAppliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
    rightSim.setInputVoltage(rightAppliedVolts);
  }
}
