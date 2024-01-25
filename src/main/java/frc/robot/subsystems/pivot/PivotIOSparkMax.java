package frc.robot.subsystems.pivot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;
import frc.robot.Constants;

public class PivotIOSparkMax implements PivotIO {
    private final CANSparkMax pivotMotor = new CANSparkMax(0, MotorType.kBrushless);
    private final Encoder m_encoder = new Encoder(Constants.IntakeConstants.pivotEncoderID, 0); // Temp encoder
    private final RelativeEncoder encoder = pivotMotor.getEncoder();

    public PivotIOSparkMax() {

        m_encoder.setDistancePerPulse(Constants.IntakeConstants.kEncoderDistancePerPulse);

    }

    @Override
    public void setPivotVoltage(double volts) {
        volts = MathUtil.clamp(volts, -1, 1);
        pivotMotor.setVoltage(volts * 12);
    }

    @Override
    public void updateInputs(PivotIOInputs inputs) {
        inputs.positionRad = Units.rotationsToRadians(encoder.getPosition() / Constants.IntakeConstants.pivotGearRatio);
        inputs.velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(encoder.getVelocity() / Constants.IntakeConstants.pivotGearRatio);
        inputs.appliedVolts = pivotMotor.getAppliedOutput() * pivotMotor.getBusVoltage();
        inputs.currentAmps = new double[] { pivotMotor.getOutputCurrent(), pivotMotor.getOutputCurrent() };
    }
}
