package frc.robot.subsystems.intake;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.math.util.Units;
import frc.robot.Constants;
import frc.robot.subsystems.intake.IntakeIO.IntakeIOInputs;

public class IntakeIOTalonFX implements IntakeIO {

    //private static final double GEAR_RATIO = 1.5;

    //private final TalonFX leader = new TalonFX(0);
   // private final TalonFX follower = new TalonFX(1);
    private final WPI_TalonSRX intake = new WPI_TalonSRX(Constants.IntakeConstants.intakeID);

    private final StatusSignal<Double> leaderVelocity = intake.getSelectedSensorVelocity();
    private final StatusSignal<Double> leaderAppliedVolts = leader.getMotorVoltage();
    private final StatusSignal<Double> leaderCurrent = leader.getStatorCurrent();
    private final StatusSignal<Double> followerCurrent = follower.getStatorCurrent();

    public IntakeIOTalonFX() {
        var config = new TalonFXConfiguration();
        config.CurrentLimits.StatorCurrentLimit = 30.0;
        config.CurrentLimits.StatorCurrentLimitEnable = true;
        config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
        leader.getConfigurator().apply(config);
        follower.getConfigurator().apply(config);
        follower.setControl(new Follower(leader.getDeviceID(), false));

        BaseStatusSignal.setUpdateFrequencyForAll(
                50.0, leaderPosition, leaderVelocity, leaderAppliedVolts, leaderCurrent, followerCurrent);
        leader.optimizeBusUtilization();
        follower.optimizeBusUtilization();
    }

    @Override
    public void updateInputs(IntakeIOInputs inputs) {
        BaseStatusSignal.refreshAll(
                leaderPosition, leaderVelocity, leaderAppliedVolts, leaderCurrent, followerCurrent);
        inputs.positionRad = Units.rotationsToRadians(leaderPosition.getValueAsDouble()) / Constants.IntakeConstants.gearRatio;
        inputs.velocityRadPerSec = Units.rotationsToRadians(leaderVelocity.getValueAsDouble()) / Constants.IntakeConstants.gearRatio;
        inputs.appliedVolts = leaderAppliedVolts.getValueAsDouble();
        inputs.currentAmps = new double[] { leaderCurrent.getValueAsDouble(), followerCurrent.getValueAsDouble() };
    }

    @Override
    public void setVoltage(double volts) {
        leader.setControl(new VoltageOut(volts));
    }

    @Override
    public void setVelocity(double velocityRadPerSec, double ffVolts) {
        leader.setControl(
                new VelocityVoltage(
                        Units.radiansToRotations(velocityRadPerSec),
                        0.0,
                        true,
                        ffVolts,
                        0,
                        false,
                        false,
                        false));
    }

    @Override
    public void stop() {
        leader.stopMotor();
    }

    @Override
    public void configurePID(double kP, double kI, double kD) {
        var config = new Slot0Configs();
        config.kP = kP;
        config.kI = kI;
        config.kD = kD;
        leader.getConfigurator().apply(config);
    }
}