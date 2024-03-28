package frc.robot.commands;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.pivot.Pivot;

public class IntakeGroundAuto extends SequentialCommandGroup {
    public IntakeGroundAuto (Intake intake, Pivot pivot) {
        addCommands(
            new ParallelCommandGroup(pivot.stupidPIDCommand(Units.degreesToRadians(5)), intake.continuousCommand()),
            pivot.stupidPIDCommand(Units.degreesToRadians(165))
        );
    }
}
