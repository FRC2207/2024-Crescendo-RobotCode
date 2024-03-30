package frc.robot.subsystems.climber;

import org.littletonrobotics.junction.AutoLog;

public interface ClimberIO {
    @AutoLog
    public static class ClimberIOInputs {
        public double leftClimberAppliedVolts = 0.0;
        public double rightClimberAppliedVolts = 0.0;
        public double leftClimberPosition = 0.0;
        public double rightClimberPosition = 0.0;
        
        public double[] leftCurrentAmps = new double[] {};
        public double[] rightCurrentAmps = new double[] {};
    }

    // outputs the values of the left and right climber arms
    public default void updateInputs(ClimberIOInputs inputs) {}

    /** Method to bring the left arm to a designated position */
    public default void setLeftPosition(double inches) {}

    /** Method to bring the right arm to a designated position */
    public default void setRightPosition(double inches) {}

    /** Returns the position of the left arm */
    public default double getLeftPosition() {
        return 0;
    }

    /** Returns the position of the right arm */
    public default double getRightPosition() {
        return 0;
    }

    public default void setBreakMode(boolean bool) {}

    /** Sets the voltage for the left climber arm */
    public default void setLeftSpeed(double speed) {}

    /** Sets the voltage for the right climber arm */
    public default void setRightSpeed(double speed) {}
}
