package frc.robot.subsystems.pivot;

import com.revrobotics.CANSparkMax;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import frc.robot.Constants;

public class PivotIOSim implements PivotIO {
    private DCMotorSim pivotSim = new DCMotorSim(DCMotor.getCIM(1), 1, 0.0001);

    private boolean closedLoop = false;
    private double ffVolts = 0.0;
    private double appliedVolts = 0.0;

    @Override
    public double getMeasurement(){ 
        return 0;     // temp
    }

    @Override
    public void updateInputs(PivotIOInputs inputs) {}
}
