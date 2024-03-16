package frc.robot.subsystems.pivot;

import org.littletonrobotics.junction.AutoLog;


public interface PivotIO {
    @AutoLog
    public static class PivotIOInputs {
        public double encoderPosition = 0.0;
        public double positionRad = 0.0;
        public double velocityRadPerSec = 0.0;
        public double appliedVolts = 0.0;
        public double[] currentAmps = new double[] {};
    }

    public default void updateInputs(PivotIOInputs inputs) {}

    /** Run the pivot at the specified voltage. */
    public default void setPivotVoltage(double volts) {}

    /** Get the measurement of an encoder. */
    public double getMeasurement();
}
