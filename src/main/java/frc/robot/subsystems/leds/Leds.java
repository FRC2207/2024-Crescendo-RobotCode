package frc.robot.subsystems.leds;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.leds.LedsIO;
import frc.robot.subsystems.leds.LedsIO.Section;

public class Leds extends SubsystemBase {
    
    public static LedsIO io;

    public Leds(LedsIO io) {
        this.io = io;

        setDefaultCommand(
        run(() -> {
          io.rainbow(Section.FULL);
        }));
    }

}
