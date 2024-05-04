package frc.robot.subsystems.pivot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import frc.robot.Constants;

public class PivotIOSim implements PivotIO {
    // private final Encoder m_encoder = new
    // Encoder(Constants.IntakeConstants.pivotEncoderID,
    // Constants.IntakeConstants.pivotEncoderID);

    public PivotIOSim() {
        System.out.println("Creating PivotIOSim");
    }

    private final SingleJointedArmSim armSim = new SingleJointedArmSim(
            DCMotor.getNEO(1),
            60,
            SingleJointedArmSim.estimateMOI(12,
                    3.6),
            12,
            0,
            Units.degreesToRadians(177),
            true,
            175,
            VecBuilder.fill(0));

    private double appliedVolts = 0.0;

    public void updateInputs(PivotIOInputs inputs) {
        armSim.update(0.02);

        inputs.encoderPosition = getMeasurement();
        inputs.velocityRadPerSec = armSim.getVelocityRadPerSec();
        inputs.appliedVolts = appliedVolts;
        inputs.currentAmps = new double[] {};
    }

    /** Run the pivot at the specified voltage. */
    public void setPivotVoltage(double volts) {
        volts = MathUtil.clamp(volts, -12, 12);
        armSim.setInputVoltage(volts);
        this.appliedVolts = volts;
    }

    /** Get the measurement of an encoder. */
    public double getMeasurement() {
        return armSim.getAngleRads();
    }
}