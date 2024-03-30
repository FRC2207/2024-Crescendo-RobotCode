package frc.robot.subsystems.climber;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.Constants.ClimberConstants;

public class Climber extends SubsystemBase {
    private final double upSpeed = 0.5;
    private final double downSpeed = -0.25;
    private final double adjustmentSpeed = -0.125;

    private final ClimberIO io;
    private final GyroIO gyro;
    private final ClimberIOInputsAutoLogged inputs = new ClimberIOInputsAutoLogged();

    public Climber(ClimberIO io, GyroIO gyro) {
        this.io = io;
        this.gyro = gyro;

        setDefaultCommand(
                run(() -> {
                    io.setLeftSpeed(0.0);
                    io.setRightSpeed(0.0);
                }));
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Launcher", inputs);

    }

    /** command to climb autonomously once in position */
    public Command autoClimb() {
        return (runOnce(() -> {
            Commands.print("Autonomous climb has been initiated");
            io.setLeftPosition(ClimberConstants.maxPosition);
            io.setRightPosition(ClimberConstants.maxPosition);
        })).andThen(
                runOnce(() -> {
                    io.setLeftSpeed(0.0);
                    io.setRightSpeed(0.0);
                })).andThen(
                        Commands.print("Climb is ready, please drive to position, countdown initiated"),
                        countDown())
                .andThen(
                        runOnce(() -> {
                            io.setLeftPosition(ClimberConstants.minimumElevation);
                            io.setRightPosition(ClimberConstants.minimumElevation);
                        }))
                .andThen(
                        runOnce(() -> {
                            io.setLeftSpeed(0.0);
                            io.setRightSpeed(0.0);
                            Commands.print("Climb is in position, autoAdjust initiated");
                        }))
                .andThen(
                        autoClimbAdjustmentCommand());
    }

    /** Runs a 5 second countdown with feedback */
    public Command countDown() {
        return Commands.sequence(
                runOnce(() -> {
                    Commands.print("5 seconds remaining");
                }),
                Commands.waitSeconds(1.0),
                runOnce(() -> {
                    Commands.print("4 seconds remaining");
                }),
                Commands.waitSeconds(1.0),
                runOnce(() -> {
                    Commands.print("3 seconds remaining");
                }),
                Commands.waitSeconds(1.0),
                runOnce(() -> {
                    Commands.print("2 seconds remaining");
                }),
                Commands.waitSeconds(1.0),
                runOnce(() -> {
                    Commands.print("1 seconds remaining");
                }),
                Commands.waitSeconds(1.0),
                runOnce(() -> {
                    Commands.print("0 seconds remaining");
                }));
    }

    /** automatically balances the robot while hanging */
    public Command autoClimbAdjustmentCommand() {
        return Commands.run(() -> {
            if (gyro.getLeftRightAngle() < 0) {
                io.setLeftSpeed(adjustmentSpeed);
            } else if (gyro.getLeftRightAngle() > 0) {
                io.setRightSpeed(adjustmentSpeed);
            } else {
                Commands.print("Climb Successful, continuing to balance");
                io.setLeftSpeed(0.0);
                io.setRightSpeed(0.0);
            }
        });
    }

    /** sets the upward voltage to both arms */
    public Command upBothCommand() {
        return Commands.run(
                () -> {
                    io.setLeftSpeed(upSpeed);
                    io.setRightSpeed(upSpeed);
                });
    }

    /** sets the upward voltage to the left arm only */
    public Command upLeftCommand() {
        return Commands.run(
                () -> {
                    io.setLeftSpeed(upSpeed);
                });
    }

    /** sets the upward voltage to the right arm only */
    public Command upRightCommand() {
        return Commands.run(
                () -> {
                    io.setRightSpeed(upSpeed);
                });
    }

    /** sets the downward voltage to both arms */
    public Command downBothCommand() {
        return Commands.run(
                () -> {
                    io.setLeftSpeed(downSpeed);
                    io.setRightSpeed(downSpeed);
                });
    }

    /** sets the downward voltage to the left arm only */
    public Command downLeftCommand() {
        return Commands.run(
                () -> {
                    io.setLeftSpeed(downSpeed);
                });
    }

    /** sets the downward voltage to the right arm only */
    public Command downRightCommand() {
        return Commands.run(
                () -> {
                    io.setRightSpeed(downSpeed);
                });
    }

}
