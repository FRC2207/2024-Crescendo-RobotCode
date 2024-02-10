package frc.robot.subsystems.drive;

import com.kauailabs.navx.frc.*;
import edu.wpi.first.math.util.Units;


public class GyroIONavX2 implements GyroIO{
    private final AHRS navX2 = new AHRS();
    
    public GyroIONavX2() {
        System.out.println("[Init] Creating GyroIOnavX2");

        navX2.reset();
    }

    public void updateInputs(GyroIOInputs inputs) {
        double angle = navX2.getYaw();
        double rate = navX2.getRawGyroZ();
        inputs.connected = navX2.isConnected();
        inputs.rollPositionRad = navX2.getRoll();
        inputs.pitchPositionRad = navX2.getPitch();
        inputs.yawPositionRad = Units.degreesToRadians(angle);
        inputs.rollVelocityRadPerSec = Units.degreesToRadians(navX2.getRawGyroX());
        inputs.pitchVelocityRadPerSec = Units.degreesToRadians(navX2.getRawGyroY());
        inputs.yawVelocityRadPerSec = Units.degreesToRadians(rate);
    }
}
