package frc.robot.subsystems.drive;

import com.kauailabs.navx.frc.*;
import edu.wpi.first.math.util.Units;


public class GyroIOnavX2 implements GyroIO{
    private final AHRS navX2 = new AHRS();
    
    public GyroIOnavX2() {
        System.out.println("[Init] Creating GyroIOnavX2");

        navX2.reset();
    }

    public void updateInputs(GyroIOInputs inputs) {
        double angle = -navX2.getAngle();
        double rate = -navX2.getRate();
        inputs.connected = navX2.isConnected();
        inputs.rollPositionRad = Units.degreesToRadians(navX2.getRawGyroX());
        inputs.pitchPositionRad = Units.degreesToRadians(navX2.getRawGyroY());
        inputs.yawPositionRad = Units.degreesToRadians(angle);
        inputs.rollVelocityRadPerSec = 0.0;
        inputs.pitchVelocityRadPerSec = 0.0;
        inputs.yawVelocityRadPerSec = Units.degreesToRadians(rate);
    }
}
