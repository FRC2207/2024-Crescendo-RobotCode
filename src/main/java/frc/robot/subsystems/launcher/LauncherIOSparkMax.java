package frc.robot.subsystems.launcher;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants;

public class LauncherIOSparkMax implements LauncherIO {
    private final CANSparkMax leftLaunchMotor = new CANSparkMax(Constants.LaunchConstants.leftMotorID,
            MotorType.kBrushless);
    private final CANSparkMax rightLaunchMotor = new CANSparkMax(Constants.LaunchConstants.rightMotorID,
            MotorType.kBrushless);

    public LauncherIOSparkMax() {
    rightLaunchMotor.setInverted(true);
  }

    @Override
    public void updateInputs(LauncherIOInputs inputs) {
        inputs.leftLaunchAppliedVolts = leftLaunchMotor.getAppliedOutput();
        inputs.rightLaunchAppliedVolts = rightLaunchMotor.getAppliedOutput();

        // inputs.leftLaunchCurrentAmps = new double[] {null};
        // inputs.rightLaunchCurrentAmps = new double[]
        // {rightLaunchMotor.getStatorCurrent()};

    }

    /** Run the left launcher wheel at the specified voltage. */
    public void setLeftLaunchVoltage(double volts) {
        volts = MathUtil.clamp(volts, -12, 12);
        leftLaunchMotor.setVoltage(volts);
    }

    /** Run the right launcher wheel at the specidied voltage. */
    public void setRightLaunchVoltage(double volts) {
        volts = MathUtil.clamp(volts, -12, 12);
        rightLaunchMotor.setVoltage(volts);
    }
}
