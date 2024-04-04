package frc.robot.subsystems.pivot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import frc.robot.Constants;
import frc.robot.Constants.IntakeConstants;
import edu.wpi.first.wpilibj.DutyCycleEncoder;

public class PivotIOSparkMax implements PivotIO {
    private final CANSparkMax pivotMotor = new CANSparkMax(IntakeConstants.pivotMotorID, MotorType.kBrushless);
    private final DutyCycleEncoder throughEncoder = new DutyCycleEncoder(Constants.IntakeConstants.pivotEncoderID);


    public PivotIOSparkMax() {
        throughEncoder.getDistancePerRotation();
    }

    @Override
    public void setPivotVoltage(double volts) {
        volts = MathUtil.clamp(volts, -12, 12);
        pivotMotor.setVoltage(volts);
    }

    @Override
    public double getMeasurement(){ 
        return (.716 + (-((throughEncoder.getAbsolutePosition() + 0.5) % 1))) * (2 * Math.PI);
    }

    @Override
    public void updateInputs(PivotIOInputs inputs) {
        inputs.encoderPosition = getMeasurement();
        //inputs.velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(throughEncoder.get() / Constants.IntakeConstants.pivotGearRatio);
        inputs.appliedVolts = pivotMotor.getAppliedOutput() * pivotMotor.getBusVoltage();
        inputs.currentAmps = new double[] { pivotMotor.getOutputCurrent(), pivotMotor.getOutputCurrent() };
    }
}
