package frc.robot.subsystems.launcher;

import org.littletonrobotics.junction.AutoLog;

public interface LauncherIO {
    @AutoLog
    public static class LauncherIOInputs {
      public double leftLaunchPositionRad = 0.0;
      public double leftLaunchVelocityRadPerSec = 0.0;
      public double leftLaunchAppliedVolts = 0.0;
      public double[] leftLaunchCurrentAmps = new double[] {};

      public double rightLaunchPositionRad = 0.0;
      public double rightLaunchVelocityRadPerSec = 0.0;
      public double rightLaunchAppliedVolts = 0.0;
      public double[] rightLaunchCurrentAmps = new double[] {};
    }
  
    /** Updates the set of loggable inputs. */
    public default void updateInputs(LauncherIOInputs inputs) {}
  
    /** Run the left launcher wheel at the specified voltage. */
    public default void setLeftLaunchVoltage(double volts) {}

    /** Run the right launcher wheel at the specidied voltage. */
    public default void setRightLaunchVoltage(double volts) {}
}
