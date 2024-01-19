package frc.robot.subsystems.drive;

import com.ctre.phoenix6.hardware.CANcoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkLowLevel.PeriodicFrame;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.RobotController;
import frc.robot.Constants;
import frc.robot.RobotContainer;
import frc.robot.util.CleanSparkMaxValue;
import frc.robot.util.SparkMaxPeriodicFrameConfig;

public class ModuleIOSparkMax implements ModuleIO {
    private CANSparkMax driveSparkMax;
    private CANSparkMax turnSparkMax;

    private final RelativeEncoder driveEncoder;
    private final RelativeEncoder turnRelativeEncoder;
    
    // I've opted to also add our CANCoders in here for the time being. These can be moved if we see a need, although I don't see us mounting anything else on these modules right now
    private CANcoder turnAbsoluteEncoder;

    // SDS MK4i L3 Gearing
    private final double driveAfterEncoderReduction = (50.0 / 14.0) * (16.0 / 28.0 ) * (45.0 / 15.0 );
    private final double turnAfterEncoderReduction = 150.0 / 7.0;

    private final boolean isTurnMotorInverted = true;
    private final Rotation2d absoluteEncoderOffset;

    public ModuleIOSparkMax(int index) {
        System.out.println("[Init] Creating ModuleIOSparkMax" + Integer.toString(index));

        switch (index) {
            case 0:
                driveSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.FrontLeft.drive, MotorType.kBrushless);
                turnSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.FrontLeft.turn, MotorType.kBrushless);
                turnAbsoluteEncoder = new CANcoder(Constants.SwerveConstants.Modules.FrontLeft.encoder);
                absoluteEncoderOffset = new Rotation2d(Constants.SwerveConstants.Modules.FrontLeft.encoderOffset);
                break;
            case 1:
                driveSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.FrontRight.drive, MotorType.kBrushless);
                turnSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.FrontRight.turn, MotorType.kBrushless);
                turnAbsoluteEncoder = new CANcoder(Constants.SwerveConstants.Modules.FrontRight.encoder);
                absoluteEncoderOffset = new Rotation2d(Constants.SwerveConstants.Modules.FrontRight.encoderOffset);
                break;
            case 2:
                driveSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.BackLeft.drive, MotorType.kBrushless);
                turnSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.BackLeft.turn, MotorType.kBrushless);
                turnAbsoluteEncoder = new CANcoder(Constants.SwerveConstants.Modules.BackLeft.encoder);
                absoluteEncoderOffset = new Rotation2d(Constants.SwerveConstants.Modules.BackLeft.encoderOffset);
                break;
            case 3:
                driveSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.BackRight.drive, MotorType.kBrushless);
                turnSparkMax = new CANSparkMax(Constants.SwerveConstants.Modules.BackRight.turn, MotorType.kBrushless);
                turnAbsoluteEncoder = new CANcoder(Constants.SwerveConstants.Modules.BackRight.encoder);
                absoluteEncoderOffset = new Rotation2d(Constants.SwerveConstants.Modules.BackRight.encoderOffset);
                break;
            default:
                throw new RuntimeException("Invalid module index for ModuleIOSparkMax");
        }

        // Insert burn logic here...

        driveSparkMax.setCANTimeout(500);
        turnSparkMax.setCANTimeout(500);

        driveEncoder = driveSparkMax.getEncoder();
        turnRelativeEncoder = turnSparkMax.getEncoder();

        // Spark Max Config
        // We'll set these values in memory. We aren't burning for now. This can be updated later...
        for (int i = 0; i < 2; i++) {
            SparkMaxPeriodicFrameConfig.configNotLeader(driveSparkMax);
            SparkMaxPeriodicFrameConfig.configNotLeader(turnSparkMax);
            driveSparkMax.setPeriodicFramePeriod(PeriodicFrame.kStatus2, 10);
      
            turnSparkMax.setInverted(isTurnMotorInverted);
      
            driveSparkMax.setSmartCurrentLimit(40);
            turnSparkMax.setSmartCurrentLimit(30);
            driveSparkMax.enableVoltageCompensation(12.0);
            turnSparkMax.enableVoltageCompensation(12.0);
      
            driveEncoder.setPosition(0.0);
            driveEncoder.setMeasurementPeriod(10);
            driveEncoder.setAverageDepth(2);
      
            turnRelativeEncoder.setPosition(0.0);
            turnRelativeEncoder.setMeasurementPeriod(10);
            turnRelativeEncoder.setAverageDepth(2);
        }

        driveSparkMax.setCANTimeout(0);
        turnSparkMax.setCANTimeout(0);

        driveSparkMax.burnFlash();
        turnSparkMax.burnFlash();
    }

    public void updateInputs(ModuleIOInputs inputs) {
        inputs.drivePositionRad = 
            CleanSparkMaxValue.cleanSparkMaxValue(
                inputs.drivePositionRad,
                Units.rotationsToRadians(driveEncoder.getPosition()) / driveAfterEncoderReduction);
            CleanSparkMaxValue.cleanSparkMaxValue(
                inputs.driveVelocityRadPerSec,
                Units.rotationsPerMinuteToRadiansPerSecond(driveEncoder.getVelocity()) / driveAfterEncoderReduction);
        inputs.driveAppliedVolts = driveSparkMax.getAppliedOutput() * driveSparkMax.getBusVoltage();
        inputs.driveCurrentAmps = new double[] {driveSparkMax.getOutputCurrent()};
        inputs.driveTempCelcius = new double[] {driveSparkMax.getMotorTemperature()};

        inputs.turnAbsolutePositionRad =
            MathUtil.angleModulus(
                new Rotation2d(
                    turnAbsoluteEncoder.getAbsolutePosition().getValueAsDouble()
                    * 2.0
                    * Math.PI)
                .minus(absoluteEncoderOffset)
                .getRadians());
        inputs.turnPositionRad =
            CleanSparkMaxValue.cleanSparkMaxValue(
                inputs.turnPositionRad, 
                Units.rotationsToRadians(turnRelativeEncoder.getPosition())
                / turnAfterEncoderReduction);
        inputs.turnAppliedVolts = turnSparkMax.getAppliedOutput() * turnSparkMax.getBusVoltage();
        inputs.turnCurrentAmps = new double[] {turnSparkMax.getOutputCurrent()};
        inputs.turnTempCelcius = new double[] {turnSparkMax.getMotorTemperature()};
    }

    public void setDriveVoltage(double volts) {
        driveSparkMax.setVoltage(volts);
    }

    public void setTurnVoltage(double volts) {
        turnSparkMax.setVoltage(volts);
    }

    public void setDriveBrakeMode(boolean enable) {
        driveSparkMax.setIdleMode(enable ? IdleMode.kBrake : IdleMode.kCoast);
    }

    public void setTurnBrakeMode(boolean enable) {
        turnSparkMax.setIdleMode(enable ? IdleMode.kBrake : IdleMode.kCoast);
    }
}
