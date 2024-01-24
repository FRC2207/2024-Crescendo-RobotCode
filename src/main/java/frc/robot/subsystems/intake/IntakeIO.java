package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {
    @AutoLog
    public static class IntakeIOInputs {
      public double launchPositionRad = 0.0;
      public double launchVelocityRadPerSec = 0.0;
      public double launchAppliedVolts = 0.0;
      public double[] launchCurrentAmps = new double[] {};

    }
  
    /** Updates the set of loggable inputs. */
    public default void updateInputs(IntakeIOInputs inputs) {}
  
    /** Run the intake wheels at the specified voltage. */
    public default void setIntakeVoltage(double volts) {}
  
}
