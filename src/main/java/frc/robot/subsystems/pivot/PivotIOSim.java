package frc.robot.subsystems.pivot;

import com.revrobotics.CANSparkMax;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.util.Units;
import frc.robot.Constants;

public class PivotIOSim implements PivotIO {
    private final CANSparkMax pivotMotor = new CANSparkMax(Constants.IntakeConstants.pivotMotorID, kBrushless);

    private boolean closedLoop = false;
    private double ffVolts = 0.0;
    private double appliedVolts = 0.0;

    @Override
    public void updateInputs(PivotIOInputs inputs) {
    
    if (closedLoop) {
      appliedVolts =
          MathUtil.clamp(pid.calculate(sim.getAngularVelocityRadPerSec()) + ffVolts, -12.0, 12.0);
      sim.setInputVoltage(appliedVolts);
    }
}
