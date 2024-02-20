package frc.robot.subsystems.climber;

import org.littletonrobotics.junction.AutoLog;

public interface ClimberIO {
    @AutoLog
    public static class ClimberIOInputs {
        public double leftClimberAppliedVolts = 0.0;
        public double rightClimberAppliedVolts = 0.0;
        
        public double[] leftCurrentAmps = new double[] {};
        public double[] rightCurrentAmps = new double[] {};
    }

    // outputs the values of the left and right climber arms
    public default void updateInputs(ClimberIOInputs inputs) {}

    // sets the voltage for the left climber arm
    public default void setLeftVoltage(double volts) {}

    // sets the voltage for the right climber arm
    public default void setRightVoltage(double volts) {}
}
