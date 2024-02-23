package frc.robot.subsystems.launcher;

import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants;

public class LauncherIOSparkMax implements LauncherIO {
    private final CANSparkMax leftLaunchMotor = new CANSparkMax(Constants.LaunchConstants.leftMotorID,
            MotorType.kBrushless);
    private final CANSparkMax rightLaunchMotor = new CANSparkMax(Constants.LaunchConstants.rightMotorID,
            MotorType.kBrushless);
    private final RelativeEncoder leftEncoder = leftLaunchMotor.getEncoder();
    private final RelativeEncoder rightEncoder = rightLaunchMotor.getEncoder();

    public LauncherIOSparkMax() {
    rightLaunchMotor.setInverted(true);
  }

    @Override
    public void updateInputs(LauncherIOInputs inputs) {
        inputs.leftLaunchAppliedVolts = leftLaunchMotor.getAppliedOutput();
        inputs.rightLaunchAppliedVolts = rightLaunchMotor.getAppliedOutput();

        inputs.leftLaunchVelocityRotPerMin = getLeftLaunchSpeed();
        inputs.rightLaunchVelocityRotPerMin = getRightLaunchSpeed();

        // inputs.leftLaunchCurrentAmps = new double[] {null};
        // inputs.rightLaunchCurrentAmps = new double[]
        // {rightLaunchMotor.getStatorCurrent()};

    }

    /** Run the left launcher wheel at the specified voltage. */
    public void setLeftLaunchVoltage(double volts) {
        volts = MathUtil.clamp(volts, -12, 12);
        leftLaunchMotor.setVoltage(volts);
    }

    /** Run the right launcher wheel at the specified voltage. */
    public void setRightLaunchVoltage(double volts) {
        volts = MathUtil.clamp(volts, -12, 12);
        rightLaunchMotor.setVoltage(volts);
    }

    /** Run the right launcher wheel at a specified speed */
    public void setLeftLaunchSpeed(double speed) {
        speed = MathUtil.clamp(speed, -1, 1);
        leftLaunchMotor.set(speed);
    }

    /** Run the right launcher wheel at a specified speed */
    public void setRightLaunchSpeed(double speed) {
        speed = MathUtil.clamp(speed, -1, 1);
        rightLaunchMotor.set(speed);
    }

    /** Returns the speed of the left launch wheel in RPM */
    public double getLeftLaunchSpeed() {
        return leftEncoder.getVelocity();
    }

    /** Returns the speed of the right launch wheel in RPM */
    public double getRightLaunchSpeed() {
        return rightEncoder.getVelocity();
    }
}
