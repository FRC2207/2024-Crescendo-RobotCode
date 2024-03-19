package frc.robot.commands;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.pivot.Pivot;

public class IntakeGround extends Command {
    private final Intake intake;
    private final Pivot pivot;

    public IntakeGround (Intake intake, Pivot pivot) {
        this.intake = intake;
        this.pivot = pivot;

        addRequirements(intake, pivot);
    }

    @Override
    public void initialize() {
        pivot.setIntakeAngle(Units.degreesToRadians(0));
        pivot.setShouldRunStupid(true);
        intake.setIntakeVoltageRaw(0.5);
    }

    @Override
    public void execute() {
        if (intake.hasNote()) {
            intake.setIntakeVoltageRaw(0);
            end(false);
        }
    }

    @Override
    public void end(boolean interrupted) {
        intake.setIntakeVoltageRaw(0);
        pivot.stupidPIDCommand(Units.degreesToRadians(165));
    }
}
