package frc.robot.subsystems.pivot;

import com.revrobotics.CANSparkMax;

import edu.wpi.first.math.util.Units;
import frc.robot.Constants;

public class PivotIOSim implements PivotIO {
    private final CANSparkMax pivotMotor = new CANSparkMax(Constants.IntakeConstants.pivotMotorID, kBrushless):
    @Override
    public void updateInputs(PivotIOInputs inputs) {
        inputs.positionRad = Units.rotationsToRadians(encoder.getPosition() / Constants.IntakeConstants.pivotGearRatio);
        inputs.velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(encoder.getVelocity() / Constants.IntakeConstants.pivotGearRatio);
        inputs.appliedVolts = leader.getAppliedOutput() * leader.getBusVoltage();
        inputs.currentAmps = new double[] { leader.getOutputCurrent(), follower.getOutputCurrent() };
    }
}
