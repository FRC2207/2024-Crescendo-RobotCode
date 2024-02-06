package frc.robot.subsystems.claw;

import org.littletonrobotics.junction.AutoLog;

public interface ClawIO {
    @AutoLog
    public static class ClawIOInputs {
        public double leftClawAppliedVolts = 0.0;
        public double rightClawAppliedVolts = 0.0;
        
        public double[] leftCurrentAmps = new double[] {};
        public double[] rightCurrentAmps = new double[] {};
    }

    // outputs the values of the left and right claw arms
    public default void updateInputs(ClawIOInputs inputs) {}

    // sets the voltage for the left claw arm
    public default void setLeftVoltage(double volts) {}

    // sets the voltage for the right claw arm
    public default void setRightVoltage(double volts) {}
}
