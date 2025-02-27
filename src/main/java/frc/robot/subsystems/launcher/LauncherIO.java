package frc.robot.subsystems.launcher;

import org.littletonrobotics.junction.AutoLog;

public interface LauncherIO {
    @AutoLog
    public static class LauncherIOInputs {
      public double leftLaunchVelocityRotPerMin = 0.0;
      public double leftLaunchAppliedVolts = 0.0;
      public double[] leftLaunchCurrentAmps = new double[] {};

      public double rightLaunchVelocityRotPerMin = 0.0;
      public double rightLaunchAppliedVolts = 0.0;
      public double[] rightLaunchCurrentAmps = new double[] {};
    }
  
    /** Updates the set of loggable inputs. */
    public default void updateInputs(LauncherIOInputs inputs) {}
  
    /** Run the left launcher wheel at the specified voltage. */
    public default void setLeftLaunchVoltage(double volts) {}

    /** Run the right launcher wheel at the specified voltage. */
    public default void setRightLaunchVoltage(double volts) {}

    /** Run the left launcher wheel at the specified speed */
    public default void setLeftLaunchSpeed(double speed) {}

    /** Run the right launcher wheel at the specified speed */
    public default void setRightLaunchSpeed(double speed) {}

    /** Returns the speed of the left launch wheel in RPM */
    public default double getLeftLaunchSpeed() {
      return 0;
    }

    /** Returns the speed of the rigth launch wheel in RPM */
    public default double getRightLaunchSpeed() {
      return 0;
    }

}
