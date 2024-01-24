package frc.robot.subsystems.intake;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;
import frc.robot.subsystems.intake.IntakeIO.IntakeIOInputs;

public class IntakeIOSim implements IntakeIO {

  private DCMotorSim intakeSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);

  private double launchAppliedVolts = 0.0;

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    intakeSim.update(0.02);

    inputs.launchPositionRad = intakeSim.getAngularPositionRad();
    inputs.launchVelocityRadPerSec = intakeSim.getAngularVelocityRadPerSec();
    inputs.launchAppliedVolts = launchAppliedVolts;
    inputs.launchCurrentAmps = new double[] { intakeSim.getCurrentDrawAmps() };
  }

  @Override
  public void setIntakeVoltage(double volts) {
    launchAppliedVolts = MathUtil.clamp(volts, -1, 1);
    intakeSim.setInputVoltage(launchAppliedVolts);
  }
}
