package frc.robot.subsystems.pivot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.robot.Constants;
import frc.robot.Constants.IntakeConstants;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.robot.Constants;
import edu.wpi.first.wpilibj.DutyCycleEncoder;

public class PivotIOSparkMax implements PivotIO {
    private final CANSparkMax pivotMotor = new CANSparkMax(0, MotorType.kBrushless);
    private final Encoder m_encoder = new Encoder(Constants.IntakeConstants.pivotEncoderID, 0); // Temp encoder
    public final RelativeEncoder encoder = pivotMotor.getEncoder();
    public final DutyCycleEncoder throughEncoder = new DutyCycleEncoder(Constants.IntakeConstants.pivotEncoderID);
    private final ArmFeedforward m_feedforward =
      new ArmFeedforward(
          IntakeConstants.kSVolts, IntakeConstants.kGVolts,
          IntakeConstants.kVVoltSecondPerRad, IntakeConstants.kAVoltSecondSquaredPerRad);

    public PivotIOSparkMax() {

        m_encoder.setDistancePerPulse(Constants.IntakeConstants.kEncoderDistancePerPulse);
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
        inputs.positionRad = Units.rotationsToRadians(encoder.getPosition() / Constants.IntakeConstants.pivotGearRatio);
        inputs.velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(encoder.getVelocity() / Constants.IntakeConstants.pivotGearRatio);
        inputs.appliedVolts = pivotMotor.getAppliedOutput() * pivotMotor.getBusVoltage();
        inputs.currentAmps = new double[] { pivotMotor.getOutputCurrent(), pivotMotor.getOutputCurrent() };
    }
}
