package frc.robot.subsystems.pivot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.util.Units;
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
        return throughEncoder.getAbsolutePosition();     
    }


    @Override
    public void updateInputs(PivotIOInputs inputs) {
        //inputs.positionRad = Units.rotationsToRadians(throughEncoder.getAbsolutePosition() / Constants.IntakeConstants.pivotGearRatio);
        inputs.positionRad = Units.rotationsToRadians(-throughEncoder.get()) + 1.2693; // The encoder is mounted to the output. No gear ratio is required.
        //inputs.velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(throughEncoder.get() / Constants.IntakeConstants.pivotGearRatio);
        inputs.appliedVolts = pivotMotor.getAppliedOutput() * pivotMotor.getBusVoltage();
        inputs.currentAmps = new double[] { pivotMotor.getOutputCurrent(), pivotMotor.getOutputCurrent() };
    }
}
