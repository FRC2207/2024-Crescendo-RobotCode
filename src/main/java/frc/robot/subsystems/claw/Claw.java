package frc.robot.subsystems.claw;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Claw extends SubsystemBase {
    private final double upSpeed = 0.5;
    private final double downSpeed = -1 * 0.25;

    private final ClawIO io;
    private final ClawIOInputsAutoLogged inputs = new ClawIOInputsAutoLogged();

    public Claw(ClawIO io) {
        this.io = io;

        setDefaultCommand(
                run(
                        () -> {
                            io.setLeftVoltage(0.0);
                            io.setRightVoltage(0.0);
                        }));
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Launcher", inputs);

    }

    /** sets the upward voltage to both arms */
    public Command upBothCommand() {
        return Commands.run(
                () -> {
                    io.setLeftVoltage(upSpeed);
                    io.setRightVoltage(upSpeed);
                });
    }

    /** sets the upward voltage to the left arm only */
    public Command upLeftCommand() {
        return Commands.run(
                () -> {
                    io.setLeftVoltage(upSpeed);
                });
    }

    /** sets the upward voltage to the right arm only */
    public Command upRightCommand() {
        return Commands.run(
                () -> {
                    io.setRightVoltage(upSpeed);
                });
    }


    /** sets the downward voltage to both arms */
    public Command downBothCommand() {
        return Commands.run(
                () -> {
                    io.setLeftVoltage(downSpeed);
                    io.setRightVoltage(downSpeed);
                });
    }

    /** sets the downward voltage to the left arm only */
    public Command downLeftCommand() {
        return Commands.run(
                () -> {
                    io.setLeftVoltage(downSpeed);  
                });
    }

    /** sets the downward voltage to the right arm only */
    public Command downRightCommand() {
        return Commands.run(
                () -> {
                    io.setRightVoltage(downSpeed);  
                });
    }


}
