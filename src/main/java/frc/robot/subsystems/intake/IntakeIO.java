package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {
    @AutoLog
    public static class IntakeIOInputs {
      public double intakeAppliedVolts = 0.0;
      public double[] intakeCurrentAmps = new double[] {};
      public boolean intakeLimitSwitch = false;
    }
  
    /** Updates the set of loggable inputs. */
    public default void updateInputs(IntakeIOInputs inputs) {}
  
    /** Run the intake wheels at the specified voltage. */
    public default void setIntakeVoltage(double volts) {}

    public boolean hasNote();
  
}
