package frc.robot.subsystems.intake;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

public class IntakeIOSim implements IntakeIO {

  private DCMotorSim intakeSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);

  private double launchAppliedVolts = 0.0;

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    intakeSim.update(0.02);

    inputs.intakeAppliedVolts = launchAppliedVolts;
    inputs.intakeCurrentAmps = new double[] { intakeSim.getCurrentDrawAmps() };
    inputs.intakeLimitSwitch = false;
  }

  @Override
  public void setIntakeVoltage(double volts) {
    launchAppliedVolts = MathUtil.clamp(volts, -1, 1);
    intakeSim.setInputVoltage(launchAppliedVolts);
  }

  public boolean hasNote() {
    return false;
  }
}
