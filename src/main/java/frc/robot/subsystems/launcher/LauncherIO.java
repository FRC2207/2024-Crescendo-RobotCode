package frc.robot.subsystems.launcher;

import org.littletonrobotics.junction.AutoLog;

public interface LauncherIO {
     @AutoLog
    public static class IntakeIOInputs {
      public double launchPositionRad = 0.0;
      public double launchVelocityRadPerSec = 0.0;
      public double launchAppliedVolts = 0.0;
      public double[] launchCurrentAmps = new double[] {};
  
      public double feedPositionRad = 0.0;
      public double feedVelocityRadPerSec = 0.0;
      public double feedAppliedVolts = 0.0;
      public double[] feedCurrentAmps = new double[] {};
    }
  
    /** Updates the set of loggable inputs. */
    public default void updateInputs(IntakeIOInputs inputs) {}
  
    /** Run the launcher wheel at the specified voltage. */
    public default void setLaunchVoltage(double volts) {}
}
